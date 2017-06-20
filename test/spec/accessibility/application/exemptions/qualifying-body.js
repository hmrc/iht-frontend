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


describe('Assets left to qualifying body (Exemptions), accessibility : ', function() {
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

    it('assets left to body yes/no', function (done) {
        behaves.actsAsYesNo(done, driver, {
            url: Browser.baseUrl + '/estate-report/assets-left-to-other-body',
            pageTitle: 'Any assets left to other body'
        })
    });

    it('add a body', function (done) {
        actionHelper.populateApplicationData(driver, 'ExemptionsAssetsLeftToQualifyingBodyYesFilled');
        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/add-another-body',
            pageTitle: "Add another body"
        })
    });

    it('add a body, filled', function (done) {
        actionHelper.populateApplicationData(driver, 'ExemptionsAssetsLeftToQualifyingBodyYesNameValueFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/add-another-body/1',
            pageTitle: "Add another body"
        })
    });

    it('other bodies overview', function (done) {
        actionHelper.populateApplicationData(driver, 'ExemptionsAssetsLeftToQualifyingBodyYesNameValueFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/assets-left-to-other-bodies',
            pageTitle: "Assets left to other bodies"
        })
    });

    it('delete a body', function (done) {
        actionHelper.populateApplicationData(driver, 'ExemptionsAssetsLeftToQualifyingBodyYesNameValueFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/delete-qualifying-body/1',
            pageTitle: "Delete qualifying body"
        })
    });

    it('body name', function (done) {
        actionHelper.populateApplicationData(driver, 'ExemptionsAssetsLeftToQualifyingBodyYesFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/body-name',
            pageTitle: "Body name"
        })
    });

    it('body value', function (done) {
        actionHelper.populateApplicationData(driver, 'ExemptionsAssetsLeftToQualifyingBodyYesFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/assets-value-left-to-body',
            pageTitle: "Value of assets left to body"
        })
    });

});