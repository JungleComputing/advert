import time

print 'Content-Type: text/plain'
print ''
print 'Hello, world!'

start = time.time()

for x in range(1000000):
  y = 100*x - 99 # do something

end = time.time()

print (end - start) * 1000