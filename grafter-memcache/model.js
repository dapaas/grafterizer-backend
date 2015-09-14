'use strict';

// Q is a promise library that supports the deferred promise coding style
var Q = require('q');

// A cache entry correspond to a query identified by a hash
// It contains t
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
  }

  // When the result is finally received from Graftwerk
  finalize(statusCode, contentType) {
    this.processing = false;
    this.statusCode = statusCode;
    this.contentType = contentType;
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
  CacheEntry
};
