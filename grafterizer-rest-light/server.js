'use strict';

var express = require('express');
var compression = require('compression');
var morgan = require('morgan');
var request = require('request');
var cors = require('cors');
var mime = require('mime');
var contentDisposition = require('content-disposition');
var path = require('path');
var bodyParser = require('body-parser');
var raven = require('raven');
var escape = require('escape-html');
var concat = require('concat-stream');

if (process.env.DEBUG) {
  require('request-debug')(request);
}

var endpointOntotext = process.env.ONTOTEXT || 'http://ontotext:8080';
var endpointGraftwerk = process.env.GRAFTWERK || 'http://graftwerk:8080';
/*var maxPreviewSize = process.env.MAX_PREVIEW_SIZE ?
  filesizeParser(process.env.MAX_PREVIEW_SIZE) : filesizeParser('10MiB');*/

var app = express();

app.use(compression());
app.use(morgan('short'));
app.use(cors());

var log = new raven.Client(process.env.SENTRY);

var jsonParser = bodyParser.json();

// To check the API status
app.get('/', function(req, res) {
  res.send('Hei');
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

  var disposition;
  try {
    disposition = contentDisposition.parse(response.headers['content-disposition']);
  } catch (e) {
    return defaultInfos;
  }

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

var downloadRaw = function(req, res) {

  var auth = req.headers.authorization || req.query.authorization;

  if (!auth) {
    res.status(401).json({
      error: 'The authorization header is missing'
    });
    return;
  }

  var distributionUri = req.query.distributionUri;

  if (!distributionUri) {
    if (req.body && req.body.distributionUri) {
      distributionUri = req.body.distributionUri;
    } else {
      res.status(418).json({
        error: 'The distribution URI parameter is missing'
      });
      return;
    }
  }

  return request.get({
    url: endpointOntotext + '/catalog/distributions/file',
    headers: {
      'distrib-id': distributionUri,
      Authorization: auth
    }
  }).on('error', function(err) {
    res.status(500).json({
      error: err
    });
    log.captureMessage('Unable to download the raw transformation', {
      extra: {
        error: err
      }
    });
  });
};

app.get('/raw', function(req, res) {
  var stream = downloadRaw(req, res);
  if (!stream) return;
  stream.pipe(res);
});

app.get('/original', function(req, res) {
  var stream = downloadRaw(req, res);
  if (!stream) return;
  stream.on('response', function(response) {
    if (!response || response.statusCode !== 200) {
      stream.pipe(res);
      return;
    }

    var streamInfos = getAttachmentInfos(response);

    var formData = {
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
    };

    if (typeof req.query.page !== undefined) {
      formData.page = parseInt(req.query.page) || 0;
    }

    if (req.query.pageSize) {
      formData['page-size'] = parseInt(req.query.pageSize) || 50;
    }

    request.post({
      url: endpointGraftwerk + '/evaluate/pipe',
      headers: {
        'transfer-encoding': 'chuncked'
      },
      formData: formData
    }).on('error', function(err) {
      res.status(500).json({
        error: err
      });
      log.captureMessage('Unable to transform the file using the original transformation', {
        extra: {
          error: err
        }
      });
    }).pipe(res);

  });
});

app.post('/preview', jsonParser, function(req, res) {

  var auth = req.headers.authorization;
  if (!auth) {
    res.status(401).json({
      error: 'The authorization header is missing'
    });
  }

  if (!req.body || !req.body.clojure) {
    res.status(418).json({
      error: 'The clojure transformation code is missing'
    });
    return;
  }

  var clojure = req.body.clojure;

  var stream = downloadRaw(req, res);
  if (!stream) return;
  stream.on('response', function(response) {
    if (!response || response.statusCode !== 200) {
      stream.pipe(res);
      return;
    }

    var type = req.body.transformationType === 'graft' ? 'graft' : 'pipe';

    var streamInfos = getAttachmentInfos(response);

    var formData = {
      data: {
        value: stream,
        options: {
          filename: streamInfos.filename,
          contentType: streamInfos.mime,
          knownLength: 10000000000
        }
      },
      pipeline: {
        value: clojure,
        options: {
          filename: 'pipeline.clj',
          contentType: 'text/plain'
        }
      },
      command: req.query.command || ('my-' + type)
    };

    if (typeof req.body.page !== undefined) {
      formData.page = parseInt(req.body.page) || 0;
    }

    if (req.body.pageSize) {
      formData['page-size'] = parseInt(req.body.pageSize) || 50;
    }

    request.post({
      url: endpointGraftwerk + '/evaluate/' + type,
      headers: {
        'transfer-encoding': 'chuncked'
      },
      formData: formData
    }).on('error', function(err) {
      res.status(500).json({
        error: err
      });
      log.captureMessage('Unable to preview the file', {
        extra: {
          error: err
        }
      });
    }).pipe(res);
  });
});

var downloadErrorText = '<h3>An error has occured.</h3>' + '<p><code></pre>{{OUTPUT}}</pre></code></p>' +
  '<p><a href="http://project.dapaas.eu/dapaas-contact-us">Please contact us.</a></p>';

app.get('/download', function(req, res) {

  var showDownloadError = function(status, message) {
    res.status(status);
    if (!req.query.raw) {
      res.send(downloadErrorText.replace('{{OUTPUT}}', escape(message)));
    } else {
      res.json({
        error: message
      });
    }
  };

  var auth = req.headers.authorization || req.query.authorization;
  if (!auth) {
    showDownloadError(401, 'The authorization header is missing');
    return;
  }

  var transformationUri = req.query.transformationUri;

  if (!transformationUri) {
    showDownloadError(418, 'The transformation URI parameter is missing');
    return;
  }

  request.get({
    url: endpointOntotext + '/catalog/transformations/code/clojure',
    headers: {
      'transformation-id': transformationUri,
      Authorization: auth
    }
  }, function(err, response, bodyClojure) {

    if (err || (response && response.statusCode !== 200)) {
      showDownloadError(500, 'Unable to load the clojure code');
      log.captureMessage('Unable to load the clojure code', {
        extra: {
          error: err
        }
      });
      return;
    }

    var dataStream = downloadRaw(req, res);
    if (!dataStream) return;

    var type = req.query.type === 'graft' ? 'graft' : 'pipe';

    dataStream.on('response', function(response) {
      if (!response || response.statusCode !== 200) {
        showDownloadError(500, 'The raw data is invalid');
        log.captureMessage('The returned raw data is invalid', {
          extra: {
            response: response ? response.statusCode : 'response empty'
          }
        });
        return;
      }

      var dataStreamInfos = getAttachmentInfos(response);

      var resultStream = request.post({
        url: endpointGraftwerk + '/evaluate/' + type,
        headers: {
          'transfer-encoding': 'chuncked',
          Accept: type === 'graft' ? 'application/n-triples' : 'text/csv'
        },
        formData: {
          data: {
            value: dataStream,
            options: {
              filename: dataStreamInfos.filename,
              contentType: dataStreamInfos.mime,
              knownLength: 10000000000
            }
          },
          pipeline: {
            value: bodyClojure,
            options: {
              filename: 'pipeline.clj',
              contentType: 'text/plain'
            }
          },
          command: req.query.command || ('my-' + type)
        }
      });

      resultStream.on('error', function(err) {
        showDownloadError(500, 'Unable to execute the transformation');
        log.captureMessage('Unable to execute the transformation', {
          extra: {
            error: err
          }
        });
        return;
      }).on('response', function(response) {

        if (!response || response.statusCode !== 200) {
          var outputError = concat(function(graftwerkOutput) {
            showDownloadError(500, graftwerkOutput);
          });

          resultStream.pipe(outputError);

          log.captureMessage('The transformed data is invalid', {
            extra: {
              response: response ? response.statusCode : 'response empty'
            }
          });
          return;
        }

        delete response.headers['content-disposition'];
        delete response.headers['content-type'];
        delete response.headers.server;

        var filename = dataStreamInfos.name.replace(/[^a-zA-Z0-9_\-]/g, '') + '-processed';

        if (type === 'graft') {
          res.contentType('application/n-triples');
          res.setHeader('content-disposition', 'attachment; filename=' + filename + '.nt');
        } else {
          res.contentType('text/csv');
          res.setHeader('content-disposition', 'attachment; filename=' + filename + '.csv');
        }

        resultStream.pipe(res);
      });

    });
  });
});

var serverPort = process.env.HTTP_PORT || 8080;
app.listen(serverPort, function() {
  console.log('Server started on http://localhost:' + serverPort + '/');
  console.log('Ontotext endpoint: ' + endpointOntotext);
  console.log('Graftwerk endpoint: ' + endpointGraftwerk);
});
