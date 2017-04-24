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


describe('Money (Assets) accessibility : ', function() {
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

    function submitPage(button){
        var buttonSelector = '#save-continue'
        if(button){
            buttonSelector = button
        }
        driver.findElement(By.css(buttonSelector)).click();
    }


    function triggerErrorSummary(done, title, button){
        driver.wait(until.titleContains(title), 2000)
        submitPage(button);
        driver.wait(until.titleContains(title), 2000)
    }


    function fillMoneyOwned(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/own-money-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("value")).sendKeys('5000');
        submitPage();
    }
    function fillMoneyJointlyOwned(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/money-jointly-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("shareValue")).sendKeys('8000');
        submitPage();
    }


    it('money overview', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/money-owned')
        driver.wait(until.titleContains('Money'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('money overview, filled', function (done) {
        fillMoneyOwned();
        fillMoneyJointlyOwned();
        driver.get('http://localhost:9070/inheritance-tax/estate-report/money-owned')
        driver.wait(until.titleContains('Money'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('money owned by deceased', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/own-money-owned')
        triggerErrorSummary(done, 'Own money owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
           accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('money owned jointly', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/money-jointly-owned')
        triggerErrorSummary(done, 'Joint money owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });


});



