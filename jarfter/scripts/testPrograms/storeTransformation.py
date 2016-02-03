#!/bin/env python
try:
    # For Python 3.0 and later
    from urllib.request import urlopen, Request, HTTPError
except ImportError:
    # Fallback to Python 2
    from urllib2 import urlopen, Request, HTTPError
    
import sys
import os

if len(sys.argv) < 2:
    print ("Usage:")
    print ("\t %s input_clj {metadata}" % sys.argv[0])
    exit()

input_clj = sys.argv[1]

headers = {}
headers["Content-Length"]= "%d" % os.stat(input_clj).st_size
headers["Content-type"] = "text/txt; charset=utf-8"
headers["transformations_name"] = input_clj.split('/')[-1]

if len(sys.argv) > 2:
    headers["transformations_metadata"] = sys.argv[2]

endpoint = "http://192.168.11.43:8080/jarfter/webresources/jarCreator"
#endpoint = "http://localhost:8088/jarfter/webresources/jarCreator"    

with open(input_clj, "r") as clj:
    request = Request(url = endpoint, data=clj, headers=headers)
    try:
        request.get_method = lambda: "PUT";
        result = urlopen(request)
        print ("HTML response: " , str(result.getcode()))
    
        while True:
            buffer = result.read()
            if not buffer:
                break
            print(buffer)
    except HTTPError, error:
        print error
        print (error.read())

                

