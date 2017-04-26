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


describe('Property (Assets) accessibility : ', function() {
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

    function fillPropertyQuestion(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-properties-buildings-land-owned')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillPropertyValue(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-property')
        driver.findElement(By.name("value")).sendKeys('150000');
        actionHelper.submitPageHelper(done, driver);
    }

    it('assets overview', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/assets-in-estate')
        driver.wait(until.titleContains('Assets in the estate'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('properties question', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-properties-buildings-land-owned')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Properties')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('properties overview', function (done) {
        fillPropertyQuestion(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/properties-buildings-land-owned')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('add property overview', function (done) {
        fillPropertyQuestion(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/add-property')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('property address', function (done) {
        fillPropertyQuestion(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/property-address')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Property address')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('type of property', function (done) {
        fillPropertyQuestion(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/type-of-property')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Type of property')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('how property was owned', function (done) {
        fillPropertyQuestion(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/how-property-was-owned')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'How property was owned')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('freehold or leasehold', function (done) {
        fillPropertyQuestion(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/freehold-or-leasehold-property')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Freehold or leasehold')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('value of property', function (done) {
        fillPropertyQuestion(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-property')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Property value')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('delete property', function(done){
        fillPropertyQuestion(done, driver);
        fillPropertyValue(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/delete-property/1')
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    })
});



