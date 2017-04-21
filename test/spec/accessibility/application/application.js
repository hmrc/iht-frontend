var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
//var Login = require('../../../spec-helpers/application-login.js');
var TestReporter = require('../../../spec-helpers/reporter.js');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


fdescribe('Application accessibility : ', function() {
    var driver;

    beforeEach(function(done) {


      driver = new selenium.Builder()
          .forBrowser('chrome')
          .build();

//          var appLogin = new Login(driver, selenium);
//          var run = appLogin.login();

      driver.manage().timeouts().setScriptTimeout(60000);

      driver.get('http://localhost:9949/auth-login-stub/gg-sign-in');
      driver.findElement(By.name("authorityId")).sendKeys('1');
      driver.findElement(By.name("redirectionUrl")).sendKeys('http://localhost:9070/inheritance-tax/estate-report');
      driver.findElement(By.name("credentialStrength")).sendKeys('strong');
      driver.findElement(By.name("confidenceLevel")).sendKeys('200');
      driver.findElement(By.name("nino")).sendKeys('CS700100A');
      driver.findElement(By.css('[type="submit"]')).click();
      driver.wait(until.titleContains('Your estate reports'), 1000)
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

    function submitPage(done){
        driver.findElement(By.css('#continue-button')).click();
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

    function triggerErrorSummary(done, title){
        driver.wait(until.titleContains(title), 2000)
        submitPage();
        driver.wait(until.titleContains(title), 2000)
    }

//    function fillDateOfDeath(done){
//        driver.get('http://localhost:9070/inheritance-tax/registration/date-of-death')
//        triggerErrorSummary(done, 'Date of death')
//            driver.findElement(By.name("dateOfDeath.day")).sendKeys('1');
//        driver.findElement(By.name("dateOfDeath.month")).sendKeys('12');
//        driver.findElement(By.name("dateOfDeath.year")).sendKeys('2016');
//        submitPage();
//    }




    it('estate report', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report')
        driver.wait(until.titleContains('Your estate reports'), 2000)
        .then(function(){
            checkAccessibility(done)
        });
    });

    it('estate report overview', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report')
        driver.findElement(By.css("table a:first-of-type")).click();
        driver.wait(until.titleContains('Estate overview'), 2000)
        driver.then(function(){
            checkAccessibility(done)
        });
    });



});



