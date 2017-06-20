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


describe('Exemptions, accessibility : ', function() {
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

    it('guidance', function (done) {
        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/claiming-estate-exemptions/CS700100A000001',
            pageTitle: "Claiming estate exemptions"
        })
    });

    it('exemptions overview', function (done) {
        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/estate-exemptions',
            pageTitle: "Estate exemptions"
        })
    });

});