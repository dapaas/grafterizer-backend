# Installer script for Java JDK 8 and Glassfish 4.1 targeting Ubuntu 14.04
# Will also install leiningen and put jarfter scripts and templates on expected
# locations, as well as compiling the jarfter maven project and deploying it
# in Glassfish

# Update application database
sudo aptitude update

#Install Java JDK 8
sudo aptitude install -y --with-recommends software-properties-common

sudo add-apt-repository -y ppa:webupd8team/java
sudo aptitude update

# Requires approving Oracles licence:
sudo aptitude -y --with-recommends install oracle-java8-installer


# Download and unzip glassfish
wget http://download.java.net/glassfish/4.1/release/glassfish-4.1-web.zip
sudo aptitude install -y unzip zip
unzip glassfish-4.1*.zip -d ~


# Start glassfish 
asadmin=~/glassfish4/bin/asadmin
$asadmin start-domain

# Download leiningen
wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
chmod a+x lein
sudo mv lein /usr/local/bin/

# Run setup script for lein without sudo
lein

# Compile jarfter-template and rename the standalone jar 
cd jarfter-template
mvn deploy:deploy-file -Dfile=ww-geo-coords-1.0.jar -DgroupId=ww-geo-coords -DartifactId=ww-geo-coords -Dversion=1.0 -Dpackaging=jar -DlocalRepositoryPath=maven_repository -Durl=file:maven_repository
lein uberjar
cp target/jarfter-0.1.0-SNAPSHOT-standalone.jar jarfter.jar
cd -

# Copy clj2jar to available location:
sudo cp clj2jar.sh /usr/local/bin/
sudo mkdir -p /usr/local/var/
sudo cp -r jarfter-template /usr/local/var/jarfter-template
# The jarfter.jar is now available as /usr/local/var/jarfter-template/jarfter.jar

# Compile warfter-template and rename the standalone war
cd warfter-ws/warfter-ws
mvn deploy:deploy-file -Dfile=ww-geo-coords-1.0.jar -DgroupId=ww-geo-coords -DartifactId=ww-geo-coords -Dversion=1.0 -Dpackaging=jar -DlocalRepositoryPath=maven_repository -Durl=file:maven_repository
lein ring uberwar
cp target/warfter-ws-0.1.0-SNAPSHOT-standalone.war warfter-ws.war
cd -

# Copy clj2war to available location:
sudo cp clj2war.sh /usr/local/bin/
sudo cp -r warfter-ws/warfter-ws /usr/local/var/warfter-template
# The warfter-ws.war is now available as /usr/local/var/warfter-template/warfter-ws.war

# BUILD and deploy the web service

# copy config file to proper location:
sudo cp jarfter_config.json /usr/local/var/jarfter_config.json

# Install maven
sudo aptitude install -y maven

# Go into maven project folder and build jarfter:
cd ../jarfter
mvn package
#Deploy jarfter in Glassfish
cp target/jarfter-1.0-SNAPSHOT.war target/jarfter.war
$asadmin deploy target/jarfter.war

cd ../scripts



 
