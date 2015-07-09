var express = require('express'),
    compression = require('compression'),
    morgan = require('morgan'),
    request = require('request'),
    cors = require('cors'),
    bodyParser = require('body-parser');

var endpointOntotext = process.env.ONTOTEXT || "http://ontotext:8080";
var endpointGraftwerk = process.env.GRAFTWERK || "http://graftwerk:8080";

var app = express();

app.use(compression());
app.use(morgan('short'));
app.use(cors());

var jsonParser = bodyParser.json();

// Used to check the API access
app.get('/', function(req, res){
    res.send("Hei");
});

var sendRequestError = function(err, response, body, res) {
    res.status(response && response.statusCode ? response.statusCode : 500);

    if (body) {
        res.contentType('text/plain');
        res.send(body);
    } else {
        res.json({error: err});
    } 
};

var genericSuccessCallback = function(err, response, body, res) {
    if (err || (response && response.statusCode !== 200)) {
        sendRequestError(err, response, body, res);
    } else {
        res.contentType('text/plain');
        res.send(body);
    }
};

var downloadRaw = function(req, res, callbackSuccess) {

    var auth = req.headers.authorization;

    if (!auth) {
        res.status(401).json({error: "The authorization header is missing"});
        return;
    }

    var distributionUri = req.query.distributionUri;

    if (!distributionUri) {
        if (req.body && req.body.distributionUri) {
            distributionUri = req.body.distributionUri;
        } else {
            res.status(418).json({error: "The distribution URI parameter is missing"});
            return;
        }
    }

    request.get({
        url: endpointOntotext + "/catalog/distributions/file",
        headers: {
            'distrib-id': distributionUri,
            Authorization: auth
        }
    }, function(err, response, body) {
        if (err || (response && response.statusCode !== 200)) {
            sendRequestError(err, response, body);
        } else {
            callbackSuccess(response, body); 
        }
    });
};

app.get('/raw', function(req, res) {
    downloadRaw(req, res, function(response, body){
        res.contentType('text/plain');
        res.send(body);
    });
});

app.get('/original', function(req, res) {
    downloadRaw(req, res, function(response, body){
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
            genericSuccessCallback(err, response, body, res);
        });
    });
});

app.post('/preview', jsonParser, function(req, res) {

    var auth = req.headers.authorization;
    if (!auth) {
        res.status(401).json({error: "The authorization header is missing"});
    }


    if (!req.body || !req.body.clojure) {
        res.status(418).json({error: "The clojure transformation code is missing"});
        return;
    }

    var clojure = req.body.clojure;

    downloadRaw(req, res, function(response, body){
        var type = req.body.transformationType === "graft" ? "graft" : "pipe";

        request.post({
            url: endpointOntotext+"/dapaas-services/grafter/transformation/preview",
            json: true,
            headers: {
                "command": req.query.command || "my-pipe",
                "transformation-type": req.query.transformationType || "pipe",
                Authorization: auth
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
                }
            } 
        }, function(err, response, body) {
            genericSuccessCallback(err, response, body, res);
        });
    });
});

app.get('/download', function(req, res) {

    var auth = req.headers.authorization || req.query.key;
    if (!auth) {
        res.status(401).json({error: "The authorization header is missing"});
    }

    var transformationUri = req.query.transformation;

    if (!transformationUri) {
        res.status(418).json({error: "The transformation URI parameter is missing"});
        return;
    }

    downloadRaw(req, res, function(response, bodyFile){
        request.get({
            url: endpointOntotext + "/catalog/transformations/code/clojure",
            headers: {
                'transformation-id': transformationUri,
                Authorization: auth
            }
        }, function(err, response, bodyClojure) {
            if (err || (response && response.statusCode !== 200)) {
                sendRequestError(err, response, body);
                return;
            }

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

                if (err || (response && response.statusCode !== 200)) {
                    sendRequestError(err, response, body);
                    return;
                }

                res.status(response.statusCode);
                res.contentType('application/n-triples');
                res.setHeader('Content-disposition', 'attachment; filename=output.nt');
                res.send(body);
            });
        });

    });
});

var serverPort = process.env.HTTP_PORT || 8080;
var server = app.listen(serverPort, function() {
    console.log("Server started on http://localhost:"+serverPort+"/");
    console.log("Ontotext endpoint: "+endpointOntotext);
    console.log("Graftwerk endpoint: "+endpointGraftwerk);
});
