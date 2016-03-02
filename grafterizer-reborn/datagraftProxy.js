/**
 * Instead of parsing request, requesting datagraft, parsing the returned data
 * and returning the data, we use a proxy approach. We just foward the request
 * with the correct authentication and some access control.
 *
 * It's faster and simpler.
 */

// jscs:disable requireCamelCaseOrUpperCaseIdentifiers

// Logging component for error and info messages
const logging = require('./logging');

// http-proxy is the component used to create the reverse proxy
const httpProxy = require('http-proxy');

// AgentKeepAlive adds support of keepalive HTTP connections for http-proxy
const AgentKeepAlive = require('agentkeepalive');

// The following settings are related to the keepAlive options,
// the default values are good enough for most of the use cases

// Sets the working socket to timeout after milliseconds of inactivity
const proxyTimeout = parseInt(process.env.PROXY_TIMEOUT) || 60 * 1000;

// Sets the free socket to timeout after milliseconds of inactivity
const keepAliveTimeout = parseInt(process.env.KEEP_ALIVE_TIMEOUT) || 60 * 10000;

// Maximum number of sockets to allow per host
const maxSockets = parseInt(process.env.KEEP_ALIVE_MAX_SOCKETS) || 500;

// Maximum number of sockets to leave open in a free state
const maxFreeSockets = parseInt(process.env.KEEP_ALIVE_MAX_FREE_SOCKETS) || 10;

// Initializing the proxy with the keep alive agent
const proxy = httpProxy.createProxyServer({
  agent: new AgentKeepAlive({
    maxSockets,
    maxFreeSockets,
    keepAliveTimeout,
    timeout: proxyTimeout
  })
});

// Configure the proxied request
proxy.on('proxyReq', function(proxyReq, req, res, options) {
  // We only use the JSON API
  proxyReq.setHeader('Accept', 'application/json');

  try {
    proxyReq.setHeader('Authorization', 'Bearer ' + req.oauthSession.token.access_token);
  } catch (e) {
    logging.error('Unable to get the authorization token from the session cookie store', {
      message: e.message
    });
  }
});

// Foward requests that match only this pattern
const matchUriPattern = /^\/[^\/]+\/(utility_functions|queriable_data_stores|transformations|data_distributions)\/?/;

module.exports = (app, settings) => {
  app.use((req, res, next) => {

    // Skip the requests that are not related to the bridged DataGraft API
    if (!matchUriPattern.test(req.path)) return next();

    // Foward the incomming request to Datagraft
    proxy.web(req, res, {
      target: settings.datagraftUri
    });
  });
};
