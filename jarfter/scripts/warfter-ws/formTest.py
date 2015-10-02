#!/bin/env python
try:
    # For Python 3.0 and later
    from urllib.request import urlopen, Request, HTTPError, urlencode
except ImportError:
    # Fallback to Python 2
    from urllib2 import urlopen, Request, HTTPError
    from urllib import urlencode
    
import sys
import os
#from urllib import urlencode

if len(sys.argv) < 3:
    print ("Usage:")
    print ("\t %s input_csv output_rdf" % sys.argv[0])
    exit()

input_csv = sys.argv[1]
output_rdf = sys.argv[2]


endpoint = "http://192.168.11.43:8080/grafter-ws/formTransform"
#endpoint = "http://localhost:8088/jarfter/webresources/transformStandAlone"

if len(sys.argv) > 3:
    endpoint = "http://" + sys.argv[3] + "/grafter-ws/formTransform"
print( endpoint )


headers = {}
headers["Content-type"] = "application/x-www-form-urlencoded"

csvString = ""
with open(input_csv, "r") as csv:
    csvString = csv.read()


with open(output_rdf, "wb") as rdf:
    request = Request(url = endpoint, data=urlencode({'csv': csvString, 'output': output_rdf}), headers=headers)
    try:
        request.get_method = lambda: "POST";
        result = urlopen(request)
        print ("HTML response: " , str(result.getcode()))
        
        while True:
            buffer = result.read()
            if not buffer:
                break
            rdf.write(buffer)
    except HTTPError, error:
        print error
        print (error.read())

