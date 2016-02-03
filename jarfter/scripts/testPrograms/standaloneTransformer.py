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

if len(sys.argv) < 4:
    print ("Usage:")
    print ("\t %s input_clj input_csv output_rdf [ip:port]" % sys.argv[0])
    exit()

input_clj = sys.argv[1]
input_csv = sys.argv[2]
output_rdf = sys.argv[3]


endpoint = "http://192.168.11.43:8080/jarfter/webresources/transformStandAlone"
#endpoint = "http://localhost:8088/jarfter/webresources/transformStandAlone"

if len(sys.argv) > 4:
    endpoint = "http://" + sys.argv[4] + "/jarfter/webresources/transformStandAlone"
print ("Running on " + endpoint)


headers = {}
#headers["Content-Length"]= "%d" % os.stat(input_clj).st_size
#headers["Content-type"] = "txt/text; charset=utf-8"
headers["Content-type"] = "application/x-www-form-urlencoded"
#headers["transformed_filename"] = output_rdf

clojureString = ""
with open(input_clj, "r") as clj:
    clojureString = clj.read()

csvString = ""
with open(input_csv, "r") as csv:
    csvString = csv.read()


with open(output_rdf, "wb") as rdf:
    request = Request(url = endpoint, data=urlencode({'clojure': clojureString, 'csv': csvString, 'transformed_filename': output_rdf}), headers=headers)
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

