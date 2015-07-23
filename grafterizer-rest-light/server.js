var express = require('express'),
    compression = require('compression'),
    morgan = require('morgan'),
    request = require('request'),
    cors = require('cors'),
    mime = require('mime'),
    contentDisposition = require('content-disposition'),
    path = require('path'),
    bodyParser = require('body-parser'),
    filesizeParser = require('filesize-parser');

if (process.env.DEBUG) {
    require('request-debug')(request);
}

var endpointOntotext = process.env.ONTOTEXT || "http://ontotext:8080";
var endpointGraftwerk = process.env.GRAFTWERK || "http://graftwerk:8080";
var maxPreviewSize = process.env.MAX_PREVIEW_SIZE ?
    filesizeParser(process.env.MAX_PREVIEW_SIZE) : filesizeParser('10MiB');

var app = express();

app.use(compression());
app.use(morgan('short'));
app.use(cors());

var jsonParser = bodyParser.json();

// To check the API status
app.get('/', function(req, res){
    res.send("Hei");
});

// Returns information about the file, using the content-disposition header
// Also returns default values as a failback (in CSV)
var getAttachmentInfos = function(response) {
    var defaultInfos = {
        type: 'csv',
        name: 'output',
        filename: 'output.csv',
        mime: 'text/csv'
    };

    if (!response.headers || !response.headers['content-disposition']) {
        return defaultInfos;
    }

    var disposition = contentDisposition.parse(response.headers['content-disposition']);

    if (!disposition.parameters || !disposition.parameters.filename) {
        return defaultInfos;
    }

    var filename = disposition.parameters.filename;
    var ext = path.extname(filename);

    return {
        type: ext.slice(1),
        name: path.basename(filename, ext),
        filename: path.basename(filename),
        mime: mime.lookup(ext)
    };
};

var downloadRaw = function(req, res, callbackSuccess) {

    var auth = req.headers.authorization || req.query.authorization;

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

    return request.get({
        url: endpointOntotext + "/catalog/distributions/file",
        headers: {
            'distrib-id': distributionUri,
            Authorization: auth
        }
    })
    .on('error', function(err){
        res.status(500).json({error: err});
    })
};

app.get('/raw', function(req, res) {
    var stream = downloadRaw(req, res);
    if (!stream) return;
    stream.pipe(res);
});

app.get('/original', function(req, res) {
    var stream = downloadRaw(req, res);
    if (!stream) return;
    stream.on('response', function(response){
        if (!response || response.statusCode !== 200) {
            stream.pipe(res);
            return;
        };

        var streamInfos = getAttachmentInfos(response);

        request.post({
            url: endpointGraftwerk+"/evaluate/pipe",
            headers: {
                'transfer-encoding': 'chuncked'
            },
            // json: true,
            formData: {
                pipeline: {
                    value: '(defpipe my-pipe [data-file] (-> (read-dataset data-file)))',
                    options: {
                        filename: 'pipeline.clj',
                        contentType: 'text/plain'
                    }
                },
                data: {
                    value: stream,
                    options: {
                        filename: streamInfos.filename,
                        contentType: streamInfos.mime,
                        knownLength: 10000000000
                    }
                },
                command: req.query.command || 'my-pipe'
            }
        }).on('error', function(err) {
            res.status(500).json({error: err});
        }).pipe(res);

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

    var stream = downloadRaw(req, res);
    if (!stream) return;
    stream.on('response', function(response){
        if (!response || response.statusCode !== 200) {
            stream.pipe(res);
            return;
        };

        var type = req.body.transformationType === "graft" ? "graft" : "pipe";

        var streamInfos = getAttachmentInfos(response);

        request.post({
            // url: endpointOntotext+"/dapaas-services/grafter/transformation/preview",
            url: endpointGraftwerk+"/evaluate/"+type,
            // json: true,
            headers: {
                // "command": req.query.command || "my-pipe",
                // "transformation-type": req.query.transformationType || "pipe",
                // Authorization: auth,
                'transfer-encoding': 'chuncked'
            },
            formData: {
                "data": {
                    value: stream,
                    options: {
                        filename: streamInfos.filename,
                        contentType: streamInfos.mime,
                        knownLength: 10000000000
                    }
                },
                "pipeline": {
                    value: clojure,
                    options: {
                        filename: 'pipeline.clj',
                        contentType: 'text/plain'
                    }
                },
                command: req.query.command || ('my-'+type)
            } 
        }).on('error', function(err) {
            res.status(500).json({error: err});
        }).pipe(res);
    });
});

var downloadErrorText = '<h3>An error has occured.</h3>'
    + '<p><a href="http://project.dapaas.eu/dapaas-contact-us">Please contact us.</a></p>';

app.get('/download', function(req, res) {

    var auth = req.headers.authorization || req.query.authorization;
    if (!auth) {
        res.status(401).json({error: "The authorization header is missing"});
        return;
    }

    var transformationUri = req.query.transformationUri;

    if (!transformationUri) {
        res.status(418).json({error: "The transformation URI parameter is missing"});
        return;
    }

    request.get({
        url: endpointOntotext + "/catalog/transformations/code/clojure",
        headers: {
            'transformation-id': transformationUri,
            Authorization: auth
        }
    }, function(err, response, bodyClojure) {

        if (err || (response && response.statusCode !== 200)) {
            res.status(500).send(downloadErrorText);
            return;
        };

        var dataStream = downloadRaw(req, res);
        if (!dataStream) return;

        var type = req.query.type === "graft" ? "graft" : "pipe";

        dataStream.on('response', function(response) {
            if (!response || response.statusCode !== 200) {
                res.status(500).send(downloadErrorText);
                return;
            };

            var dataStreamInfos = getAttachmentInfos(response);
           
            var resultStream = request.post({
                url: endpointGraftwerk+"/evaluate/"+type,
                headers: {
                    'transfer-encoding': 'chuncked',
                    Accept: type === 'graft' ? 'application/n-triples' : 'text/csv'
                },
                formData: {
                    "data": {
                        value: dataStream,
                        options: {
                            filename: dataStreamInfos.filename,
                            contentType: dataStreamInfos.mime,
                            knownLength: 10000000000
                        }
                    },
                    "pipeline": {
                        value: bodyClojure,
                        options: {
                            filename: 'pipeline.clj',
                            contentType: 'text/plain'
                        }
                    },
                    command: req.query.command || ('my-'+type)
                } 
            });

            resultStream.on('error', function(err) {
                res.status(500).send(downloadErrorText);
            }).on('response', function(response){
                if (!response || response.statusCode !== 200) {
                    res.status(500).send(downloadErrorText);
                    return;
                };

                delete response.headers['content-disposition'];
                delete response.headers['content-type'];
                delete response.headers['server'];

                var filename = dataStreamInfos.name.replace(/[^a-zA-Z0-9_\-]/g,'')
                    +"-processed";

                if (type === 'graft') {
                    res.contentType('application/n-triples');
                    res.setHeader('content-disposition', 'attachment; filename='+filename+'.nt');
                } else {
                    res.contentType('text/csv');
                    res.setHeader('content-disposition', 'attachment; filename='+filename+'.csv');
                }

                resultStream.pipe(res);
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
