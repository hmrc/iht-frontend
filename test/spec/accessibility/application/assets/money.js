var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../../spec-helpers/reporter.js');
var Browser = require('../../../../spec-helpers/browser.js');
var accessibilityhelper = require('../../../../spec-helpers/check-accessibility-helper.js');
var loginhelper = require('../../../../spec-helpers/login-helper.js');
var actionHelper = require('../../../../spec-helpers/action-helper.js');
var behaves = require('../../../../spec-helpers/behaviour.js');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


describe('Money (Assets) accessibility : ', function() {
    var driver;

    beforeEach(function(done) {
      driver = Browser.startBrowser();

      loginhelper.authenticate(done, driver, 'report')
    });

    // Close website after each test is run (so it is opened fresh each time)
    afterEach(function(done) {
      driver.quit().then(function () {
          done();
      });
    });


    function fillMoneyOwned(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/own-money-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("value")).sendKeys('5000');
        actionHelper.submitPageHelper(done, driver);
    }
    function fillMoneyJointlyOwned(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/money-jointly-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("shareValue")).sendKeys('8000');
        actionHelper.submitPageHelper(done, driver);
    }


    it('money overview', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/money-owned')
        driver.wait(until.titleContains('Money'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('money overview, filled', function (done) {
        fillMoneyOwned(done, driver);
        fillMoneyJointlyOwned(done, driver);
        driver.get('http://localhost:9070/inheritance-tax/estate-report/money-owned')
        driver.wait(until.titleContains('Money'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('money owned by deceased', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/own-money-owned')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Own money owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
           accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('money owned jointly', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/money-jointly-owned')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Joint money owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });


});



