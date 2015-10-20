var express = require('express');
var _ = require('lodash');
var httpProxy = require('http-proxy');
var getAWSIPs = require('./getAWSIPs.js');
var Agent = require('agentkeepalive');
var raven = require('raven');

var serverPort = process.env.HTTP_PORT || 8081;
var proxyTimeout = parseInt(process.env.PROXY_TIMEOUT) || 180000;
var keepAliveTimeout = parseInt(process.env.KEEP_ALIVE_TIMEOUT) || 30000;
var maxSockets = parseInt(process.env.MAX_SOCKETS) || 100;
var maxFreeSockets = parseInt(process.env.MAX_FREE_SOCKETS) || 10;
var updateTargetsInterval = parseInt(process.env.UPDATE_TARGETS_INTERVAL) || 60000;
var latencyAverageWindow = parseInt(process.env.LATENCY_AVERAGE_WINDOW) || 30000;
var latencyGroupBySize = parseInt(process.env.LATENCY_GROUPBY_SIZE) || 100;

var log = new raven.Client(process.env.SENTRY);

var graftwerkTargets = [];

var updateGraftwerkTargets = _.throttle(function() {
  console.log('Fetching graftwerk nodes');
  getAWSIPs(function(ips) {
    graftwerkTargets = ips;
  });
}, 5000);

updateGraftwerkTargets();

setInterval(updateGraftwerkTargets, updateTargetsInterval);

var proxy = httpProxy.createProxyServer({
  agent: new Agent({
    maxSockets: maxSockets,
    maxFreeSockets: maxFreeSockets,
    timeout: proxyTimeout,
    keepAliveTimeout: keepAliveTimeout
  })
});

var printLatency = function(req) {
  var now = Date.now();
  var latency = now - req.loadBalancerStartTime;
  console.log('Latency ' + req.loadBalancerTarget + ': ' + latency);
};

proxy.on('error', function(err, req, res) {
  console.log('The ' + req.loadBalancerTarget + ' graftwerk node returned an error');
  printLatency(req);
  req.nbDispatchRetry = ++req.nbDispatchRetry || 1;

  if (req.nbDispatchRetry > 6) {
    console.log('6 tentatives later, no one works. We give up');
    res.status(503).json({
      error: 'Unable to contact a Grafter server'
    });
    log.captureMessage('6 tentatives later, no ones work. We give up.');
    return;
  }

  processRequest(req, res);
});

proxy.on('proxyRes', function(lol, req) {
  printLatency(req);
});

var currentTargetIndex = 0;
var selectTarget = function() {
  if (currentTargetIndex >= graftwerkTargets.length) {
    currentTargetIndex = 0;
  }

  return graftwerkTargets[currentTargetIndex++];
};

var processRequest = function(req, res) {
  // var target = _.sample(graftwerkTargets);
  var target = selectTarget();

  if (!target) {
    res.status(503).json({
      error: 'No Graftwerk server is available'
    });
    log.captureMessage('No Graftwerk server is available');
    return;
  }

  req.loadBalancerTarget = target;
  req.loadBalancerStartTime = Date.now();

  proxy.web(req, res, {
    target: {
      host: target,
      port: 8080
    }
  });
};

var app = express();
app.use(processRequest);

app.listen(serverPort, function() {
  console.log('LoadBalancer started on http://localhost:' + serverPort + '/');
});
