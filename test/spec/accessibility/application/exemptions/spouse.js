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


describe('Assets left to spouse (Exemptions), accessibility : ', function() {
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

    it('assets left to spouse yes/no', function (done) {
        behaves.actsAsYesNo(done, driver, {
            url: Browser.baseUrl + '/estate-report/any-assets-left-to-partner',
            pageTitle: 'Any assets left to partner'
        })
    });

    it('spouse permanent home yes/no', function (done) {
        actionHelper.populateApplicationData(driver, 'ExemptionsSpouseYesFilled');

        behaves.actsAsYesNo(done, driver, {
            url: Browser.baseUrl + '/estate-report/partner-residence',
            pageTitle: "Partner’s residence"
        })
    });

    it('name of spouse', function(done){
        actionHelper.populateApplicationData(driver, 'ExemptionsSpouseRequiredFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/partners-name',
            pageTitle: "Partner’s name"
        })
    });

    it('spouse dob', function(done){
        actionHelper.populateApplicationData(driver, 'ExemptionsSpouseRequiredFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/partners-date-of-birth',
            pageTitle: "Partner’s date of birth"
        })
    });

    it('spouse nino', function(done){
        actionHelper.populateApplicationData(driver, 'ExemptionsSpouseRequiredFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/partners-national-insurance-number',
            pageTitle: "Partner’s National Insurance number"
        })
    });

    it('value of assets left to spouse', function(done){
        actionHelper.populateApplicationData(driver, 'ExemptionsSpouseRequiredFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/assets-value-left-to-partner',
            pageTitle: "Value of assets left to partner"
        })
    });
});