import cgi

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext import db
from google.appengine.ext.webapp.util import run_wsgi_app

class Data(db.Model):
  path = db.StringProperty()
  data = db.StringProperty()

class MData(db.Model):
  path = db.StringProperty()
  data = db.IntegerProperty()
  
class Max(db.Model):
  data = db.TextProperty()
  path = db.StringProperty()

def dbfunc():
    data = Data()
    
    data.path = "abc"
    data.data = "some_data"
    
    data.put() #store object in database

    for i in range(1, 5):
      mdata = MData(parent=data)
      mdata.path = "abc"
      mdata.data = i
      mdata.put()
      
    return
  
class Size(webapp.RequestHandler):
  def post(self):
    m = Max()
    m.data = self.request.get('upfile') #1MB
    #m.put()
    self.response.out.write(len(m)) 

class Test(webapp.RequestHandler):
  def get(self):
    self.response.out.write('TESTING')
    try:
      db.run_in_transaction(dbfunc)
    except db.TransactionFailedError: 
      self.response.out.write('FAILED')
      return
    
    self.response.out.write('SUCCESS')

class MainPage(webapp.RequestHandler):
  def get(self):
    self.response.out.write("""
      <html>
        <body>
          <form enctype="multipart/form-data" action="./size" method="post">
            <div><input type=file name=upfile></div>
            <div><input type="submit" value="Test></div>
          </form>
        </body>
      </html>""")

application = webapp.WSGIApplication(
                                     [('/trans/', MainPage),
                                      ('/trans/test', Test),
                                      ('/trans/size', Size)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()