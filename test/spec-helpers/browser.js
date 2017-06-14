var selenium = require('selenium-webdriver')

var startBrowser = function(){
    return new selenium.Builder().forBrowser('chrome').build();
}
exports.startBrowser = startBrowser;

exports.baseUrl = "http://localhost:9070/inheritance-tax"