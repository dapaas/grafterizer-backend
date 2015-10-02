The API documentations uses APIBluePrint (https://apiblueprint.og/)

For compiling the .apio files into html you will first need to install NodeJS and NPM.

Install the Beta's version of aglio:

`npm install -g aglio@beta`


In order to start a web server (port 3000) for previewing the files:

`aglio -i jarfter.apib -s`

In order to generate the documentation as a HTML file:

`aglio -i jarfter.apib -o jarfter.html`

