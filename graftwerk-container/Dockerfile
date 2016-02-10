FROM java:8

COPY ./graftwerk.jar /usr/local/var/graftwerk.jar
COPY ./java.policy /usr/local/var/.java.policy

EXPOSE 8080

CMD java -server -jar /usr/local/var/graftwerk.jar