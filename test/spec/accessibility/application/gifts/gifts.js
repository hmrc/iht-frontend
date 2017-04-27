var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../../spec-helpers/reporter.js');
var accessibilityhelper = require('../../../../spec-helpers/check-accessibility-helper.js');
var loginhelper = require('../../../../spec-helpers/login-helper.js');
var actionHelper = require('../../../../spec-helpers/action-helper.js');

var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


describe('Gifts, accessibility : ', function() {
    var driver;

    beforeEach(function(done) {
      driver = new selenium.Builder()
          .forBrowser('phantomjs')
          .build();
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
        driver.get('http://localhost:9070/inheritance-tax/estate-report/gifts-value-given-away')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Gifts given away')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('gifts overview', function (done) {
        fillGiftsGivenAway(done, driver);
        driver.get('http://localhost:9070/inheritance-tax/estate-report/gifts-given-away')
        driver.wait(until.titleContains('Gifts given away'), 2000)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('gifts overview, filled', function (done) {
        fillGiftsGivenAway(done, driver);
        fillGiftsWithReservationOfBenefit(done, driver);
        fillGiftsGivenAwayInSevenYears(done, driver);
        fillGiftsGivenToATrust(done, driver);
        fillGiftsGivenInYear(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/gifts-given-away')
        driver.wait(until.titleContains('Gifts given away'), 2000)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('gifts with reservation of benefit', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/gifts-with-reservation-of-benefit')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Gifts with reservation of benefit')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('gifts given away in 7 years before death', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/type-of-gifts-given-away')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Gifts given away')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('gifts given to a company, trust or charity', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/gifts-given-to-a-company')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Gifts given away')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('gifts given in year', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/gifts-value-for-year-1')
        driver.wait(until.titleContains('Value of gifts for year'), 2000)

        driver.findElement(By.name("value")).sendKeys('5000');  
        driver.findElement(By.name("exemptions")).sendKeys('3000');  

        driver.sleep(1000);

        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

});