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
    print ("\t %s input_clj output_war" % sys.argv[0])
    exit()

input_clj = sys.argv[1]
output_war = sys.argv[2]

endpoint = "http://54.171.122.44:8080/jarfter/webresources/warfter"
#endpoint = "http://localhost:8088/jarfter/webresources/warfter"

headers = {}
#headers["Content-Length"]= "%d" % os.stat(input_clj).st_size
headers["Content-type"] = "application/x-www-form-urlencoded"

clojureSource = ""
with open(input_clj, "r") as clj:
    clojureSource = clj.read()

    
with open(output_war, "wb") as jar:
    #params = ('clojure': clj)
    request = Request(url = endpoint, data=urlencode({'clojure': clojureSource}), headers=headers)
    try:
        request.get_method = lambda: "POST";
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

