/**
 * Computing component.
 *
 * Or the glue between Grafterizer, Graftwerk and DataGraft.
 */

'use strict';

// jscs:disable requireCamelCaseOrUpperCaseIdentifiers

// Logging component for error and info messages
const logging = require('./logging');

// Request is a library to easely create HTTP requests
const request = require('request');

// BodyParser is used to parse JSON in the body of HTTP requests
const bodyParser = require('body-parser');

// Path is used to extract information from a file path
const path = require('path');

// Mime is used to get the mimetype from a file extension
const mime = require('mime');

// Parse content-disposition headers
const contentDisposition = require('content-disposition');

// Initializing the jsonParser
const jsonParser = bodyParser.json();

// The .nt mimefile is not defined by default
mime.define({
  'application/n-triples': ['nt']
});

// Return the authorization token from the request
// The authorazation token must exist and is probably already
// checked by the authentication middleware
const getAuthorization = (req) => {
  try {
    return 'Bearer ' + req.oauthSession.token.access_token;
  } catch (e) {
    logging.error('Unable to get the authorization token from the session cookie store');
  }
};

// Returns information about the file, using the content-disposition header
// Also returns default values as a failback (in CSV)
const getAttachmentInfos = (response) => {
  const defaultInfos = {
    type: 'csv',
    name: 'output',
    filename: 'output.csv',
    mime: 'text/csv'
  };

  // If no headers are present or no content-disposition, the default informations
  // are returned
  if (!response.headers || !response.headers['content-disposition']) {
    return defaultInfos;
  }

  // Parse the content disposition header, fallback to the default informations
  // if it fails
  let disposition;
  try {
    disposition = contentDisposition.parse(response.headers['content-disposition']);
  } catch (e) {
    return defaultInfos;
  }

  // If the filename is not present int he content-disposition header,
  // returns the default information
  if (!disposition.parameters || !disposition.parameters.filename) {
    return defaultInfos;
  }

  // Compute the required informations from the parsed content-disposition header
  const filename = disposition.parameters.filename;
  const ext = path.extname(filename);

  return {
    type: ext.slice(1),
    name: path.basename(filename, ext),
    filename: path.basename(filename),
    mime: mime.lookup(ext)
  };
};

module.exports = (app, settings) => {

  // Return a request to download the raw distribution
  // The stream can be transmitted directly to the client
  // or forwarded to Grafterizer
  const downloadRaw = (req, res) => {

    // Loading the distribution id parameter
    // from the query paramaters or the post body
    const distribution = req.params.distribution || req.query.distribution || (req.body && req.body.distribution);

    // If somehow the distribution ID is not provided,
    // it's better to stop here
    if (!distribution) {
      res.status(400).json({
        error: 'The distribution ID parameter is missing'
      });
      return;
    }

    // DataGraft query asking the attached file
    return request.get({
      // /attachement DataGraft's method redirects to the URL of the attachment
      url: settings.datagraftUri + '/myassets/data_distributions/' +
        encodeURIComponent(distribution) + '/attachment',
      headers: {
        // You need a valid authorization of course
        Authorization: getAuthorization(req)
      }
    }).on('error', function(err) {
      // If the headers are already sent, it probably means the server has started to
      // provide a message and it's better to just keep the same message instead of
      // crashing trying to send already sent headers
      if (!res.headersSent) {
        res.status(500).json({
          error: err
        });
      }

      // We can still log the problem though
      log.captureMessage('Unable to download the raw data distribution', {
        extra: {
          error: err
        }
      });
    });
  };

  // Execute the transformation using Graftwerk
  // The important thing to notice is that the data_distribution is
  // directly transferred from DataGraft to Graftwerk
  // This server never has the whole file in memory. It is only
  // working on streams and buffers. The advantage is that it requires
  // less memory, and it is faster as the file transfers are reduced.
  // The cons are a sligthly more complex codebase and a small hack
  // (see the warning)
  const executeTransformation = (req, res, clojure, type) => {
    const stream = downloadRaw(req, res);
    if (!stream) return;

    // When DataGraft has returned an answer
    stream.on('response', (response) => {
      // If the requests has failed, the request is just transferred to the client
      // so it can parse or display the DataGraft error
      if (!response || response.statusCode !== 200) {
        stream.pipe(res);
        return;
      }

      // Load informations about the raw file
      const streamInfos = getAttachmentInfos(response);

      // Graftwerk Request
      const formData = {
        pipeline: {
          value: clojure,

          // Graftwerk requires a Clojure transformation file
          // so we create a virtual one
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

            // /!\ BEWARE OF THE WILD CONSTANT /!\
            // This is a hack to send the file and the pipeline in a streaming mode
            // The value just have to be very big so the file is not cut.
            // We cannot compute the real length of the file because we don't know
            // it before we start streaming this request.
            // It seems that Graftwerk works fine using this hack, but it might
            // change in the future.
            knownLength: 10000000000
          }
        },
        command: req.query.command || ('my-' + type)
      };

      // If a specific page is required, it is transmitted to Graftwerk
      if (typeof req.query.page !== undefined) {
        formData.page = parseInt(req.query.page) || 0;
      }

      // The page size can also be configured
      if (req.query.pageSize) {
        formData['page-size'] = parseInt(req.query.pageSize) || 50;
      }

      // The cache is not enabled by default, but it is recommended
      // for long and slow queries as the client HTTP timeout may occur
      // long before Graftwerk returns the result.

      const endpoint = (req.query.useCache || req.body.useCache) ?
        settings.graftwerkCacheUri : settings.graftwerkUri;

      // Querying Graftwerk
      request.post({
        url: endpoint + '/evaluate/pipe',
        headers: {
          // /!\ This is mandatory to be able to send the file in a streaming mode
          'transfer-encoding': 'chuncked'
        },
        formData: formData
      }).on('error', (err) => {

        // If an error occurs and we havn't received headers,
        // it means we should show the error message
        if (!res.headersSent) {
          res.status(500).json({
            error: err
          });
        }

        // Otherwise we can just log it
        log.captureMessage('Unable to transform the file using the original transformation', {
          extra: {
            error: err
          }
        });
      }).pipe(res);
    });
  };

  // Download the raw distribution file content
  // Graftwerk is not involved in the process
  app.get('/preview_raw/:distribution', (req, res) => {
    const stream = downloadRaw(req, res);
    if (stream) stream.pipe(res);
  });

  // Transfrom the distribution with an empty transformation
  // to parse the file and show the original content.
  // Graftwerk is involved in the process, to parse the file
  app.get('/preview_original/:distribution', (req, res) => {
    executeTransformation(req, res,

      // This transformation is very simple and is just used to parse the file
      // using Graftwerk
      '(defpipe my-pipe [data-file] (-> (read-dataset data-file)))',
      'pipe');
  });

  // Transformation previewing
  // The data distribution is loaded from DataGraft and previewed using Graftwerk
  // The transformation code is sent by the client in the HTTP request body
  // The posted document should be formatted using JSON (and not the often default form-data)
  app.post('/preview/:distribution', jsonParser, (req, res) => {

    // Loading the clojure code from the request body
    const clojure = req.body && req.body.clojure;

    // If the clojure code is missing, the request is aborted
    if (!clojure) {
      res.status(400).json({
        error: 'The clojure transformation code is missing'
      });
      return;
    }

    // If the client tries to send a number, an array or whatever
    // it's probably better to abort the request now
    if (typeof req.body.clojure !== 'string') {
      res.status(400).json({
        error: 'The clojure transformation code is not a string'
      });
      return;
    }

    // Load the transformation type from the request body
    const type = req.body.transformationType === 'graft' ? 'graft' : 'pipe';

    executeTransformation(req, res, clojure, type);
  });
};
