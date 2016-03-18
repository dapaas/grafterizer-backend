/**
 * Grafter-memcache is a server component.
 * It sits between Graftwerk and the client to save Graftwerk
 * requests in memory, and also to prevent HTTP timeouts.
 *
 * It takes the Graftwerk query as input, computes a hash and
 * transfer the query to Graftwerk. The client receives a ticket
 * and can fetch the result in a separate query. If the result is
 * not ready after a specified delay, the client is asked to try
 * again. We chosed this behavior in order to prevent HTTP timeouts
 * that can be triggered by many components (CDNs, browser, proxy…).
 *
 * It's a NodeJS software (tested with NodeJS 4.0.0)
 * Some parts are developed using JavaScript ES6.
 */

'use strict';

// The HTTP module is used to contact Graftwerk
var http = require('http');

// In this implementation, the cache is stored using the filesystem
// It facilitates the implementation and use less memory in the NodeJS
// process.
// If the system supports it, the suggest using a tmpfs filesystem.
var fs = require('fs');

// The cryptographic module is used to compute a
// SHA256 hash from the query, used as a ticket number
var crypto = require('crypto');

// Express is a library providing tools to develop HTTP Apis.
var express = require('express');

// Graftwerk queries must be parsed to generate unique hash,
// as a query may contain random values that are not related to the  inputs.
// BusBoy has been selected because it provides a low-level API with streams.
var Busboy = require('busboy');

// Temp is used to generate temporary file paths.
var temp = require('temp');

// HTTP CORS library
var cors = require('cors');

// This component allows compressing the output, to reduce the result size
// when it is sent to the client
var compression = require('compression');

// Morgan is a HTTP logging component.
var morgan = require('morgan');

// Length-stream is used to compute the size of a streams
var lengthStream = require('length-stream');

// Filesize parser is used to convert size such as 200kb to bytes
var filesizeParser = require('filesize-parser');

// Local dependencies
var cleaning = require('./cleaning');
var sin = require('./model.js');


/**
 * All the following settings are pre-configured, but can be overrided using environnement
 * variables.
 *
 * Example:
 *  export HTTP_PORT=8085
 */

// This is the TCP port where this component is listening
var serverPort = process.env.HTTP_PORT || 8082;

// The maximum waiting delay (in ms) is the maximum duration of a fetching request.
// When this delay ends, a HTTP 204 answer is sent to the client. The client must
// send a new request to fetch the data.
var maximumWaitingDelay = parseInt(process.env.MAXIMUM_WAITING_DELAY) || 15000;

// The cache folder contains the results from Graftwerk.
// Each file is identified by a hash (also known as ticked number)
var cacheFolder = process.env.CACHE_FOLDER || './cache/';

// The reqs folder contains the queries that must be sent to Graftwerk
// They are saved as a file in order to reduce the memory usage by the component.
// If the server contains a lot of memory, this folder can be mounted as a tmpfs
// filesystem.
var reqsFolder = process.env.REQS_FOLDER || './reqs/';

// The max-age header is used by some proxies and caching system. It is sent when
// the data is fetched by a client.
var cacheMaxAgeHeader = parseInt(process.env.MAX_HAGE) || 86400000;

// Graftwerk hostname and ports.
// This component doesn't include loadbalancing. You can however configure a
// loadbalancer and set the loadbalancer hostname here.
var graftwerkHostname = process.env.GRAFTWERK || 'graftwerk';
var graftwerkPort = process.env.GRAFTWERK_PORT || 8080;

// The maximum amount of storage that can be used in the cache
var maxStorage = filesizeParser(process.env.MAX_STORAGE || '2gb');

// We clear the reqs folder. This folder only contains the requests that are currently
// received from the client or sent to Grafter. It is empty most of the time.
cleaning(reqsFolder);

// The cache folder is cleared after every startup because we don't know the previous status
// (http response code and http content type).
cleaning(cacheFolder);

// The cache map has the SHA256 hash as key, and a Cache object as value.
var cache = new sin.Cache();

// Setting-up the express HTTP server
var app = express();

// Compress the output
app.use(compression());

// Add a HTTP logging using Apache syntax
app.use(morgan('short'));

// Enable CORS for every request
app.use(cors(function(req, callback) {
  callback(null, {origin: true, credentials: true});
}));

/**
 * This is the main API method provided by this component.
 *
 * It listens for HTTP POST queries, no matter what the query
 * path is.
 * A query hash is computed. If a result is not present in the cache,
 * the query is forwarded to Graftwerk.
 *
 * In every case, a ticket number (the hash) is sent to the client serialized in JSON.
 *
 * Example output:
 *   {
 *     "hash": "6710eee6e90e696652222f7c3e71241cefacefea3d9dbfc389099ff189ef1da7",
 *     "processing": true,
 *     "startTime": "2015-09-14T12:44:55.377Z"
 *   }
 */
app.use((req, res, next) => {
  if (req.method !== 'POST') {
    // Calling next allows the other methods to proceed
    return next();
  }

  // We start by cleaning the cache
  cache.clean(maxStorage, cacheFolder);

  // Form parsing initialization
  var bus = new Busboy({
    headers: req.headers
  });

  // Hash stream initialization
  // We chosed SHA256 because it has a good quality.
  // The encoding is set to hex, so a string containing
  // the hexadecimal hash is computed.
  var inputHash = crypto.createHash('sha256');
  inputHash.setEncoding('hex');

  // The url is part of the hash because it can be either graft or pipe
  inputHash.write(req.url);

  // the method is always POST in this version, but it may change
  inputHash.write(req.method);

  // When an uploaded file has been detected in the POST query
  bus.on('file', (fieldname, file, filename, encoding, mimetype) => {
    // We use all these informations to compute the hash
    inputHash.write(fieldname);
    inputHash.write(filename);
    inputHash.write(encoding);
    inputHash.write(mimetype);

    // We also use the file content to compute the hash
    // We ask the pipe to be automaticcaly closed because
    // more files or fields can be received after
    file.pipe(inputHash, {
      end: false
    });
  });

  // When a field (key=value store) has beend detected in the POST query
  bus.on('field', (fieldname, val) => {
    // Both the key and the values are used to compute the hash
    inputHash.write(fieldname);
    inputHash.write(val);
  });

  // The query is saved locally in the filesystem
  // We chosed to do that using a filesystem because a query can be huge
  // (few gigabytes), and we prefer to not have such a big memory usage
  // with the nodejs process.
  // However, if the server has a lot of memory, a tmpfs (a RAM filesystem)
  // maybe used, but we leave this choice to the system administrator.
  var tempPath = temp.path({
    dir: reqsFolder
  });

  var writeStream = fs.createWriteStream(tempPath);

  // The hashkey (or the ticket number) declaration
  var hashKey = 'no-hash-key';

  // When the query is finally completely loaded and saved
  writeStream.on('finish', () => {

    // If the cache already contains the result
    // We just need to remove the query (we do not need it)
    // and to send the informations to the client
    if (cache.has(hashKey)) {
      fs.unlink(tempPath);
      res.json(cache.get(hashKey));
      return;
    }

    // If the cache doesn't exist, we need to create a new entry
    var cacheEntry = new sin.CacheEntry(hashKey);
    cache.set(hashKey, cacheEntry);

    // The informations are directly sent to the client and the request
    // is closed. However we job is not done here for this component,
    // we must foward the query to Grafwerk, save the result and
    // trigger the eventuals event listeners
    res.json(cacheEntry);

    // Create the HTTP query to send to Graftwerk
    var request = http.request({
      hostname: graftwerkHostname,
      port: graftwerkPort,
      method: req.method,
      path: req.url,
      headers: req.headers
    }, (response) => {
      // When we receive the response, we can finally remove the query
      fs.unlink(tempPath);

      // The cache location is a path in the cache filesystem
      var cacheLocation = cacheFolder + hashKey;

      // If Graftwerk returned an error the cache must still be saved
      // so the clients can fetch the errors
      // but the lifetime of the cache entry is short so the user can
      // try again after a delay
      if (response.statusCode === 500) {
        setTimeout(() => {
          // The cache is removed from the collection
          cache.delete(hashKey);

          // and the filesystem data is removed as well
          fs.unlink(cacheLocation);
        }, 1000 * 60 * 15); // after 15 minutes
      }

      // We save the response to the filesystem
      var cacheStream = fs.createWriteStream(cacheLocation);

      var cacheStreamLength = 0;

      // When the filesystem has saved the response, we can mark it as processed
      // and trigger the eventual event listeners
      cacheStream.on('finish', () => {
        cacheEntry.finalize(response.statusCode, response.headers['content-type'], cacheStreamLength);
        cache.addStreamLength(cacheStreamLength);

        // We save the content-disposition header for some reasons
        if (response.headers['content-disposition']) {
          cacheEntry.contentDisposition = response.headers['content-disposition'];
        }
      });
      
      response.pipe(lengthStream((length) => {
        cacheStreamLength = length;
      })).pipe(cacheStream);
    });

    // When something goes wrong
    request.on('error', function(err) {
      // We log the problem
      console.log('ERROR: ', err);

      // We cancel the eventual event listeners
      cacheEntry.reject(err);

      // We remove the cache entry
      cache.delete(hashKey);

      // We remove the query file to save memory
      // You can disable this line if you want to keep it for debug purposes
      fs.unlink(tempPath);
    });

    // Read the query from the filesystem...
    var readStream = fs.createReadStream(tempPath);

    // ...and send it to Graftwerk
    readStream.pipe(request);
  });

  // When we have finished the POST query parsing
  bus.on('finish', () => {
    // We close the query file to save it
    // It will trigger the previous event block
    writeStream.end();

    // We compute the hash
    inputHash.end();
    hashKey = inputHash.read();
  });

  // We connect the query to the POST query parsing
  req.pipe(bus);

  // We write the query in a temporary file in the queries folder
  req.pipe(writeStream);
});

// Send a file from a cache entry
// We just use Express tools
var sendFile = (req, res, cacheEntry) => {

  // If the client ask for the status, we send the status
  // instead of the cache content
  if (req.query.status) {
    res.json(cacheEntry);
    return;
  }

  // We send the file with the same status than the one saved
  res.status(cacheEntry.statusCode);

  var headers = {
    'Content-Type': cacheEntry.contentType
  };

  // We put back the content-disposition header if necessary
  if (cacheEntry.contentDisposition) {
    headers['Content-Disposition'] = cacheEntry.contentDisposition;
  }

  // We send the file using express.js
  res.sendFile(cacheEntry.hash, {
    root: cacheFolder,
    maxAge: cacheMaxAgeHeader,
    headers
  }, (err) => {
    if (err) {
      console.log('SendFile error: ', err);

      // We close the client connection
      res.end();
    }
  });
};

/**
 * The client will fetch this endpoint once it received the hash.
 */
app.get('/graftermemcache/:hash', (req, res) => {
  var hashKey = req.params.hash;

  // If the hash is not found in the cache, it's a HTTP 404 not found error
  // This occurs when the client send a wrong hash, or if this component
  // has been restarted after the client received the hash
  // Another possibility is when this component is used behind a loadbalancer
  // and the hash has been sent by another instance. If used behind a loadbalancer,
  // a client should always contact the same instance.
  if (!cache.has(hashKey)) {
    res.status(404).end();
    return;
  }

  var cacheEntry = cache.get(hashKey);

  // If Graftwerk is currently processing the query
  if (cacheEntry.processing) {

    // We get a promise to send the result once the data is received and saved
    var promise = cacheEntry.promise;

    // We however have an internal timeout
    // After waiting the maximum waiting delay, the an empty response is sent to the
    // client. It should interpret it as the necessity to ask again.
    // As explained before, this is done like this to prevent HTTP timeouts that can
    // occurs.
    var timeoutId = setTimeout(function() {
      // Send the empty response with no cache because the status will change
      res.header('Cache-Control', 'no-cache, no-store');
      res.header('Pragma', 'no-cache');
      res.status(204).end();

      // Remove the timeoutID, also used to notify the promise callback that the
      // request has timeout.
      timeoutId = null;

      // Clear variables, it might save memory if a garbage collection occurs before the promise
      // callback. But I am not sure.
      req = null;
      res = null;
    }, maximumWaitingDelay);

    // When the result has been received and saved from Graftwerk
    promise.then(() => {
      // If a timeout hasn't occured yet
      if (timeoutId !== null) {

        // It's time to send the file
        sendFile(req, res, cacheEntry);

        // And to cancel the timeout
        clearTimeout(timeoutId);
      }
    },

    (error) => {
      // Sadly, errors may occur
      res.status(500).json(error);
      clearTimeout(timeoutId);
    });
  } else {
    // If the data is already in the cache and has already been computed
    // we just need to send it
    sendFile(req, res, cacheEntry);
  }

});

// Starting the HTTP server
app.listen(serverPort, () => {
  console.log('Grafter-memcache started on http://localhost:' + serverPort + '/');
  console.log('Graftwerk endpoint: http://' + graftwerkHostname + ':' + graftwerkPort);
});
