var fs = require('fs');

module.exports = function(path) {
  fs.readdir(path, function(err, files) {
    if (err) return;
    files.forEach(function(file) {
      fs.unlink(path + file);
    });
  });
};
