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


describe('Assets left to charity (Exemptions), accessibility : ', function() {
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

    function fillAssetsLeftToCharity(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/assets-left-to-charity')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillCharityName(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/charity-name')
        driver.findElement(By.name("name")).sendKeys('Dogs Trust');
        actionHelper.submitPageHelper(done, driver);
    }

    function fillCharityNumber(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/charity-number/1')
        driver.findElement(By.name("charityNumber")).sendKeys('123456');
        actionHelper.submitPageHelper(done, driver);
    }

    function fillCharityValue(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/assets-value-left-to-charity/1')
        driver.findElement(By.name("totalValue")).sendKeys('5000');
        actionHelper.submitPageHelper(done, driver);
    }

    it('assets left to charity yes/no', function (done) {
        behaves.actsAsYesNo(done, driver, {
            url: Browser.baseUrl + '/estate-report/assets-left-to-charity',
            pageTitle: 'Any assets left to a charity'
        })
    });

    it('add a charity', function (done) {
        fillAssetsLeftToCharity(done, driver)

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/add-charity',
            pageTitle: "Add charity"
        })
    });

    it('add a charity, filled', function (done) {
        fillAssetsLeftToCharity(done, driver)
        fillCharityName(done, driver)
        fillCharityNumber(done, driver)
        fillCharityValue(done, driver)

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/add-charity/1',
            pageTitle: "Add charity"
        })
    });

    it('charity overview', function (done) {
        fillAssetsLeftToCharity(done, driver)
        fillCharityName(done, driver)
        fillCharityNumber(done, driver)
        fillCharityValue(done, driver)

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/assets-left-to-charities',
            pageTitle: "Assets left to charities"
        })
    });

    it('delete a charity', function (done) {
        fillAssetsLeftToCharity(done, driver)
        fillCharityName(done, driver)

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/delete-charity/1',
            pageTitle: "Delete charity"
        })
    });

    it('charity name', function (done) {
        fillAssetsLeftToCharity(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/charity-name',
            pageTitle: "Charity name"
        })
    });

    it('charity number', function (done) {
        fillAssetsLeftToCharity(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/charity-number',
            pageTitle: "Charity number"
        })
    });

    it('charity value', function (done) {
        fillAssetsLeftToCharity(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/assets-value-left-to-charity',
            pageTitle: "Value of assets left to charity"
        })
    });

});