'use strict';

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
  }

  finalize(statusCode, contentType) {
    this.processing = false;
    this.statusCode = statusCode;
    this.contentType = contentType;
    this.endTime = new Date();
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
