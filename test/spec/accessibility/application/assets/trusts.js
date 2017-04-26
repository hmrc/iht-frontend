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


describe('Trusts (Assets) accessibility : ', function() {
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

    function fillTrustQuestion(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-assets-in-trust')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillHowManyTrusts(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/how-many-trusts')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillValueOfTrust(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-trusts')
        driver.findElement(By.name("value")).sendKeys('5000');
        actionHelper.submitPageHelper(done, driver);
    }


    it('benefit from trusts yes/no', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-assets-in-trust')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Any assets in trust')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('trust overview', function (done) {
        fillTrustQuestion();

        driver.wait(until.titleContains('Assets held in trust'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('trust overview, filled', function (done) {
        fillTrustQuestion(done, driver);
        fillHowManyTrusts(done, driver);
        fillValueOfTrust(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/assets-in-trust');
        driver.wait(until.titleContains('Assets held in trust'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('how many trusts', function (done){
        fillTrustQuestion(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/how-many-trusts')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'How many trusts benefitted')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('value of trust', function (done){
        fillTrustQuestion(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-trusts')
        driver.wait(until.titleContains('Trust value'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });




});