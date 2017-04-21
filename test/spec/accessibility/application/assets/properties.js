var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../../spec-helpers/reporter.js');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


describe('Property (Assets) accessibility : ', function() {
    var driver;

    beforeEach(function(done) {
      driver = new selenium.Builder()
          .forBrowser('chrome')
          .build();

      driver.manage().timeouts().setScriptTimeout(60000);

      driver.get('http://localhost:9949/auth-login-stub/gg-sign-in');
      driver.findElement(By.name("authorityId")).sendKeys('1');
      driver.findElement(By.name("redirectionUrl")).sendKeys('http://localhost:9070/inheritance-tax/estate-report');
      driver.findElement(By.name("credentialStrength")).sendKeys('strong');
      driver.findElement(By.name("confidenceLevel")).sendKeys('200');
      driver.findElement(By.name("nino")).sendKeys('CS700100A');
      driver.findElement(By.css('[type="submit"]')).click();
      driver.findElement(By.css("table a:first-of-type")).click();
      driver.wait(until.titleContains('Estate overview'), 2000)
          .then(function () {
            driver.get('http://localhost:9070/inheritance-tax/test-only/drop');
            done();
          });
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

    function checkAccessibility(done) {
        AxeBuilder(driver)
        .include('#content')
        .analyze(function(results) {
            if (results.violations.length > 0) {
                console.log('Accessibility Violations: '.bold.bgRed.white, results.violations.length);
                results.violations.forEach(function(violation){
                    console.log(violation);
                    console.log('============================================================'.red);
                });
            }
            expect(results.violations.length).toBe(0);
            done();
        })

    }

    function triggerErrorSummary(done, title, button){
        driver.wait(until.titleContains(title), 2000)
        submitPage(button);
        driver.wait(until.titleContains(title), 2000)
    }


    function fillPropertyQuestion(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-properties-buildings-land-owned')
        driver.findElement(By.css('#yes-label')).click();
        submitPage();
    }

    function fillPropertyValue(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-property')
        driver.findElement(By.name("value")).sendKeys('150000');
        submitPage();
    }

    it('assets overview', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/assets-in-estate')
        driver.wait(until.titleContains('Assets in the estate'), 2000)
        .then(function(){
            checkAccessibility(done)
        });
    });

    it('properties question', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-properties-buildings-land-owned')
        triggerErrorSummary(done, 'Properties')
        driver.then(function(){
            checkAccessibility(done)
        });
    });

    it('properties overview', function (done) {
        fillPropertyQuestion();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/properties-buildings-land-owned')
        driver.then(function(){
            checkAccessibility(done)
        });
    });

    it('add property overview', function (done) {
        fillPropertyQuestion();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/add-property')
        driver.then(function(){
            checkAccessibility(done)
        });
    });

    it('property address', function (done) {
        fillPropertyQuestion();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/property-address')
        triggerErrorSummary(done, 'Property address')
        driver.then(function(){
            checkAccessibility(done)
        });
    });

    it('type of property', function (done) {
        fillPropertyQuestion();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/type-of-property')
        triggerErrorSummary(done, 'Type of property')
        driver.then(function(){
            checkAccessibility(done)
        });
    });

    it('how property was owned', function (done) {
        fillPropertyQuestion();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/how-property-was-owned')
        triggerErrorSummary(done, 'How property was owned')
        driver.then(function(){
            checkAccessibility(done)
        });
    });

    it('freehold or leasehold', function (done) {
        fillPropertyQuestion();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/freehold-or-leasehold-property')
        triggerErrorSummary(done, 'Freehold or leasehold')
        driver.then(function(){
            checkAccessibility(done)
        });
    });

    it('value of property', function (done) {
        fillPropertyQuestion();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-property')
        triggerErrorSummary(done, 'Property value')
        driver.then(function(){
            checkAccessibility(done)
        });
    });

    it('delete property', function(done){
        fillPropertyQuestion();
        fillPropertyValue();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/delete-property/1')
        .then(function(){
            checkAccessibility(done)
        });
    })
});



