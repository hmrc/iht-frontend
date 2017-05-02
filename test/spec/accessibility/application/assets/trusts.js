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


describe('Trusts (Assets) accessibility : ', function() {
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
        behaves.actsAsStandardForm(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/any-assets-in-trust',
            pageTitle: "Any assets in trust"

        })
    });

    it('trust overview', function (done) {
        fillTrustQuestion(done, driver);

         behaves.actsAsBasicPage(done, driver, {
             url: 'http://localhost:9070/inheritance-tax/estate-report/assets-in-trust',
             pageTitle: "Assets held in trust"

         })
    });

    it('trust overview, filled', function (done) {
        fillTrustQuestion(done, driver);
        fillHowManyTrusts(done, driver);
        fillValueOfTrust(done, driver);

         behaves.actsAsBasicPage(done, driver, {
             url: 'http://localhost:9070/inheritance-tax/estate-report/assets-in-trust',
             pageTitle: "Assets held in trust"

         })
    });

    it('how many trusts', function (done){
        fillTrustQuestion(done, driver);

        behaves.actsAsStandardForm(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/how-many-trusts',
            pageTitle: "How many trusts benefitted"

        })
    });

    it('value of trust', function (done){
        fillTrustQuestion(done, driver);

        behaves.actsAsBasicPage(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/value-of-trusts',
            pageTitle: "Trust value"

        })
    });




});