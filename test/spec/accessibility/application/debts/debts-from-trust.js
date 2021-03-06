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


describe('Debts from a trust (Debts), accessibility : ', function() {
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

    it('debts owed from a trust', function (done) {
        behaves.actsAsYesNo(done, driver, {
            url: Browser.baseUrl + '/estate-report/debts-owed-from-trust',
            pageTitle: 'Debts owed from trust'
        })
    });

    it('debts owed from a trust, showing value', function (done) {
        behaves.actsAsYesNoWithValue(done, driver, {
            url: Browser.baseUrl + '/estate-report/debts-owed-from-trust',
            pageTitle: 'Debts owed from trust'
        })
    });

});