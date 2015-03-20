FROM dockerfile/java:oracle-java8

COPY ./graftwerk.jar $HOME/graftwerk.jar
COPY ./java.policy $HOME/.java.policy

EXPOSE 3000

CMD java -server -jar ~/graftwerk.jar