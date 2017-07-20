var selenium = require('selenium-webdriver');
var AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../spec-helpers/reporter.js');
var Browser = require('../../../spec-helpers/browser.js');
var accessibilityhelper = require('../../../spec-helpers/check-accessibility-helper.js');
var loginhelper = require('../../../spec-helpers/login-helper.js');
var behaves = require('../../../spec-helpers/behaviour.js');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


describe('Application accessibility : ', function() {
    var driver;

    beforeEach(function(done) {
      driver = Browser.startBrowser();
      loginhelper.authenticate(done, driver, 'app')
    });

    afterEach(function(done) {
      driver.quit().then(function () {
          done();
      });
    });



    it('estate report', function (done) {
        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report',
            pageTitle: "Your estate reports"

        })
    });

    it('estate report overview', function (done) {
        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/estate-overview/CS700100A000001',
            pageTitle: "Estate overview"

        });
    });

    it('estate report overview, filled', function (done) {
        driver.get(Browser.baseUrl + '/estate-report/estate-overview/CS700100A000001')
        driver.wait(until.titleContains('Estate overview'), 2000)

        driver.get(Browser.baseUrl + '/test-only/fill');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/estate-overview/CS700100A000001',
            pageTitle: "Estate overview"

        });
    });



});



