var selenium = require('selenium-webdriver')
var By = selenium.By, until = selenium.until;
var Browser = require('./browser.js');

var submitPageHelper = function(done, driver, button) {

        var buttonSelector = '#save-continue'
        if(button){
            buttonSelector = button
        }
        driver.findElement(By.css(buttonSelector)).click();

}
exports.submitPageHelper = submitPageHelper;

var triggerErrorSummaryHelper = function(done, driver, title, button) {

        driver.wait(until.titleContains(title), 2000)
        submitPageHelper(done, driver, button);
        driver.wait(until.titleContains(title), 2000)

}
exports.triggerErrorSummaryHelper = triggerErrorSummaryHelper;

var populateRegistrationData = function(driver, dataFile) {
    driver.get(Browser.baseUrl + '/test-only/store-registration-details')
    var data = require('../spec-json/registration/' + dataFile);
    var json = JSON.stringify(data)
    driver.executeScript(function(args) {
        document.querySelector('#registrationDetails').innerText = args;
    }, json);
    driver.findElement(By.css('[type="submit"]')).click();
}
exports.populateRegistrationData = populateRegistrationData;

var populateApplicationData = function(driver, dataFile) {
    driver.get(Browser.baseUrl + '/test-only/store-application-details')
    var data = require('../spec-json/application/' + dataFile);
    var json = JSON.stringify(data)
    driver.executeScript(function(args) {
        document.querySelector('#nino').setAttribute("value","CS700100A");
        document.querySelector('#fileReference').setAttribute("value","000001");
        document.querySelector('#applicationDetails').innerText = args;
    }, json);
    driver.findElement(By.css('[type="submit"]')).click();
}
exports.populateApplicationData = populateApplicationData;