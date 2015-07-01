var express = require('express'),
    compression = require('compression'),
    morgan = require('morgan'),
    request = require('request'),
    cors = require('cors'),
    bodyParser = require('body-parser');

var endpointOntotext = process.env.ONTOTEXT,
    endpointGraftwerk = process.env.GRAFTWERK;

var app = express();

app.use(compression());
app.use(morgan('short'));
app.use(cors());

var jsonParser = bodyParser.json();

app.get('/', function(req, res){
    res.send("Hei");
});


app.get('/poney', function(req, res) {
    var distributionUri = req.query.distributionUri;
    if (!distributionUri) {
        res.status(418).json({error: "The distribution URI parameter is missing"});
        return;
    }
    request.get({
        url: endpointOntotext + "/catalog/distributions/file",
        headers: {
            'distrib-id': distributionUri
        }
    }, function(err, response, body) {
        res.status(response.statusCode);

        res.contentType('text/plain');
        res.send(body);
        return;
    });
});

app.get('/vache', function(req, res) {
    var distributionUri = req.query.distributionUri;
    if (!distributionUri) {
        res.status(418).json({error: "The distribution URI parameter is missing"});
        return;
    }
    request.get({
        url: endpointOntotext + "/catalog/distributions/file",
        headers: {
            'distrib-id': distributionUri
        }
    }, function(err, response, body) {
        request.post({
            url: endpointGraftwerk+"/evaluate/pipe",
            json: true,
            formData: {
                pipeline: {
                    value: '(defpipe my-pipe [data-file] (-> (read-dataset data-file :format :csv)))',
                    options: {
                        filename: 'pipeline.clj',
                        contentType: 'text/plain'
                    }
                },
                data: {
                    value: body,
                    options: {
                        filename: 'data.csv',
                        contentType: 'text/csv'
                    }
                },
                command: req.query.command || 'my-pipe'
            }
        }, function(err, response, body) {
            res.status(response.statusCode);

            res.contentType('text/plain');
            res.send(body);
            return;
        });
    });
});

app.post('/lapin', jsonParser, function(req, res) {

    if (req.body) {
        var distributionUri = req.body.distributionUri,
            clojure = req.body.clojure;
    }

    if (!distributionUri) {
        res.status(418).json({error: "The distribution URI parameter is missing"});
        return;
    }

    if (!clojure) {
        res.status(418).json({error: "The clojure transformation code is missing"});
        return;
    }

    request.get({
        url: endpointOntotext + "/catalog/distributions/file",
        headers: {
            'distrib-id': distributionUri
        }
    }, function(err, response, body) {

        var type = req.body.transformationType === "graft" ? "graft" : "pipe";

        if (response.statusCode === 200) {
            /*request.post({
                url: endpointGraftwerk+"/evaluate/"+type,
                json: true,
                formData: {
                    pipeline: {
                        value: clojure,
                        options: {
                            filename: 'pipeline.clj',
                            contentType: 'text/plain'
                        }
                    },
                    data: {
                        value: body,
                        options: {
                            filename: 'data.csv',
                            contentType: 'text/csv'
                        }
                    },
                    command: req.body.command || 'my-pipe'
                }
            }, function(err, response, body) {
                res.status(response.statusCode);

                res.contentType('text/plain');
                res.send(body);
                return;
            });
            return;*/
            request.post({
                url: endpointOntotext+"/dapaas-services/grafter/transformation/preview",
                json: true,
                headers: {
                    // 'transformation-id': transformationUri
                    "command": req.query.command || "my-pipe",
                    "transformation-type": req.query.transformationType || "pipe",
                },
                formData: {
                    "input-file": {
                        value: body,
                        options: {
                            filename: 'data.csv',
                            contentType: 'text/csv'
                        }
                    },
                    "transformation-code": {
                    value: clojure,
                    options: {
                        filename: 'data.csv',
                        contentType: 'text/csv'
                    }
                },
                } 
            }, function(err, response, body) {
                res.status(response.statusCode);

                res.contentType('text/plain');
                res.send(body);
                return;
            });
            return;
        }

        res.status(response.statusCode);

        res.contentType('text/plain');
        res.send(body);
        return;
    });
});

app.get('/download', function(req, res) {

    var distributionUri = req.query.distribution,
        transformationUri = req.query.transformation;

    if (!distributionUri) {
        res.status(418).json({error: "The distribution URI parameter is missing"});
        return;
    }

    if (!transformationUri) {
        res.status(418).json({error: "The transformation URI parameter is missing"});
        return;
    }

    request.get({
        url: endpointOntotext + "/catalog/distributions/file",
        headers: {
            'distrib-id': distributionUri
        }
    }, function(err, response, bodyFile) {

        if (response.statusCode === 200) {
            request.get({
                url: endpointOntotext + "/catalog/transformations/code/clojure",
                headers: {
                    'transformation-id': transformationUri
                }
            }, function(err, response, bodyClojure) {
                if (response.statusCode === 200) {
                    request.post({
                        url: endpointGraftwerk+"/evaluate/graft",
                        json: true,
                        formData: {
                            pipeline: {
                                value: bodyClojure,
                                options: {
                                    filename: 'pipeline.clj',
                                    contentType: 'text/plain'
                                }
                            },
                            data: {
                                value: bodyFile,
                                options: {
                                    filename: 'data.csv',
                                    contentType: 'text/csv'
                                }
                            },
                            command: req.query.command || 'my-graft'
                        }
                    }, function(err, response, body) {

                        res.status(response.statusCode);
                        res.contentType('application/n-triples');
                        res.setHeader('Content-disposition', 'attachment; filename=output.nt');
                        res.send(body);
                    });
                    return;
                }

                res.status(response.statusCode);
                res.contentType('text/plain');
                res.send(bodyClojure);
            });
            return;
        }

        res.status(response.statusCode);
        res.contentType('text/plain');
        res.send(bodyFile);
    });
});

var serverPort = process.env.HTTP_PORT || 8080;
var server = app.listen(serverPort, function() {
    console.log("Server started on http://localhost:"+serverPort+"/");
    console.log("Ontotext endpoint: "+endpointOntotext);
    console.log("Graftwerk endpoint: "+endpointGraftwerk);
});
