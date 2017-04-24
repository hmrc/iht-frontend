var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../../spec-helpers/reporter.js');
var accessibilityhelper = require('../../../../spec-helpers/check-accessibility-helper.js');
var loginhelper = require('../../../../spec-helpers/login-helper.js');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


describe('Business interests (Assets) accessibility : ', function() {
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

    function submitPage(button){
        var buttonSelector = '#save-continue'
        if(button){
            buttonSelector = button
        }
        driver.findElement(By.css(buttonSelector)).click();
    }

    function triggerErrorSummary(done, title, button){
        driver.wait(until.titleContains(title), 2000)
        submitPage(button);
        driver.wait(until.titleContains(title), 2000)
    }


    it('business yes/no', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/business-interests-owned')
        triggerErrorSummary(done, 'Business interests owned')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('business yes/no, with value', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/business-interests-owned')
        driver.wait(until.titleContains('Business interests owned'), 2000)
        driver.findElement(By.css('#yes-label')).click()
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

});