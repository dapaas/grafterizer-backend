#!/bin/bash

# Check that we have all the arguments that we need
if [ "$#" != 3 ];
then
    echo "Usage: $0 <workFolder> <clojureFileName> <jarFileName>"
    exit
fi

workFolder=$1
clojureFileName=$2
jarFileName=$3

cd $workFolder

log=$workFolder/log.txt

echo "Copying template jar into tmp folder" > $log
cp -r /usr/local/var/jarfter-template/jarfter.jar $jarFileName >> $log

echo "Copying the jar layout for the clojure file" >> $log
mkdir -p grafterizer
cp $clojureFileName grafterizer/transformation.clj

echo "Copying input clojure code into jar file" >> $log
# Replace clojure code
zip -u $jarFileName grafterizer/transformation.clj >> $log

echo "DONE" >> $log
