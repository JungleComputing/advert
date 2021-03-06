import cgi

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app

class MainPage(webapp.RequestHandler):
  def get(self):
    user = users.get_current_user()
    
    if user:
      self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('Hello')
      if users.is_current_user_admin():
        self.response.out.write(' administator')
        
      
      self.response.out.write(', ' + user.nickname() + '.')
    else:
      self.redirect(users.create_login_url(self.request.uri))
    
class Bla(webapp.RequestHandler):
  def get(self):
    self.response.out.write("bla");

application = webapp.WSGIApplication(
                                     [('/cookies/', MainPage),
                                      ('/cookies/bla', Bla)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()