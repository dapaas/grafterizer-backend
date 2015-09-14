'use strict';

var Q = require('q');

class Cache extends Map {
  // constructor() {}

  /*addEntry(entry) {
      console.log("je mange du chocolat")
      this.entries.set(entry.hash, entry);
  }

  hasEntry(hash) {
      return this.entries.has(hash);
  }

  getEntry(hash) {
      return this.entries.get(hash);
  }*/
}

class CacheEntry {
  constructor(hash) {
    this.hash = hash;
    this.processing = true;
    this.statusCode = 0;
    this.startTime = new Date();
    this.location = './cache/' + hash;
    this.deferred = null;
  }

  finalize(statusCode, contentType) {
    this.processing = false;
    this.statusCode = statusCode;
    this.contentType = contentType;
    this.endTime = new Date();

    if (this.deferred) {
      this.deferred.resolve(statusCode);
      this.deferred = null;
    }
  }

  get promise() {
    if (!this.processing) {
      throw 'the promise is only accessible during processing';
    }

    if (!this.deferred) {
      this.deferred = Q.defer();
    }

    return this.deferred.promise;
  }

  reject(err) {
    if (this.deferred) {
      this.deferred.reject(err);
      this.deferred = null;
    }
  }

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
