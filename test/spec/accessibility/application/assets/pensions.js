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


describe('Pensions (Assets) accessibility : ', function() {
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


    function fillPensionsFilter(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-private-pensions-owned')
        driver.findElement(By.css('#yes-label')).click();
        submitPage();
    }

    function fillPensionChanges(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-pension-changes')
        driver.findElement(By.css('#no-label')).click();
        submitPage();
    }

    function fillPensionValue(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-pensions')
        driver.findElement(By.css('#value')).sendKeys('15000')
        submitPage();
    }


    it('pensions filter question', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-private-pensions-owned')
        driver.wait(until.titleContains('Any private pensions'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('pensions overview', function (done) {
        fillPensionsFilter();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/private-pensions')
        driver.wait(until.titleContains('Private pensions'), 2000)

        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('pensions overview, filled', function (done) {
        fillPensionsFilter();
        fillPensionChanges();
        fillPensionValue();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/private-pensions')
        driver.wait(until.titleContains('Private pensions'), 2000)

        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('changes to pension', function (done) {
        fillPensionsFilter();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-pension-changes')
        triggerErrorSummary(done, 'Changes to pension')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('pension value', function (done) {
        fillPensionsFilter();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-pensions')
        driver.wait(until.titleContains('Pension value'), 2000)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

});



