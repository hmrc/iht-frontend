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


describe('TNRB, accessibility : ', function() {
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

    it('tnrb overview, filled', function (done) {
        actionHelper.populateApplicationData(driver, 'TNRBAllFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/increasing-the-threshold',
            pageTitle: "Increasing the threshold"
        })
    });

    it('threshold increased', function (done) {
        actionHelper.populateApplicationData(driver, 'TNRBAllFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/threshold-increased',
            pageTitle: "Threshold increased"
        })
    });

    it('guidance', function (done) {
        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/increase-the-threshold',
            pageTitle: "Increasing the threshold"
        })
    });

    it('deceased ever widowed', function (done) {
        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/deceased-ever-widowed',
            pageTitle: "Increasing the Inheritance Tax threshold"

        })
    });

    it('date of death', function (done) {
        actionHelper.populateApplicationData(driver, 'TNRBDeceasedEverWidowedFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/date-of-death',
            pageTitle: "Increasing the Inheritance Tax threshold"

        })
    });

    it('name of spouse', function (done) {
        actionHelper.populateApplicationData(driver, 'TNRBDeceasedEverWidowedDateOfDeathFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/name-of-spouse-or-civil-partner',
            pageTitle: "Name of the spouse"

        })
    });

    it('date of marriage', function (done) {
        actionHelper.populateApplicationData(driver, 'TNRBDeceasedEverWidowedDateOfDeathFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/marriage-or-civil-partnership-date',
            pageTitle: "Date of marriage"

        })
    });

    it('permanent home', function (done) {
        actionHelper.populateApplicationData(driver, 'TNRBDeceasedEverWidowedDateOfDeathFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/permanent-home-location',
            pageTitle: "Location of permanent home"

        })
    });

    it('fully exempt estate', function (done) {
        actionHelper.populateApplicationData(driver, 'TNRBDeceasedEverWidowedDateOfDeathFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/fully-exempt-estate',
            pageTitle: "Fully exempt estate"

        })
    });

    it('joint assets in estate', function (done) {
        actionHelper.populateApplicationData(driver, 'TNRBDeceasedEverWidowedDateOfDeathFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/joint-assets-in-estate',
            pageTitle: "Joint assets in estate"

        })
    });

    it('any relief claimed', function (done) {
        actionHelper.populateApplicationData(driver, 'TNRBDeceasedEverWidowedDateOfDeathFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/any-relief-claimed',
            pageTitle: "Any relief claimed"

        })
    });

    it('trust benefit', function (done) {
        actionHelper.populateApplicationData(driver, 'TNRBDeceasedEverWidowedDateOfDeathFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/any-trusts-in-estate',
            pageTitle: "Any trust benefited"

        })
    });

    it('non-exempt gifts', function (done) {
        actionHelper.populateApplicationData(driver, 'TNRBDeceasedEverWidowedDateOfDeathFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/any-non-exempt-gifts',
            pageTitle: "Any non-exempt gifts"

        })
    });

    it('gifts with reservation of benefit', function (done) {
        actionHelper.populateApplicationData(driver, 'TNRBDeceasedEverWidowedDateOfDeathFilled');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/gift-types-given-away',
            pageTitle: "Type of gifts"

        })
    });



});