var AWS = require('aws-sdk');
var _ = require('lodash');

AWS.config.region = process.env.AWS_REGION || 'eu-west-1';
var instanceImageId = process.env.INSTANCE_IMAGE_ID || 'secret';

var ec2 = new AWS.EC2({
  apiVersion: '2015-04-15'
});

module.exports = function(callback) {
  ec2.describeInstances({
      Filters: [
        {
          Name: 'instance-state-name',
          Values: ['running']
        },
        {
          Name: 'image-id',
          Values: [instanceImageId]
        }
      ]
    }, function(err, data) {
      if (err) {
        console.log(err, err.stack);
        callback([]);
        return;
      }

      var instancesIps = _(_.get(data, 'Reservations'))
        .pluck('Instances').flatten()
        .pluck('PublicIpAddress').value();

      callback(instancesIps);
    });
};
