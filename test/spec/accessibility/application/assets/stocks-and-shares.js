var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../../spec-helpers/reporter.js');
var Browser = require('../../../../spec-helpers/browser.js');
var accessibilityhelper = require('../../../../spec-helpers/check-accessibility-helper.js');
var loginhelper = require('../../../../spec-helpers/login-helper.js');
var actionHelper = require('../../../../spec-helpers/action-helper.js');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);

describe('Stocks and Shares (Assets) accessibility : ', function() {
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

    function fillStocksOwned(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/listed-stocks-and-shares-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("valueListed")).sendKeys('5000');
        actionHelper.submitPageHelper(done, driver);
    }
    function fillStocksJointlyOwned(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/unlisted-stocks-and-shares-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("valueNotListed")).sendKeys('8000');
        actionHelper.submitPageHelper(done, driver);
    }


    it('stocks and shares overview', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/stocks-and-shares-owned')
        driver.wait(until.titleContains('Stocks and shares'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('stocks and shares overview, filled', function (done) {
        fillStocksOwned(done, driver);
        fillStocksJointlyOwned(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/stocks-and-shares-owned')
        driver.wait(until.titleContains('Stocks and shares'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('stocks and shares listed on an exchange', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/listed-stocks-and-shares-owned')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Listed stocks and shares')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('stocks and shares not listed on an exchange', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/unlisted-stocks-and-shares-owned')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Unlisted stocks and shares')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
           accessibilityhelper.checkAccessibility(done, driver)
        });
    });


});



