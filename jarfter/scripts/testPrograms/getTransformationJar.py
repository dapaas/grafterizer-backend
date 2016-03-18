#!/bin/env python
try:
    # For Python 3.0 and later
    from urllib.request import urlopen, Request, HTTPError
except ImportError:
    # Fallback to Python 2
    from urllib2 import urlopen, Request, HTTPError
    
import sys
import os

if len(sys.argv) < 3:
    print ("Usage:")
    print ("\t %s transform_uri output_jar" % sys.argv[0])
    exit()

transform_uri = sys.argv[1]
output_jar = sys.argv[2]

endpoint = "http://localhost:8080/jarfter/webresources/jarCreator"
#endpoint = "http://192.168.11.43:8080/jarfter/webresources/jarCreator"


headers = {}
headers["transform_uri"] = transform_uri

with open(output_jar, "wb") as jar:
    request = Request(url = endpoint, headers=headers)
    try:
        result = urlopen(request)
        print ("HTML response: " , str(result.getcode()))
        
        while True:
            buffer = result.read()
            if not buffer:
                break
            jar.write(buffer)
                
    except HTTPError, error:
        print error
        print (error.read())
