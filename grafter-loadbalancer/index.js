var express = require('express');
var _ = require('lodash');
var httpProxy = require('http-proxy');
var getAWSIPs = require('./getAWSIPs.js');
var Agent = require('agentkeepalive');

var serverPort = process.env.HTTP_PORT || 8081;
var proxyTimeout = parseInt(process.env.PROXY_TIMEOUT) || 180000;
var keepAliveTimeout = parseInt(process.env.KEEP_ALIVE_TIMEOUT) || 30000;
var maxSockets = parseInt(process.env.MAX_SOCKETS) || 100;
var maxFreeSockets = parseInt(process.env.MAX_FREE_SOCKETS) || 10;
var updateTargetsInterval = parseInt(process.env.UPDATE_TARGETS_INTERVAL) || 90000;
var latencyAverageWindow = parseInt(process.env.LATENCY_AVERAGE_WINDOW) || 30000;
var latencyGroupBySize = parseInt(process.env.LATENCY_GROUPBY_SIZE) || 100;

var graftwerkTargets = {};

var updateGraftwerkTargets = _.throttle(function() {
  console.log('Fetching graftwerk nodes');

  getAWSIPs(function(ips) {
    _.each(ips, function(ip) {
      if (!graftwerkTargets.hasOwnProperty(ip)) {
        console.log('New node added: ' + ip);
        graftwerkTargets[ip] = {
          ip: ip,
          latencies: []
        };
      }
    });
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

proxy.on('error', function(err, req, res) {
  delete graftwerkTargets[req.loadBalancerTarget.ip];
  console.log('The ' + req.loadBalancerTarget.ip + ' graftwerk node has been removed');

  req.nbDispatchRetry = ++req.nbDispatchRetry || 1;

  if (req.nbDispatchRetry > 3) {
    console.log('3 tentatives later, no ones works. We give up');
    res.status(503).json({
      error: 'No Graftwerk server is available'
    });
    return;
  }

  processRequest(req, res);
});

proxy.on('proxyRes', function(lol, req) {
  var now = Date.now();
  var latency = now - req.loadBalancerStartTime;
  console.log('Latency: ' + latency);
  req.loadBalancerTarget.latencies.push({
    time: now,
    latency: latency
  });
});

// This might be the slowest method I ever wrote <3
var selectTarget = function() {
  // Remove the outdated latencies
  var now = Date.now();
  _.each(graftwerkTargets, function(target) {
    _.remove(target.latencies, function(latency) {
      return (now - latency.time) > latencyAverageWindow;
    });
  });

  var latenciesGroups = _.groupBy(graftwerkTargets, function(target) {
    if (target.latencies.length === 0) return '0';

    var l = Math.floor((_.reduce(_.pluck(target.latencies, 'latency'), function(total, latency) {
      return total + latency;
    }) / target.latencies.length) / latencyGroupBySize);

    return l;
  });

  var lowestLatenciesGroup = _(latenciesGroups).keys().sortBy(function(k) {
    return parseInt(k);
  }).first();

  return _.sample(latenciesGroups[lowestLatenciesGroup]);

};

var processRequest = function(req, res) {
  var target = selectTarget();

  if (!target) {
    res.status(503).json({
      error: 'No Graftwerk server is available'
    });
    return;
  }

  req.loadBalancerTarget = target;
  req.loadBalancerStartTime = Date.now();

  proxy.web(req, res, {
    target: {
      host: target.ip,
      port: 8080
    }
  });
};

var app = express();
app.use(processRequest);

app.listen(serverPort, function() {
  console.log('LoadBalancer started on http://localhost:' + serverPort + '/');
});
