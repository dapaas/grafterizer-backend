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

cleaning('./reqs/');
cleaning('./cache/');

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
    file.pipe(inputHash);
  });

  bus.on('field', (fieldname, val) => {
    inputHash.write(fieldname);
    inputHash.write(val);
  });

  var tempPath = temp.path({
    dir: './reqs'
  });

  var tempPath = temp.path({
    dir: './reqs'
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
        console.log("IT?S NOT NDNNST NDNTCANASRUUHHUHUHFORM THE CACHE")
        var readStream = fs.createReadStream(tempPath);

        var lapin = http.request({
          hostname: '54.77.10.112',
          port: 8080,
          method: req.method,
          path: req.url,
          headers: req.headers,
        }, (response) => {
          fs.unlink(tempPath);
          // res.status(response.statusCode);
          console.log("salut")
            // response.pipe(res);

          var cacheStream = fs.createWriteStream(cacheLocation);
          response.pipe(cacheStream);
          cacheStream.on('finish', () => {
            cacheEntry.finalize(response.statusCode, response.headers['content-type']);
          });
        });

        lapin.on('error', function(err) {
          console.log("ceci est une erreur", err)
          cache.delete(hashKey);
        });

        readStream.pipe(lapin);

      })
      .on('open', () => {
        console.log("IT?S FORM THE CACHE")
        fs.unlink(tempPath);
        // readCacheStream.pipe(res);
      });
  });

  bus.on('finish', () => {
    writeStream.end();

    inputHash.end();
    hashKey = inputHash.read();
    console.log('bonsoir', hashKey);

  });

  req.pipe(bus);
  req.pipe(writeStream);
});

var sendFile = (res, cacheEntry) => {
  res.sendFile(cacheEntry.hash, {
    root: './cache/',
    maxAge: 86400000,
    headers: {
      'Content-Type': cacheEntry.contentType
    }
  }, (err) => {
    if (err) {
      // todo error
      res.status(err.status).end();
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
    var cptTries = 0;
    var intervalId = setInterval(() => {
      console.log("check processing")
      if (!cacheEntry.processing) {
      console.log("cool")
        sendFile(res, cacheEntry);
        clearInterval(intervalId);
      } else if (++cptTries > 10) {
      console.log("give up")
        res.json(cacheEntry.toJSON());
        clearInterval(intervalId);
      }
    },
    
    100);
    return;
  }

  sendFile(res, cacheEntry);

});

var serverPort = 8082;
app.listen(serverPort, () => {
  console.log('LoadBalancer started on http://localhost:' + serverPort + '/');
});
