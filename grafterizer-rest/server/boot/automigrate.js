module.exports = function(app) {
	app.dataSources['pgminicloud'].automigrate(['datapages', 'transformation'], function(err) {
		console.log(err);
	});
};