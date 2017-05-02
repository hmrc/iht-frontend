 var selenium = require('selenium-webdriver')
 var By = selenium.By, until = selenium.until;
 var actionHelper = require('./action-helper.js');
 var accessibilityhelper = require('./check-accessibility-helper.js');


 var actsAsYesNo = function actsAsYesNo(done, driver, options){
        driver.get(options.url)
        actionHelper.triggerErrorSummaryHelper(done, driver, options.pageTitle)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });

    }
exports.actsAsYesNo = actsAsYesNo;

 var actsAsYesNoWithValue = function actsAsYesNoWithValue(done, driver, options){
        driver.get(options.url)
        driver.wait(until.titleContains(options.pageTitle), 2000)
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    }
 exports.actsAsYesNoWithValue = actsAsYesNoWithValue;


 var actsAsStandardForm = function actsAsStandardForm(done, driver, options){
         driver.get(options.url)
         actionHelper.triggerErrorSummaryHelper(done, driver, options.pageTitle, options.button)
         driver.then(function(){
             accessibilityhelper.checkAccessibility(done, driver)
         });
     }
  exports.actsAsStandardForm = actsAsStandardForm;