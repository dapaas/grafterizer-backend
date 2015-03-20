## Building

You need to copy the ```graftwerk.jar``` file in this folder. Then you can build the Docker container.


```docker build -t graftwerk .```

## Running

```docker run -d --publish=3000:3000 graftwerk```