import datetime
import sys
import asizeof

print 'Content-Type: text/plain'
print ''
print 'Hello, world!'

t = datetime.datetime.now()

print asizeof.asizeof(t)