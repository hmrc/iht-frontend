var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../../spec-helpers/reporter.js');
var accessibilityhelper = require('../../../../spec-helpers/check-accessibility-helper.js');
var loginhelper = require('../../../../spec-helpers/login-helper.js');
var actionHelper = require('../../../../spec-helpers/action-helper.js');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


fdescribe('Gifts, accessibility : ', function() {
    var driver;

    beforeEach(function(done) {
      driver = new selenium.Builder()
          .forBrowser('chrome')
          .build();

      loginhelper.authenticate(done, driver, 'report')
    });

    // Close website after each test is run (so it is opened fresh each time)
    afterEach(function(done) {
      driver.quit().then(function () {
          done();
      });
    });

//    function submitPage(button){
//        var buttonSelector = '#save-continue'
//        if(button){
//            buttonSelector = button
//        }
//        driver.findElement(By.css(buttonSelector)).click();
//    }
//
//    function triggerErrorSummary(done, title, button){
//        driver.wait(until.titleContains(title), 2000)
//        submitPage(button);
//        driver.wait(until.titleContains(title), 2000)
//    }





    it('gifts given away', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/gifts-value-given-away')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Gifts given away')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });


});