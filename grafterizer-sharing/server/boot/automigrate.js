module.exports = function(app) {
    app.dataSources['persistent'].autoupdate(['function'], function(err) {
        console.log(err || "Autoupdate successfull");
    });
};