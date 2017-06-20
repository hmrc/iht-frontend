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


fdescribe('Pensions (Assets) accessibility : ', function() {
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


    it('pensions filter question', function (done) {
        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/any-private-pensions-owned',
            pageTitle: "Any private pensions"
        })
    });

    it('pensions overview', function (done) {
        actionHelper.populateApplicationData(driver, 'PensionsFilterFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/private-pensions',
            pageTitle: "Private pensions"
        })
    });

    it('pensions overview, filled', function (done) {
        actionHelper.populateApplicationData(driver, 'PensionsFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/private-pensions',
            pageTitle: "Private pensions"
        })
    });

    it('changes to pension', function (done) {
        actionHelper.populateApplicationData(driver, 'PensionsFilterFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/any-pension-changes',
            pageTitle: "Changes to pension"
        })
    });

    it('pension value', function (done) {
        actionHelper.populateApplicationData(driver, 'PensionsFilterFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/value-of-pensions',
            pageTitle: "Pension value"
        })
    });

});



