# Jarfter Web Service

We here provide web services for generating executable Jar's and deployable War's for doing transformations on tabular data. There are also services for doing server-side transformations.

Structure of the code:
 - The documentations can be found in doc/
 - Installer scripts are found in scripts/
 - Test scripts in python are found in scripts/testPrograms
 - The web services are in a maven projectin jarfter/


## Installing on Ubuntu 14.04

Given a blank Ubuntu image from AWS or a fresh installation, these steps should be sufficient to set-up a Glassfish server with the web services developed in this repo.

1. Install git and clone this repo

    ```bash
    sudo apt-get update
    sudo apt-get install git
    git clone https://github.com/sintefmath/MOD_coop.git
    ```

2. Navigate into the `scripts` directory and modify the `jarfter_config.json` file. This should contain the endpoint, username and password for you Postgresql database which you will use for Jarfter. The endpoint might look something like `jdbc:postgresql://ip_address:port/database_name`.

    ```bash
    cd MOD_coop/scripts
    vim jarfter_config.json
    ```

After this, run the `server_installer` scrip by ́`./server_installer.sh`


The installer script requires interaction for approving the licences from Oracle.

This script will do the following:
* Install Java JDK 8
* Install GlassFish 4.1
* Install Leiningen
* Copy clj2jar.sh and the template grafter project to expected locations
* Install Maven
* Compile the jarfter project and deploy it in GlassFish 
    
The endpoint for the web service should now be 
`http://<IP-address>:8080/jarfter/webresources/jarfter`

### Server support

The development of Jarfter has been done on GlassFish4.1.

The Warfter web service has been confirmed to work on GlassFish4.1, Jetty-9 and Tomcat-7.


