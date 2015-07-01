var request = require('request'),
	unparse = require('babyparse').unparse,
	stringify = require('csv-stringify'),
	edn = require("jsedn"),
	cors = require("cors");

module.exports = function(app) {

	var Transformation = app.models.Transformation,
		File = app.models.File;

	var endpoint = app.get('graftwerkEndpoint')+"/evaluate/pipe",
		endpointOntotext = app.get('ontotextEndpoint');
	
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

	app.get('/pipe/:transformation/:file.edn', cors(), function(req, res) {
		computePipe(req, res, 'edn');
	});

	app.get('/pipe/:transformation/:file.csv', cors(), function(req, res) {
		computePipe(req, res, 'csv');
	});

	app.get('/pipe/:transformation/:file.json', cors(), function(req, res) {
		computePipe(req, res, 'json');
	});

	app.get('/poney', cors(), function(req, res) {
		var distributionUri = req.query.distributionUri;
		if (!distributionUri) {
			res.status(418).json({error: "The distribution URI parameter is missing"});
			return;
		}
		request.get({
			url: endpointOntotext + "/catalog/distributions/file",
			headers: {
				'distrib-id': distributionUri
			}
		}, function(err, response, body) {
			res.status(response.statusCode);

			res.contentType('text/plain');
			res.send(body);
			return;
		});
	});

	app.get('/vache', cors(), function(req, res) {
		var distributionUri = req.query.distributionUri;
		if (!distributionUri) {
			res.status(418).json({error: "The distribution URI parameter is missing"});
			return;
		}
		request.get({
			url: endpointOntotext + "/catalog/distributions/file",
			headers: {
				'distrib-id': distributionUri
			}
		}, function(err, response, body) {
			request.post({
				url: endpoint,
				json: true,
				formData: {
					/*pipeline: transformation.clojure,
					data: csv,*/
					pipeline: {
						value: /*'(defn ->integer\n'+
'  "An example transformation function that converts a string to an integer"\n'+
'  [s]\n'+
'  (Integer/parseInt s))\n'+
'\n'+
'(def base-domain (prefixer "http://my-domain.com"))\n'+
'\n'+
'(def base-graph (prefixer (base-domain "/graph/")))\n'+
'\n'+
'(def base-id (prefixer (base-domain "/id/")))\n'+
'\n'+
'(def base-vocab (prefixer (base-domain "/def/")))\n'+
'\n'+
'(def base-data (prefixer (base-domain "/data/")))\n'+
'\n'+
'(def make-graph\n'+
'  (graph-fn [{:keys [name sex age person-uri gender]}]\n'+
'            (graph (base-graph "example")\n'+
'                   [person-uri\n'+
'                    [rdf:a foaf:Person]\n'+
'                    [foaf:gender sex]\n'+
'                    [foaf:age age]\n'+
'                    [foaf:name (s name)]])))\n'+
'\n'+
'(defpipe my-pipe\n'+
'  "Pipeline to convert tabular persons data into a different tabular format."\n'+
'  [data-file]\n'+
'  (-> (read-dataset data-file :format :csv)\n'+
'      (drop-rows 1)\n'+
'      (make-dataset [:name :sex :age])\n'+
'      (derive-column :person-uri [:name] base-id)\n'+
'      (mapc {:age ->integer\n'+
'             :sex {"f" (s "female")\n'+
'                   "m" (s "male")}})))\n'+
'\n'+
'(defgraft my-graft\n'+
'  "Pipeline to convert the tabular persons data sheet into graph data."\n'+
'  my-pipe make-graph)',*/
							'(defpipe my-pipe [data-file] (-> (read-dataset data-file :format :csv)))',
						options: {
							filename: 'pipeline.clj',
							contentType: 'text/plain'
						}
					},
					data: {
						value: body,
						options: {
							filename: 'data.csv',
							contentType: 'text/csv'
						}
					},
					command: req.query.command || 'my-pipe'
				}
			}, function(err, response, body) {
				res.status(response.statusCode);

				res.contentType('text/plain');
				res.send(body);
				return;
			});
		});
	});

	app.get('/lapin', cors(), function(req, res) {
		var distributionUri = req.query.distributionUri,
			transformationUri = req.query.transformationUri;

		if (!distributionUri) {
			res.status(418).json({error: "The distribution URI parameter is missing"});
			return;
		}

		if (!transformationUri) {
			res.status(418).json({error: "The transformationUri URI parameter is missing"});
			return;
		}

		request.get({
			url: endpointOntotext + "/catalog/distributions/file",
			headers: {
				'distrib-id': distributionUri
			}
		}, function(err, response, body) {
			if (response.statusCode === 200) {
				request.post({
        			url: endpointOntotext+"/dapaas-services/grafter/transformation/preview",
        			json: true,
					headers: {
						'transformation-id': transformationUri
					},
        			formData: {
        				"input-file": {
        					value: body,
        					options: {
								filename: 'data.csv',
								contentType: 'text/csv'
							}
        				},
						"command": req.query.command || "my-pipe",
        				"transformation-type": req.query.transformationType || "pipe",
        			} 
				}, function(err, response, body) {
					res.status(response.statusCode);

					res.contentType('text/plain');
					res.send(body);
					return;
				});
				return;
			}

			res.status(response.statusCode);

			res.contentType('text/plain');
			res.send(body);
			return;
		});
	});
 
	app.get('/canard', cors(), function(req, res) {
					request.post({
						url: endpoint,
						json: true,
						formData: {
							pipeline: {
								value: '(defn ->integer\n'+
'  "An example transformation function that converts a string to an integer"\n'+
'  [s]\n'+
'  (Integer/parseInt s))\n'+
'\n'+
'(def base-domain (prefixer "http://my-domain.com"))\n'+
'\n'+
'(def base-graph (prefixer (base-domain "/graph/")))\n'+
'\n'+
'(def base-id (prefixer (base-domain "/id/")))\n'+
'\n'+
'(def base-vocab (prefixer (base-domain "/def/")))\n'+
'\n'+
'(def base-data (prefixer (base-domain "/data/")))\n'+
'\n'+
'(def make-graph\n'+
'  (graph-fn [{:keys [name sex age person-uri gender]}]\n'+
'            (graph (base-graph "example")\n'+
'                   [person-uri\n'+
'                    [rdf:a foaf:Person]\n'+
'                    [foaf:gender sex]\n'+
'                    [foaf:age age]\n'+
'                    [foaf:name (s name)]])))\n'+
'\n'+
'(defpipe my-pipe\n'+
'  "Pipeline to convert tabular persons data into a different tabular format."\n'+
'  [data-file]\n'+
'  (-> (read-dataset data-file :format :csv)\n'+
'      (drop-rows 1)\n'+
'      (make-dataset [:name :sex :age])\n'+
'      (derive-column :person-uri [:name] base-id)\n'+
'      (mapc {:age ->integer\n'+
'             :sex {"f" (s "female")\n'+
'                   "m" (s "male")}})))\n'+
'\n'+
'(defgraft my-graft\n'+
'  "Pipeline to convert the tabular persons data sheet into graph data."\n'+
'  my-pipe make-graph)',
								options: {
									filename: 'pipeline.clj',
									contentType: 'text/plain'
								}
							},
							data: {
								value: 'name,sex,age\nAlice,f,34\nBob,m,63',
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

						// if (format === 'edn')
						// {
							res.contentType('text/plain');
							res.send(ednResult);
							return;
						// } 

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
}