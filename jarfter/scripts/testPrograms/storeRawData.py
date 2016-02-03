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
    print ("\t %s input_rawData" % sys.argv[0])
    exit()

input_rawData = sys.argv[1]

#endpoint = "http://localhost:8088/jarfter/webresources/jarCreator"
endpoint = "http://192.168.11.43:8080/jarfter/webresources/rawData"

headers = {}
headers["Content-Length"]= "%d" % os.stat(input_rawData).st_size
headers["Content-type"] = "text/txt; charset=utf-8"

headers["files_filename"] = input_rawData
    
with open(input_rawData, "r") as data:
    request = Request(url = endpoint, data=data, headers=headers)
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
                

