var selenium = require('selenium-webdriver');
var AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../../spec-helpers/reporter');
var Browser = require('../../../../spec-helpers/browser');
var accessibilityhelper = require('../../../../spec-helpers/check-accessibility-helper');
var loginhelper = require('../../../../spec-helpers/login-helper');
var actionHelper = require('../../../../spec-helpers/action-helper');
var behaves = require('../../../../spec-helpers/behaviour');
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

    afterEach(function(done) {
      driver.quit().then(function () {
          done();
      });
    });

    function fillTrustQuestion(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/any-assets-in-trust')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillHowManyTrusts(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/how-many-trusts')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillValueOfTrust(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/value-of-trusts')
        driver.findElement(By.name("value")).sendKeys('5000');
        actionHelper.submitPageHelper(done, driver);
    }


    it('benefit from trusts yes/no', function (done) {
        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/any-assets-in-trust',
            pageTitle: "Any assets in trust"

        })
    });

    it('trust overview', function (done) {
        fillTrustQuestion(done, driver);

         behaves.actsAsBasicPage(done, driver, {
             url: Browser.baseUrl + '/estate-report/assets-in-trust',
             pageTitle: "Assets held in trust"

         })
    });

    it('trust overview, filled', function (done) {
        fillTrustQuestion(done, driver);
        fillHowManyTrusts(done, driver);
        fillValueOfTrust(done, driver);

         behaves.actsAsBasicPage(done, driver, {
             url: Browser.baseUrl + '/estate-report/assets-in-trust',
             pageTitle: "Assets held in trust"

         })
    });

    it('how many trusts', function (done){
        fillTrustQuestion(done, driver);

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/how-many-trusts',
            pageTitle: "How many trusts benefitted"

        })
    });

    it('value of trust', function (done){
        fillTrustQuestion(done, driver);

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/value-of-trusts',
            pageTitle: "Trust value"

        })
    });




});