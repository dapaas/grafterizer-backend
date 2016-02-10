#!/bin/env python
try:
    # For Python 3.0 and later
    from urllib.request import urlopen, Request, HTTPError
except ImportError:
    # Fallback to Python 2
    from urllib2 import urlopen, Request, HTTPError
    
import sys
import os
import json

if len(sys.argv) < 1:
    print ("Usage:")
    print ("\t %s {transform_uri}" % sys.argv[0])
    exit()

if len(sys.argv) > 1:
    transform_uri = sys.argv[1]


#endpoint = "http://localhost:8088/jarfter/webresources/jarCreator"
endpoint = "http://192.168.11.43:8080/jarfter/webresources/jarCreator/listEntries"

headers = {}
if len(sys.argv) > 1:
    headers["transform_uri"] = transform_uri

request = Request(url = endpoint, headers=headers)
try:
    result = urlopen(request)
    if result.getcode() != 200: 
        print ("HTML response: " , str(result.getcode()))
    
    while True:
        buffer = result.read()
        if not buffer:
            break
        print(buffer)
except HTTPError, error:
    print error
    print (error.read())

# To pretty print: $ <run script> | python -mjson.tool

