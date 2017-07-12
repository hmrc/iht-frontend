var selenium = require('selenium-webdriver')

process.env["PATH"] += ":" +  __dirname + "/drivers";

var startBrowser = function(){
    return new selenium.Builder().forBrowser('phantomjs').build();
}
exports.startBrowser = startBrowser;

exports.baseUrl = "http://localhost:9070/inheritance-tax"