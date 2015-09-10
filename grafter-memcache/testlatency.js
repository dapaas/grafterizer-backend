var express = require('express');
var http = require('http');
var fs = require('fs');
var crypto = require('crypto');
var Busboy = require('busboy');
var temp = require('temp');
var cleaning = require('./cleaning');

cleaning('./reqs/');
cleaning('./cache/');

var app = express();

app.use(function(req, res, next) {
  if (req.method !== 'POST') {
    return next();
  }

  var bus = new Busboy({
    headers: req.headers
  });

  var inputHash = crypto.createHash('sha256');
  inputHash.setEncoding('hex');

  bus.on('file', function(fieldname, file, filename, encoding, mimetype) {
    inputHash.write(fieldname);
    inputHash.write(filename);
    inputHash.write(encoding);
    inputHash.write(mimetype);
    file.pipe(inputHash);
  });

  bus.on('field', function(fieldname, val) {
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

  writeStream.on('finish', function() {

    var cacheLocation = 'cache/' + hashKey;

    var readCacheStream = fs.createReadStream(cacheLocation);
    readCacheStream.on('error', function() {
      console.log("IT?S NOT NDNNST NDNTCANASRUUHHUHUHFORM THE CACHE")
      var readStream = fs.createReadStream(tempPath);

      var lapin = http.request({
        hostname: '54.77.10.112',
        port: 8080,
        method: req.method,
        path: req.url,
        headers: req.headers,
      }, function(response) {
        res.status(response.statusCode);
        console.log("salut")
        response.pipe(res);

        var cacheStream = fs.createWriteStream(cacheLocation);
        response.pipe(cacheStream);
      });

      readStream.pipe(lapin);

    })
    .on('open', function() {
      console.log("IT?S FORM THE CACHE")
      readCacheStream.pipe(res);
    });

  });

  bus.on('finish', function() {
    writeStream.end();

    inputHash.end();
    hashKey = inputHash.read();
    console.log('bonsoir', hashKey);

  });

  req.pipe(bus);
  req.pipe(writeStream);
});

var serverPort = 8082;
app.listen(serverPort, function() {
  console.log('LoadBalancer started on http://localhost:' + serverPort + '/');
});
