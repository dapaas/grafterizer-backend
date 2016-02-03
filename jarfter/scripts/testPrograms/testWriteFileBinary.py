#!/bin/env python
try:
    # For Python 3.0 and later
    from urllib.request import urlopen, Request
except ImportError:
    # Fallback to Python 2
    from urllib2 import urlopen, Request
    
import sys
import os

if len(sys.argv) < 2:
    print ("Usage:")
    print ("\t %s input_binary " % sys.argv[0])
    exit()

input_binary = sys.argv[1]

#endpoint = "http://localhost:8088/jarfter/webresources/jarCreator"
endpoint = "http://10.218.149.25:8088/jarfter/webresources/transform"
#endpoint = "http://52.17.69.234:8080/jarfter/webresources/jarCreator"


headers = {}
headers["Content-Length"]= "%d" % os.stat(input_binary).st_size
headers["Content-type"] = "application/octet-stream"

    
with open(input_binary, "r") as data:
    request = Request(url = endpoint, data=data, headers=headers)
    request.get_method = lambda: "PUT";
    result = urlopen(request)
    print ("HTML response: " , str(result.getcode()))

