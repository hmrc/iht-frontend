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


describe('Pensions (Assets) accessibility : ', function() {
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

    function fillPensionsFilter(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-private-pensions-owned')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillPensionChanges(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-pension-changes')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillPensionValue(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-pensions')
        driver.findElement(By.css('#value')).sendKeys('15000')
        actionHelper.submitPageHelper(done, driver);
    }


    it('pensions filter question', function (done) {
        behaves.actsAsStandardForm(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/any-private-pensions-owned',
            pageTitle: "Any private pensions"
        })
    });

    it('pensions overview', function (done) {
        fillPensionsFilter(done, driver);

        behaves.actsAsBasicPage(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/private-pensions',
            pageTitle: "Private pensions"
        })
    });

    it('pensions overview, filled', function (done) {
        fillPensionsFilter(done, driver);
        fillPensionChanges(done, driver);
        fillPensionValue(done, driver);

        behaves.actsAsBasicPage(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/private-pensions',
            pageTitle: "Private pensions"
        })
    });

    it('changes to pension', function (done) {
        fillPensionsFilter(done, driver);

        behaves.actsAsStandardForm(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/any-pension-changes',
            pageTitle: "Changes to pension"
        })
    });

    it('pension value', function (done) {
        fillPensionsFilter(done, driver);

        behaves.actsAsBasicPage(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/value-of-pensions',
            pageTitle: "Pension value"
        })
    });

});



