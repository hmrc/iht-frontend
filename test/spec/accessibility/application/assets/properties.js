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


describe('Property (Assets) accessibility : ', function() {
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


    it('properties question', function (done) {
        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/any-properties-buildings-land-owned',
            pageTitle: "Properties"
        })
    });

    it('properties overview', function (done) {
        actionHelper.populateApplicationData(driver, 'PropertyFilter');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/properties-buildings-land-owned',
            pageTitle: "Properties, buildings and land"
        })
    });

    it('add property overview', function (done) {
        actionHelper.populateApplicationData(driver, 'PropertyFilter');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/add-property',
            pageTitle: "Add a property"
        })
    });

    it('property address', function (done) {
        actionHelper.populateApplicationData(driver, 'PropertyFilter');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/property-address',
            pageTitle: "Property address"

        })
    });

    it('type of property', function (done) {
        actionHelper.populateApplicationData(driver, 'PropertyFilter');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/type-of-property',
            pageTitle: "Type of property"

        })
    });

    it('how property was owned', function (done) {
        actionHelper.populateApplicationData(driver, 'PropertyFilter');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/how-property-was-owned',
            pageTitle: "How property was owned"

        })
    });

    it('freehold or leasehold', function (done) {
        actionHelper.populateApplicationData(driver, 'PropertyFilter');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/freehold-or-leasehold-property',
            pageTitle: "Freehold or leasehold"

        })
    });

    it('value of property', function (done) {
        actionHelper.populateApplicationData(driver, 'PropertyFilter');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/value-of-property',
            pageTitle: "Property value"

        })
    });

    it('delete property', function(done){
        actionHelper.populateApplicationData(driver, 'OneProperty');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/delete-property/1',
            pageTitle: "Confirm that you want to delete"

        })
    })
});



