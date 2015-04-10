var request = require('request'),
	unparse = require('babyparse').unparse,
	stringify = require('csv-stringify');

module.exports = function(app) {

	var Transformation = app.models.Transformation,
		File = app.models.File;

	//app.dataSources['pgminicloud'].automigrate(['datapages', 'transformation', 'file'], function(err) {
	//	console.log(err);
	//});

	var endpoint = app.get('graftwerkEndpoint')+"/evaluate/pipe";

  app.get('/pipe/:transformation/:file.edn', function(req, res) {

  	Transformation.findById(req.params.transformation, function(err, transformation) {
  		if (err) {
  			res.status(404).send("transformation not found");
  			return;
  		}
	  	File.findById(req.params.file, function(err, file) {
	  		if (err) {
	  			res.status(404).send("file not found");
	  			return;
	  		}

	  		/*var csv = unparse(file.content.data, {
	  			skipEmptyLines: true
	  		});*/

	  		stringify(file.content.data, {
	  			header: true,
	  			columns: file.content.meta.fields
	  		},function(err, csv){

		  		console.log(csv);

	  		  	request.post({
			  		url: endpoint,
			  		json: true,
			  		formData: {
			  			/*pipeline: transformation.clojure,
			  			data: csv,*/
			  			pipeline: {
			  				value: transformation.clojure,
			  				options: {
			  					filename: 'thisisauselessfilenuma.clj',
			  					contentType: 'text/plain'
			  				}
			  			},
			  			data: {
			  				value: csv,
			  				options: {
			  					filename: 'data.csv',
			  					contentType: 'text/csv'
			  				}
			  			},
			  			command: 'my-pipe'
			  		}
			  	}, function(err, response, body) {
			  		res.status(response.statusCode).send(body);
			  	});
	  		  });
	  	});
  	});

  });
}