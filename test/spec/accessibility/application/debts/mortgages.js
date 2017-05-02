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


describe('Mortgages (Debts), accessibility : ', function() {
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

    function addProperty(done, driver){
        fillPropertyQuestion(done, driver);
        fillPropertyValue(done, driver);
    }

    function fillAnyMortgage(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-mortgage-on-property/1')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("value")).sendKeys('150000');
        actionHelper.submitPageHelper(done, driver);
    }

    it('mortgages overview, no properties', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/mortgages')
        driver.wait(until.titleContains('Mortgages'), 2000)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('mortgages overview, with properties', function (done) {
        addProperty(done, driver)
        fillAnyMortgage(done, driver)

        driver.get('http://localhost:9070/inheritance-tax/estate-report/mortgages')
        driver.wait(until.titleContains('Mortgages'), 2000)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('any mortgage on property', function (done) {
        addProperty(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/any-mortgage-on-property/1',
            pageTitle: "Any mortgage on property"

        })
    });


});