module.exports = function(app) {
	app.dataSources['pgminicloud'].autoupdate(['datapages', 'transformation', 'file'], function(err) {
		console.log(err || "Autoupdate successfull");
	});
};