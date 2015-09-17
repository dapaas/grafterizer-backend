'use strict';

// Dequeue is a simple implementation of a fifo
var Dequeue = require('dequeue');

var fs = require('fs');

// Q is a promise library that supports the deferred promise coding style
var Q = require('q');

// A cache is a collection of cache entries
// Internally, it uses two structures : a map and a fifo
class Cache {
  constructor(cacheFolder) {
    this.map = new Map();
    this.fifo = new Dequeue();
    this.usedStorage = 0;
  }

  has(key) {
    return this.map.has(key);
  }

  get(key) {
    return this.map.get(key);
  }

  set(key, value) {
    this.fifo.push(value);
    return this.map.set(key, value);
  }

  delete(key) {
    this.map.delete(key);
  }

  clean(maxStorage, cacheFolder) {
    while (this.usedStorage > maxStorage) {
      var entry = this.fifo.shift();
      fs.unlink(cacheFolder + entry.hash);
      this.map.delete(entry.hash);
      this.usedStorage -= entry.length;
    }
  }

  addStreamLength(length) {
    this.usedStorage += length;
  }
}

// A cache entry correspond to a query identified by a hash
// It contains the hash, and some informations such as the header
// and the processing duration
class CacheEntry {
  constructor(hash) {
    this.hash = hash;

    // It is in process by default
    this.processing = true;

    this.statusCode = 0;

    // The startTime is used to inform the client
    // for how long the query is processing
    this.startTime = new Date();

    // This is the deferred object for the promise used
    // when the data is finally processed and saved
    this.deferred = null;

    // Size of the cache entry (in bytes)
    this.length = 0;
  }

  // When the result is finally received from Graftwerk
  finalize(statusCode, contentType, length) {
    this.processing = false;
    this.statusCode = statusCode;
    this.contentType = contentType;
    this.length = length;
    this.endTime = new Date();

    // Trigger the promise callbacks
    if (this.deferred) {
      this.deferred.resolve(statusCode);
      this.deferred = null;
    }
  }

  // Lazy loading for the promise object
  get promise() {
    if (!this.processing) {
      throw 'the promise is only accessible during processing';
    }

    if (!this.deferred) {
      this.deferred = Q.defer();
    }

    return this.deferred.promise;
  }

  // When an error has occured, the cache entry must be rejected
  reject(err) {
    // Reject the promise callbacks
    if (this.deferred) {
      this.deferred.reject(err);
      this.deferred = null;
    }
  }

  // Serialize the Cache entry to a JSON object
  toJSON() {

    var json = {
      processing: this.processing,
      hash: this.hash,
      startTime: this.startTime
    };

    if (!this.processing) {
      json.endTime = this.endTime;
      json.duration = this.endTime - this.startTime;
      json.statusCode = this.statusCode;
    }

    return json;
  }
}

module.exports = {
  Cache,
  CacheEntry
};
