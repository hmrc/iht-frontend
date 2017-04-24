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

describe('Household (Assets) accessibility : ', function() {
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


    function fillHouseholdOwned(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/own-household-items-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("value")).sendKeys('5000');
        submitPage();
    }
    function fillHouseholdJointlyOwned(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/household-items-jointly-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("shareValue")).sendKeys('8000');
        submitPage();
    }


    it('household overview', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/household-items-owned')
        driver.wait(until.titleContains('Household and personal items'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('household overview, filled', function (done) {
        fillHouseholdOwned();
        fillHouseholdJointlyOwned();
        driver.get('http://localhost:9070/inheritance-tax/estate-report/household-items-owned')
        driver.wait(until.titleContains('Household and personal items'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('household owned by deceased', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/own-household-items-owned')
        triggerErrorSummary(done, 'Own household items owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('household owned jointly', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/household-items-jointly-owned')
        triggerErrorSummary(done, 'Joint household items owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });


});



