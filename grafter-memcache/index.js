'use strict';

var express = require('express');
var http = require('http');
var fs = require('fs');
var crypto = require('crypto');
var Busboy = require('busboy');
var temp = require('temp');
var cleaning = require('./cleaning');
var sin = require('./model.js');
var cors = require('cors');
var compression = require('compression');
var morgan = require('morgan');

var serverPort = process.env.HTTP_PORT || 8082;
var waitingDelay = parseInt(process.env.WAITING_DELAY) || 15000;
var cacheFolder = process.env.CACHE_FOLDER || './cache/';
var reqsFolder = process.env.REQS_FOLDER || './reqs/';
var cacheMaxAgeHeader = parseInt(process.env.MAX_HAGE) || 86400000;
var graftwerkHostname = process.env.GRAFTWERK || '54.77.10.112';
var graftwerkPort = process.env.GRAFTWERK_PORT || 8080;

cleaning(reqsFolder);
cleaning(cacheFolder);

var cache = new sin.Cache();

var app = express();

app.use(compression());
app.use(morgan('short'));
app.use(cors());

app.use((req, res, next) => {
  if (req.method !== 'POST') {
    return next();
  }

  var bus = new Busboy({
    headers: req.headers
  });

  var inputHash = crypto.createHash('sha256');
  inputHash.setEncoding('hex');

  bus.on('file', (fieldname, file, filename, encoding, mimetype) => {
    inputHash.write(fieldname);
    inputHash.write(filename);
    inputHash.write(encoding);
    inputHash.write(mimetype);
    file.pipe(inputHash, {
      end: false
    });
  });

  bus.on('field', (fieldname, val) => {
    inputHash.write(fieldname);
    inputHash.write(val);
  });

  var tempPath = temp.path({
    dir: reqsFolder
  });

  var writeStream = fs.createWriteStream(tempPath);

  var hashKey = 'no-hash-key';

  writeStream.on('finish', () => {

    if (cache.has(hashKey)) {
      fs.unlink(tempPath);
      res.json(cache.get(hashKey));
      return;
    }

    var cacheEntry = new sin.CacheEntry(hashKey);
    cache.set(hashKey, cacheEntry);

    res.json(cacheEntry);

    var cacheLocation = cacheEntry.location;
    var readCacheStream = fs.createReadStream(cacheLocation);
    readCacheStream.on('error', () => {
      var readStream = fs.createReadStream(tempPath);

      var request = http.request({
        hostname: graftwerkHostname,
        port: graftwerkPort,
        method: req.method,
        path: req.url,
        headers: req.headers
      }, (response) => {
        fs.unlink(tempPath);

        var cacheStream = fs.createWriteStream(cacheLocation);
        response.pipe(cacheStream);
        cacheStream.on('finish', () => {
          cacheEntry.finalize(response.statusCode, response.headers['content-type']);
        });
      });

      request.on('error', function(err) {
        console.log('ERROR: ', err);
        cacheEntry.reject(err);
        cache.delete(hashKey);
      });

      readStream.pipe(request);
    })

    .on('open', () => {
      fs.unlink(tempPath);
    });
  });

  bus.on('finish', () => {
    writeStream.end();

    inputHash.end();
    hashKey = inputHash.read();
    console.log('computed hash: ', hashKey);

  });

  req.pipe(bus);
  req.pipe(writeStream);
});

var sendFile = (res, cacheEntry) => {
  res.sendFile(cacheEntry.hash, {
    root: cacheFolder,
    maxAge: cacheMaxAgeHeader,
    headers: {
      'Content-Type': cacheEntry.contentType
    }
  }, (err) => {
    if (err) {
      console.log('SendFile error: ', err);
      res.status(err.status || 500).end();
    }
  });
};

app.get('/graftermemcache/:hash', (req, res) => {
  var hashKey = req.params.hash;
  if (!cache.has(hashKey)) {
    res.status(404).end();
    return;
  }

  var cacheEntry = cache.get(hashKey);

  if (cacheEntry.processing) {
    var promise = cacheEntry.promise;

    var timeoutId = setTimeout(function() {
      res.status(204).end();
      timeoutId = null;
      res = null;
    }, waitingDelay);

    promise.then(() => {
      if (timeoutId !== null) {
        sendFile(res, cacheEntry);
        clearTimeout(timeoutId);
      }
    },

    (error) => {
      res.status(500).json(error);
    });
  } else {
    sendFile(res, cacheEntry);
  }

});

app.listen(serverPort, () => {
  console.log('LoadBalancer started on http://localhost:' + serverPort + '/');
});
