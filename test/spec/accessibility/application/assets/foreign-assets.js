var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../../spec-helpers/reporter.js');
var Browser = require('../../../../spec-helpers/browser.js');
var accessibilityhelper = require('../../../../spec-helpers/check-accessibility-helper.js');
var loginhelper = require('../../../../spec-helpers/login-helper.js');
var actionHelper = require('../../../../spec-helpers/action-helper.js');
var behaves = require('../../../../spec-helpers/behaviour.js');
var behaves = require('../../../../spec-helpers/behaviour.js');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


fdescribe('Foreign assets (Assets) accessibility : ', function() {
    var driver;

    beforeEach(function(done) {
      driver = Browser.startBrowser();

      loginhelper.authenticate(done, driver, 'report')
    });

    // Close website after each test is run (so it is opened fresh each time)
    afterEach(function(done) {
      driver.quit().then(function () {
          done();
      });
    });


    it('foreign assets yes/no', function (done) {
        behaves.actsAsYesNo(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/foreign-assets-owned',
            pageTitle: 'Foreign assets'
        })
    });

    it('foreign assets yes/no, with value', function (done) {
        behaves.actsAsYesNoWithValue(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/foreign-assets-owned',
            pageTitle: 'Foreign assets'
        })
    });

});