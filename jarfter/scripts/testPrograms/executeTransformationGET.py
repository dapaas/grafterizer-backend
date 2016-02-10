#!/bin/env python
try:
    # For Python 3.0 and later
    from urllib.request import urlopen, Request, HTTPError
except ImportError:
    # Fallback to Python 2
    from urllib2 import urlopen, Request, HTTPError
    
import sys
import os

if len(sys.argv) < 4:
    print ("Usage:")
    print ("\t %s transform_uri files_fileid transformed_filename" % sys.argv[0])
    exit()

transform_uri = sys.argv[1]
files_fileid = sys.argv[2]
transformed_filename = sys.argv[3]

endpoint = "http://localhost:8088/jarfter/webresources/transform"
#endpoint = "http://192.168.11.43:8080/jarfter/webresources/transform"

headers = {}
headers["transformations_uri"] = transform_uri
headers["files_fileid"] = files_fileid
headers["transformed_filename"] = transformed_filename

with open(transformed_filename, "wb") as output:
    request = Request(url = endpoint, headers=headers)
    try:
        result = urlopen(request)
        print ("HTML response: " , str(result.getcode()))
    
        while True:
            buffer = result.read()
            if not buffer:
                break
            output.write(buffer)
                
    except HTTPError, error:
        print error
        print (error.read())
