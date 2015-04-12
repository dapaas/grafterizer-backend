var request = require('request'),
	unparse = require('babyparse').unparse,
	stringify = require('csv-stringify'),
	edn = require("jsedn");

module.exports = function(app) {

	var Transformation = app.models.Transformation,
		File = app.models.File;

	var endpoint = app.get('graftwerkEndpoint')+"/evaluate/pipe";
	
	var computePipe = function(req, res, format) {
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
									filename: 'pipeline.clj',
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
							command: req.query.command || 'my-pipe'
						}
					}, function(err, response, body) {
						res.status(response.statusCode);
						var ednResult = body;

						if (format === 'edn')
						{
							res.contentType('text/plain');
							res.send(ednResult);
							return;
						} 

						var doc = edn.toJS(edn.parse(ednResult));

						if (format === 'csv') {
							stringify(doc[":rows"], {
								header: true,
								columns: doc[":column-names"]
							},function(err, csv){
								if (err) {
									res.status(500).json(err);
								} else {
									res.contentType('text/csv');
									res.send(csv);
								}
							});
						} else {
							res.json(doc);
						}
					});
				});
			});
		});
	};

	app.get('/pipe/:transformation/:file.edn', function(req, res) {
		computePipe(req, res, 'edn');
	});

	app.get('/pipe/:transformation/:file.csv', function(req, res) {
		computePipe(req, res, 'csv');
	});

	app.get('/pipe/:transformation/:file.json', function(req, res) {
		computePipe(req, res, 'json');
	});
}