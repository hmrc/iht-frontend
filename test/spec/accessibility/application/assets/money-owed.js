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


describe('Money owed (Assets) accessibility : ', function() {
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

    it('money owed yes/no', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/money-owed-to-deceased')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Money owed to the deceased')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('money owed yes/no, with value', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/money-owed-to-deceased')
        driver.wait(until.titleContains('Money owed to the deceased'), 2000)
        driver.findElement(By.css('#yes-label')).click()
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

});