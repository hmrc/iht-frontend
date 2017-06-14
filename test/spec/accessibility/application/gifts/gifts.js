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


describe('Gifts, accessibility : ', function() {
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

    function fillGiftsGivenAway(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/gifts-value-given-away')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillGiftsWithReservationOfBenefit(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/gifts-with-reservation-of-benefit')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillGiftsGivenAwayInSevenYears(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/type-of-gifts-given-away')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillGiftsGivenToATrust(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/gifts-given-to-a-company')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillGiftsGivenInYear(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/gifts-value-for-year-1')
        driver.wait(until.titleContains('Value of gifts for year'), 2000)

        driver.findElement(By.name("value")).sendKeys('5000');  
        driver.findElement(By.name("exemptions")).sendKeys('3000');  
        actionHelper.submitPageHelper(done, driver);
    }

    it('gifts given away', function (done) {
        behaves.actsAsStandardForm(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/gifts-value-given-away',
            pageTitle: "Gifts given away"

        })
    });

    it('gifts overview', function (done) {
        fillGiftsGivenAway(done, driver);

        behaves.actsAsBasicPage(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/gifts-given-away',
            pageTitle: "Gifts given away"

        })
    });

    it('gifts overview, filled', function (done) {
        fillGiftsGivenAway(done, driver);
        fillGiftsWithReservationOfBenefit(done, driver);
        fillGiftsGivenAwayInSevenYears(done, driver);
        fillGiftsGivenToATrust(done, driver);
        fillGiftsGivenInYear(done, driver);

        behaves.actsAsBasicPage(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/gifts-given-away',
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
            pageTitle: "Gifts given away"

        })
    });

    it('gifts given to a company, trust or charity', function (done) {
        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/gifts-given-to-a-company',
            pageTitle: "Gifts given away"

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