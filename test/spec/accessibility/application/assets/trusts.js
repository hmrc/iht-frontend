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


describe('Trusts (Assets) accessibility : ', function() {
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

    function fillTrustQuestion(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-assets-in-trust')
        driver.findElement(By.css('#yes-label')).click();
        submitPage();
    }

    function fillHowManyTrusts(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/how-many-trusts')
        driver.findElement(By.css('#no-label')).click();
        submitPage();
    }

    function fillValueOfTrust(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-trusts')
        driver.findElement(By.name("value")).sendKeys('5000');
        submitPage();
    }


    it('benefit from trusts yes/no', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-assets-in-trust')
        triggerErrorSummary(done, 'Any assets in trust')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('trust overview', function (done) {
        fillTrustQuestion();

        driver.wait(until.titleContains('Assets held in trust'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('trust overview, filled', function (done) {
        fillTrustQuestion();
        fillHowManyTrusts();
        fillValueOfTrust();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/assets-in-trust');
        driver.wait(until.titleContains('Assets held in trust'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('how many trusts', function (done){
        fillTrustQuestion();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/how-many-trusts')
        triggerErrorSummary(done, 'How many trusts benefitted')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('value of trust', function (done){
        fillTrustQuestion();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-trusts')
        driver.wait(until.titleContains('Trust value'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });




});