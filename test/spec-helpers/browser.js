var selenium = require('selenium-webdriver')

var startBrowser = function(){
    return new selenium.Builder().forBrowser('phantomjs').build();
}
exports.startBrowser = startBrowser;