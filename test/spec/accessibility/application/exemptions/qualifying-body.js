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

    // Close website after each test is run (so it is opened fresh each time)
    afterEach(function(done) {
      driver.quit().then(function () {
          done();
      });
    });

    function fillAssetsLeftToBody(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/assets-left-to-other-body')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillBodyName(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/body-name')
        driver.findElement(By.name("name")).sendKeys('Benton cat home');
        actionHelper.submitPageHelper(done, driver);
    }

    function fillBodyValue(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/assets-value-left-to-body/1')
        driver.findElement(By.name("totalValue")).sendKeys('5000');
        actionHelper.submitPageHelper(done, driver);
    }

    it('assets left to body yes/no', function (done) {
        behaves.actsAsYesNo(done, driver, {
            url: Browser.baseUrl + '/estate-report/assets-left-to-other-body',
            pageTitle: 'Any assets left to other body'
        })
    });

    it('add a body', function (done) {
        fillAssetsLeftToBody(done, driver)

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/add-another-body',
            pageTitle: "Add another body"
        })
    });

    it('add a body, filled', function (done) {
        fillAssetsLeftToBody(done, driver)
        fillBodyName(done, driver)
        fillBodyValue(done, driver)

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/add-another-body/1',
            pageTitle: "Add another body"
        })
    });

    it('other bodies overview', function (done) {
        fillAssetsLeftToBody(done, driver)
        fillBodyName(done, driver)
        fillBodyValue(done, driver)

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/assets-left-to-other-bodies',
            pageTitle: "Assets left to other bodies"
        })
    });

    it('delete a body', function (done) {
        fillAssetsLeftToBody(done, driver)
        fillBodyName(done, driver)

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/delete-qualifying-body/1',
            pageTitle: "Delete qualifying body"
        })
    });

    it('body name', function (done) {
        fillAssetsLeftToBody(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/body-name',
            pageTitle: "Body name"
        })
    });

    it('body value', function (done) {
        fillAssetsLeftToBody(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/assets-value-left-to-body',
            pageTitle: "Value of assets left to body"
        })
    });

});