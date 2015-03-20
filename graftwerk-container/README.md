# Graftwerk-container

[Docker.io](https://www.docker.io/) container, running [Swirrl](http://www.swirrl.com/) Graftwerk server.

It exposes an HTTP REST API on the port 3000.

## Building

You need to copy the ```graftwerk.jar``` file in this folder. Then you can build the Docker container.


```docker build -t graftwerk .```

## Running

```docker run -d --publish=3000:3000 graftwerk```