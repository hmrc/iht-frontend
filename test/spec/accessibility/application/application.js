var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../spec-helpers/reporter.js');
var accessibilityhelper = require('../../../spec-helpers/check-accessibility-helper.js');
var loginhelper = require('../../../spec-helpers/login-helper.js');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


describe('Application accessibility : ', function() {
    var driver;

    beforeEach(function(done) {


      driver = new selenium.Builder()
          .forBrowser('chrome')
          .build();

           loginhelper.authenticate(done, driver, 'app')
    });

    // Close website after each test is run (so it is opened fresh each time)
    afterEach(function(done) {
      driver.quit().then(function () {
          done();
      });
    });



    function submitPage(done){
        driver.findElement(By.css('#continue-button')).click();
    }


    function triggerErrorSummary(done, title){
        driver.wait(until.titleContains(title), 2000)
        submitPage();
        driver.wait(until.titleContains(title), 2000)
    }




    it('estate report', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report')
        driver.wait(until.titleContains('Your estate reports'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('estate report overview', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report')
        driver.findElement(By.css("table a:first-of-type")).click();
        driver.wait(until.titleContains('Estate overview'), 2000)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });



});



