#!/usr/bin/env python
#
# Copyright (c) 2008, 2009, Bas Boterman, Vrije Universiteit, The Netherlands
# All rights reserved.
#
# Redistribution and use in source and binary forms,
# with or without modification, are permitted provided
# that the following conditions are met:
#
#    * Redistributions of source code must retain the above copyright
#      notice, this list of conditions and the following disclaimer.
#
#    * Redistributions in binary form must reproduce the above
#      copyright notice, this list of conditions and the following
#      disclaimer in the documentation and/or other materials provided
#      with the distribution.
#
#    * Neither the name of the Vrije Universiteit nor the names of its
#      contributors may be used to endorse or promote products derived
#      from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
# NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
# AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
# IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
# BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
# WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
# OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
# EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

import cgi
import datetime
import logging

from sets import Set
from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db
from django.utils import simplejson 

##Data types (Models)

class Advert(db.Model):
  path   = db.StringProperty()
  author = db.UserProperty()
  ttl    = db.DateTimeProperty(auto_now_add=True)
  object = db.TextProperty() #base64
  
class MetaData(db.Model):
  path   = db.StringProperty()
  keystr = db.StringProperty()
  value  = db.StringProperty()

##Private Functions

#Authentication
def auth(self): 
  if not users.get_current_user():
    self.error(403)
    self.response.headers['Content-Type'] = 'text/plain'
    self.response.out.write('Not Authenticated')
    return -1

  if not users.is_current_user_admin():
    self.error(403)
    self.response.headers['Content-Type'] = 'text/plain'
    self.response.out.write('No Administrator')
    return -1

  return 0

#Storing a JSON object 
def store(json):
  advert = Advert()
  user   = users.get_current_user()
  
  advert.path   = json[0] #extract path from message
  advert.author = user    #store author
  advert.object = json[2] #extract (base64) object from message

  advert.put() #store object in database
  
  if json[1] is None: #check if metadata is given
    return

  for k in json[1].keys():
    metadata        = MetaData(parent=advert)
    metadata.path   = json[0]
    metadata.keystr = k
    metadata.value  = json[1][k]
    metadata.put() #store metadata
  
  return
  
#Deleting an Object including MetaData
def remove(path):
  query1 = db.GqlQuery("SELECT * FROM Advert WHERE path = :1", path)
  query2 = db.GqlQuery("SELECT * FROM MetaData WHERE path = :1", path)
  
  keys = []
  
  for q1 in query1:
    keys.append(q1.key())
  
  for q2 in query2:
    keys.append(q2.key())
    
  try: #transaction remove
    db.run_in_transaction(db.delete, keys)
  except db.TransactionFailedError, message:
    logging.error(message) #log the error
    self.error(503)        #send response to client
    self.response.headers['Content-Type'] = 'text/plain'
    self.response.out.write(message)
    return -1
  
  return 0 #successful remove

#Garbage Collector
def gc(): 
  query = db.GqlQuery("SELECT * FROM Advert WHERE ttl < :1", datetime.datetime.today() + datetime.timedelta(days=-10))
  
  for advert in query: #all entities that can be deleted
    advert.delmd()     #delete all associated metadata
    advert.delete()    #delete the object itself

##Public Functions

class MainPage(webapp.RequestHandler):
  def get(self):
    self.response.out.write("""
      <html>
        <head>
          <title>Google App Engine Advert Server</title>
        </head>
        <body>
          <h1>Google App Engine Advert Server</h1>
          <h2>&copy; 2008-2009 Copyright: Bas Boterman, Vrije Universiteit Amsterdam</h2>
        </body>
      </html> 
    """)

class AddObject(webapp.RequestHandler):
  def post(self):
    if auth(self) < 0: return
    
    response = 201 #standard response
    body = self.request.body
    
    try: #try to load serialized form of JSON
      json = simplejson.loads(body)
    except:
      self.error(400)
      self.response.out.write("Failed to load JSON: unreadable content")
      return
      
    if len(json) is not 3: #check JSON length (must be 3)
      self.error(400)
      self.response.out.write("Failed to load JSON: object not properly structured")
      return
    
    if json[0] is None:
      self.error(400)
      self.response.out.write("Failed to load JSON: object not properly structured")
      return     
    
    if json[1] is not None:
      try:
        json[1].keys() #check if second array entry is a JSON object
      except:
        self.error(400)
        self.response.out.write("Failed to load JSON: object not properly structured")
    
    query = db.GqlQuery("SELECT * FROM Advert WHERE path = :1", json[0])
    if query.count() > 0: #this entry already exists; overwrite
      if remove(json[0]) is 0: #remove went well
        response = 205 #reset content

    try: #try storing the JSON object (in transaction)
      db.run_in_transaction(store, json) 
    except db.TransactionFailedError, message:
      logging.error(message) #log error message
      gc()                   #run garbage collector
      
      try: #second try
        db.run_in_transaction(store, json)
      except db.TransactionFailedError, message:
        logging.error(message) #log the error
        self.error(503)        #send response to client
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write(message)
        return
    
    self.response.http_status_message(response) #created/overwritten
    self.response.headers['Content-Type'] = 'text/plain'
    self.response.out.write('Expires: ') 
    self.response.out.write(datetime.datetime.today() + datetime.timedelta(days=10)) 
    return

class DelObject(webapp.RequestHandler):
  def post(self):
    if auth(self) < 0: return
    
    body  = self.request.body
    query1 = db.GqlQuery("SELECT * FROM Advert WHERE path = :1", body)
    query2 = db.GqlQuery("SELECT * FROM MetaData WHERE path = :1", body)
    
    if query1.count() < 1: #no matching object found
      self.error(404)
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('No Such Element')
      return      
    
    if remove(body) is 0: #remove went well
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('OK')
    
class GetObject(webapp.RequestHandler):
  def post(self):
    if auth(self) < 0: return
    
    body  = self.request.body
    query = db.GqlQuery("SELECT * FROM Advert WHERE path = :1", body)
    
    if query.count() < 1: #no matching object found
      self.error(404)
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('No Such Element')
      return      
    
    for advert in query:
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write(advert.object) #returning the first entry we find
      break #and stop
    
    return;
  
class GetMetaData(webapp.RequestHandler):
  def post(self):
    if auth(self) < 0: return
    
    body  = self.request.body
    query = db.GqlQuery("SELECT * FROM MetaData WHERE path = :1", body)
    
    if query.count() < 1: #no matching object found
      self.error(404)
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('No Such Element')
      return
    
    jsonObject = {}
    
    for metadata in query:
      jsonObject[metadata.keystr] = metadata.value
      
    self.response.headers['Content-Type'] = 'text/plain'
    self.response.out.write(simplejson.dumps(jsonObject))   

class FindMetaData(webapp.RequestHandler):
  def post(self):
    if auth(self) < 0: return
    
    body = self.request.body
    
    try: #try to load serialized form of JSON
      json = simplejson.loads(body)
    except:
      self.error(400)
      self.response.out.write("Failed to load JSON: unreadable content")
      return      
    
    if json is None: #no object will match metadata which is 'null'
      self.error(404)
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('Not Found')
      return      
      
    try: #check if second array entry is a JSON object
      json.keys() 
    except:
      self.error(400)
      self.response.out.write("Failed to load JSON: object not properly structured")
      return
    
    query = db.GqlQuery("SELECT * FROM MetaData")
    
    paths = Set()
    
    for bin in query:
      paths.add(bin.path)
      
    paths  = list(paths)
    
    for path in paths[:]:
      for k in json.keys():
        query = db.GqlQuery("SELECT * FROM MetaData WHERE path = :1 AND keystr = :2 AND value = :3", path, k, json[k])
        if query.count() < 1:
          paths.remove(path)
          break
    
    if len(paths) < 1:
      self.error(404)
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('Not Found')
      return
    
    self.response.out.write(simplejson.dumps(paths))  

class Login(webapp.RequestHandler):
  def get(self):
    self.redirect(users.create_login_url(self.request.uri))

application = webapp.WSGIApplication(
                                     [('/',      MainPage),
                                      ('/add',   AddObject),
                                      ('/del',   DelObject),
                                      ('/get',   GetObject),
                                      ('/getmd', GetMetaData),
                                      ('/find',  FindMetaData),
                                      ('/login', Login)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()