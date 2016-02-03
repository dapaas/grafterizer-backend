#!/bin/bash

# Check that we have all the arguments that we need
if [ "$#" != 3 ];
then
    echo "Usage: $0 <workFolder> <clojureFileName> <warFileName>"
    exit
fi

workFolder=$1
clojureFileName=$2
warFileName=$3

cd $workFolder

log=$workFolder/log.txt

echo "Copying template war into tmp folder" > $log
cp -r /usr/local/var/warfter-template/warfter-ws.war $warFileName >> $log

echo "Copying the war layout for the clojure file" >> $log
mkdir -p WEB-INF/classes/warfter_ws
cp $clojureFileName WEB-INF/classes/warfter_ws/core.clj

echo "Copying the input clojure code into the war file" >> $log
zip -u $warFileName WEB-INF/classes/warfter_ws/core.clj >> $log

echo "DONE" >> $log
