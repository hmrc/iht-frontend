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


describe('Insurance (Assets) accessibility : ', function() {
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



    it('insurance overview', function (done) {
        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/insurance-policies-owned',
            pageTitle: 'Insurance policies'
        })
    });

    it('insurance overview, filled', function (done) {
        actionHelper.populateApplicationData(driver, 'InsuranceFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/insurance-policies-owned',
            pageTitle: 'Insurance policies'
        })
    });

    it('insurance policies paying to the deceased', function (done) {
        behaves.actsAsYesNoWithValue(done, driver, {
            url: Browser.baseUrl + '/estate-report/insurance-policies-paying-to-deceased',
            pageTitle: 'Own insurance policies'
        })
    });

    it('joint insurance policies', function (done) {
        behaves.actsAsYesNoWithValue(done, driver, {
            url: Browser.baseUrl + '/estate-report/jointly-owned-insurance-policies',
            pageTitle: 'Joint insurance policies'
        })
    });

    it('insurance policy gifted yes/no', function (done) {
        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/any-insurance-policies-gifted',
            pageTitle: 'Premiums paid for someone else'
        })
    });

    it('insurance policy gifted value', function (done) {
        actionHelper.populateApplicationData(driver, 'InsuranceNoOverMax');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/value-of-gifted-policies',
            pageTitle: 'Value of gifted policies'
        })
    });

    it('insurance policy annuity', function (done) {
        actionHelper.populateApplicationData(driver, 'InsuranceNoAnnuity');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/any-annuities',
            pageTitle: 'Any annuities bought'
        })
    });

    it('insurance policy in trust', function (done) {
        actionHelper.populateApplicationData(driver, 'InsuranceNoInTrust');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/insurance-policies-placed-in-trust',
            pageTitle: 'Policies placed in trust'
        })
    });


    it('insurance policies are gifts', function (done) {
        actionHelper.populateApplicationData(driver, 'InsuranceFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/insurance-policies-are-gifts',
            pageTitle: 'Insurance premiums'
        })
    });
});



