var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../../spec-helpers/reporter.js');
var accessibilityhelper = require('../../../../spec-helpers/check-accessibility-helper.js');
var loginhelper = require('../../../../spec-helpers/login-helper.js');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


describe('Gift guidance, accessibility : ', function() {
    var driver;

    beforeEach(function(done) {
      driver = new selenium.Builder()
          .forBrowser('chrome')
          .build();

      loginhelper.authenticate(done, driver, 'report')
    });

    // Close website after each test is run (so it is opened fresh each time)
    afterEach(function(done) {
      driver.quit().then(function () {
          done();
      });
    });

//    function submitPage(button){
//        var buttonSelector = '#save-continue'
//        if(button){
//            buttonSelector = button
//        }
//        driver.findElement(By.css(buttonSelector)).click();
//    }
//
//    function triggerErrorSummary(done, title, button){
//        driver.wait(until.titleContains(title), 2000)
//        submitPage(button);
//        driver.wait(until.titleContains(title), 2000)
//    }


    it('guide page 1', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/guide-to-gifts/1')
        driver.wait(until.titleContains('A guide to gifts'), 2000)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('guide page 2', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/guide-to-gifts/2')
        driver.wait(until.titleContains('A guide to gifts'), 2000)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('guide page 3', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/guide-to-gifts/3')
        driver.wait(until.titleContains('A guide to gifts'), 2000)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('guide page 4', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/guide-to-gifts/4')
        driver.wait(until.titleContains('A guide to gifts'), 2000)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('guide page 5', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/guide-to-gifts/5')
        driver.wait(until.titleContains('A guide to gifts'), 2000)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('guide page 6', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/guide-to-gifts/6')
        driver.wait(until.titleContains('A guide to gifts'), 2000)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });
});