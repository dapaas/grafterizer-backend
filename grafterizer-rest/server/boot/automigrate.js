module.exports = function(app) {
	app.dataSources['pgminicloud'].automigrate(['canard', 'transformation'], function(err) {
		console.log(err);
	});
};