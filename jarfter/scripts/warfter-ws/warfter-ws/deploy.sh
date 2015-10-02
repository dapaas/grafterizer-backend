#!/bin/bash

lein ring uberwar
scp -i /home/havahol/Documents/keys/amazon/MOD_coop.pem target/grafter-ws-0.1.0-SNAPSHOT-standalone.war ubuntu@$1:/home/ubuntu/grafter-ws.war
ssh -i /home/havahol/Documents/keys/amazon/MOD_coop.pem ubuntu@$1 "/home/ubuntu/glassfish4/glassfish/bin/asadmin undeploy grafter-ws"
ssh -i /home/havahol/Documents/keys/amazon/MOD_coop.pem ubuntu@$1 "/home/ubuntu/glassfish4/glassfish/bin/asadmin deploy /home/ubuntu/grafter-ws.war"
