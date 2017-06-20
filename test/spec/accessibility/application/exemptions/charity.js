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


describe('Assets left to charity (Exemptions), accessibility : ', function() {
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

    it('assets left to charity yes/no', function (done) {
        behaves.actsAsYesNo(done, driver, {
            url: Browser.baseUrl + '/estate-report/assets-left-to-charity',
            pageTitle: 'Any assets left to a charity'
        })
    });

    it('add a charity', function (done) {
        actionHelper.populateApplicationData(driver, 'ExemptionsAssetsLeftToCharityYesFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/add-charity',
            pageTitle: "Add charity"
        })
    });

    it('add a charity, filled', function (done) {
        actionHelper.populateApplicationData(driver, 'ExemptionsAssetsLeftToCharityYesNameNumberValueFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/add-charity/1',
            pageTitle: "Add charity"
        })
    });

    it('charity overview', function (done) {
        actionHelper.populateApplicationData(driver, 'ExemptionsAssetsLeftToCharityYesNameNumberValueFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/assets-left-to-charities',
            pageTitle: "Assets left to charities"
        })
    });

    it('delete a charity', function (done) {
        actionHelper.populateApplicationData(driver, 'ExemptionsAssetsLeftToCharityYesNameNumberValueFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/delete-charity/1',
            pageTitle: "Delete charity"
        })
    });

    it('charity name', function (done) {
        actionHelper.populateApplicationData(driver, 'ExemptionsAssetsLeftToCharityYesFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/charity-name',
            pageTitle: "Charity name"
        })
    });

    it('charity number', function (done) {
        actionHelper.populateApplicationData(driver, 'ExemptionsAssetsLeftToCharityYesFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/charity-number',
            pageTitle: "Charity number"
        })
    });

    it('charity value', function (done) {
        actionHelper.populateApplicationData(driver, 'ExemptionsAssetsLeftToCharityYesFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/assets-value-left-to-charity',
            pageTitle: "Value of assets left to charity"
        })
    });

});