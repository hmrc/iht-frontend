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

describe('Household (Assets) accessibility : ', function() {
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

    function fillHouseholdOwned(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/own-household-items-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("value")).sendKeys('5000');
        actionHelper.submitPageHelper(done, driver);
    }
    function fillHouseholdJointlyOwned(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/household-items-jointly-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("shareValue")).sendKeys('8000');
        actionHelper.submitPageHelper(done, driver);
    }


    it('household overview', function (done) {
        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/household-items-owned',
            pageTitle: 'Household and personal items'
        })
    });

    it('household overview, filled', function (done) {
        actionHelper.populateApplicationData(driver, 'HouseholdFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/household-items-owned',
            pageTitle: 'Household and personal items'
        })
    });

    it('household owned by deceased', function (done) {
        behaves.actsAsYesNoWithValue(done, driver, {
            url: Browser.baseUrl + '/estate-report/own-household-items-owned',
            pageTitle: 'Own household items owned'
        })
    });

    it('household owned jointly', function (done) {
        behaves.actsAsYesNoWithValue(done, driver, {
            url: Browser.baseUrl + '/estate-report/household-items-jointly-owned',
            pageTitle: 'Joint household items owned'
        })
    });


});



