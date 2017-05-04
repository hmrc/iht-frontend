var selenium = require('selenium-webdriver')
var By = selenium.By, until = selenium.until;

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