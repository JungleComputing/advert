import cgi
import logging
import time

from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app

class MainPage(webapp.RequestHandler):
  def get(self):
    self.response.out.write("")

class MeasureUp(webapp.RequestHandler):
  def post(self):
    body = self.request.body
    self.response.out.write("")
    logging.info(len(body))
    
class MeasureDown(webapp.RequestHandler):
  def get(self):
    start = time.time()
    f = open('file.txt') # my_file.csv :  same level than main.py
    csv = f.read()
    stop = time.time()
    self.response.out.write(csv)
    logging.info((stop-start)*1000) 
    stop = time.time()
    logging.info(len(csv))
    logging.info((stop-start)*1000) 

class Guestbook(webapp.RequestHandler):
  def post(self):
    self.response.out.write('<html><body><b>%s</b> wrote:<pre>' 
      % cgi.escape(self.request.get('author')))
    self.response.out.write(cgi.escape(self.request.get('content')))
    self.response.out.write('</pre></body></html>')

application = webapp.WSGIApplication(
                                     [('/ping/', MainPage),
                                      ('/ping/up', MeasureUp),
                                      ('/ping/down', MeasureDown)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()