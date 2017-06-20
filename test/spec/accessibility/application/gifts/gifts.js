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


describe('Gifts, accessibility : ', function() {
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


    it('gifts given away', function (done) {
        behaves.actsAsYesNo(done, driver, {
            url: Browser.baseUrl + '/estate-report/gifts-value-given-away',
            pageTitle: "Gifts given away"

        })
    });

    it('gifts overview', function (done) {
        actionHelper.populateApplicationData(driver, 'GiftsFilter');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/gifts-given-away',
            pageTitle: "Gifts given away"

        })
    });

    it('gifts overview, filled', function (done) {
        actionHelper.populateApplicationData(driver, 'GiftsOverviewFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/gifts-given-away',
            pageTitle: "Gifts given away"

        })
    });

    it('gifts with reservation of benefit', function (done) {
        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/gifts-with-reservation-of-benefit',
            pageTitle: "Gifts with reservation of benefit"

        })
    });

    it('gifts given away in 7 years before death', function (done) {
        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/type-of-gifts-given-away',
            pageTitle: "Types of gifts given away in the 7 years before death"

        })
    });

    it('gifts given to a company, trust or charity', function (done) {
        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/gifts-given-to-a-company',
            pageTitle: "Types of gifts given away in the 7 years before death"

        })
    });

    it('gifts value overview', function (done) {
        actionHelper.populateApplicationData(driver, 'GiftsOverviewFilled');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/value-of-gifts-given-away',
            pageTitle: "Value of gifts given away"

        })
    });

    it('gifts given in year', function (done) {
        driver.get(Browser.baseUrl + '/estate-report/gifts-value-for-year-1')
        driver.wait(until.titleContains('Value of gifts for year'), 2000)

        driver.findElement(By.name("value")).sendKeys('5000');  
        driver.findElement(By.name("exemptions")).sendKeys('3000');  

        driver.sleep(1000);

        behaves.actsAsAccessiblePage(done, driver);
    });

});