import cgi

from google.appengine.ext import webapp
from google.appengine.ext import db
from google.appengine.ext.webapp.util import run_wsgi_app
from django.utils import simplejson 

class Foo(db.Model):
  data = db.StringProperty()

class MainPage(webapp.RequestHandler):
  def get(self):
    self.response.out.write("""
      <html>
      <body>
      This is a test page for using JSON.
      </body>
      </html>""")
    
class Null(webapp.RequestHandler):
  def get(self):
 
    json = simplejson.loads("[\"foo\", \"bar\", null]")
     
    if json[0] is None:
      self.response.out.write("Found 1") 
    
    if json[1] is None:
       self.response.out.write("Found 2")
    
    if json[2] is None:
       self.response.out.write("Found 3")
       
#     json = simplejson.loads("[\"foo\", \"bar\", \"fubar\"]")
#     
#     self.response.out.write(json)
#     
#     self.response.out.write(json[2])
#     
#     try:
#       json[2].keys()
#     except:
#       self.response.out.write("JSON, could not load object")
       
     #json[2].keys()
    
#    try:
#      json = simplejson.loads("fhdiajhfiosdjiaofjsdiaofjioah")
#    except:
#      self.response.out.write("JSON ERROR\n")
#      
#    json = simplejson.loads("[\"James\", \"Jack's\", null]")
#    
#    self.response.out.write(json[2].keys())  
#    
#    self.response.out.write("done.\n")
    
#    foo = Foo()
#    foo.data = json[0]
#    foo.put()
#    
#    foo = Foo()
#    foo.data = json[1]
#    foo.put()
#
#    foo = Foo()
#    foo.data = json[2]
#    foo.put()

#    query = db.GqlQuery("SELECT * FROM Foo")
#    
#    foos = []
#    
#    for q in query:
#      foos.append(q.data)
#    
#    self.response.out.write(foos)
#    self.response.out.write(simplejson.dumps(foos))

class Download(webapp.RequestHandler):
  def post(self):
    data = self.request.body
    json = simplejson.loads(data)
    self.response.out.write(json[0] + '\n')
    for key in json[1].keys():
      self.response.out.write(key + " - " + json[1][key] + '\n')
    #self.response.out.write(json[2] + '\n')

class Display(webapp.RequestHandler):
  def get(self):
    self.response.out.write('TEST')
  
application = webapp.WSGIApplication(
                                     [('/json/', MainPage),
                                      ('/json/null', Null),
                                      ('/json/get', Download),
                                      ('/json/display', Display)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()