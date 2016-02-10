FROM glassfish:4.1

# Download Leiningen
RUN wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
RUN chmod a+x lein && mv lein /usr/local/bin/lein
RUN lein

# Copy the files
COPY ./jarfter.war /usr/local/var/jarfter.war
COPY ./scripts/clj2*.sh /usr/local/bin/
COPY ./jarfter-template /usr/local/var/jarfter-template/
COPY ./warfter-ws/warfter-ws /usr/local/var/warfter_template
COPY ./scripts/jarfter_config.json /usr/local/var/jarfter_config.json

# Compile jarfter-template and rename the standalone jar
WORKDIR /usr/local/var/jarfter-template/jarfter-template
RUN lein uberjar
RUN cp /usr/local/var/jarfter-template/target/jarfter-0.1.0-SNAPSHOT-standalone.jar /usr/local/var/jarfter-template/jarfter.jar

# Deploy jarfter
WORKDIR /usr/local/glassfish4/bin/
RUN ./asadmin start-domain && ./asadmin -u admin deploy /usr/local/var/jarfter.war && ./asadmin stop-domain 

