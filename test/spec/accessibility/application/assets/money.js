var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var Report = require('jasmine-spec-reporter').DisplayProcessor;

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();

Report.prototype.displaySuite = function (suite, log) {
  return log;
};

Report.prototype.displaySuccessfulSpec = function (spec, log) {
  return log;
};

Report.prototype.displayFailedSpec = function (spec, log) {
  return log;
};

Report.prototype.displayPendingSpec = function (spec, log) {
  return log;
};

var SpecReporter = require('jasmine-spec-reporter').SpecReporter;
var reporter = new SpecReporter({
    customProcessors: [Report]
});
jasmine.getEnv().addReporter(reporter);


fdescribe('Property Assets accessibility : ', function() {
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
            checkAccessibility(done)
        });
    });

    it('money overview, filled', function (done) {
        fillMoneyOwned();
        fillMoneyJointlyOwned();
        driver.get('http://localhost:9070/inheritance-tax/estate-report/money-owned')
        driver.wait(until.titleContains('Money'), 2000)
        .then(function(){
            checkAccessibility(done)
        });
    });

    it('money owned by deceased', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/own-money-owned')
        triggerErrorSummary(done, 'Own money owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
            checkAccessibility(done)
        });
    });

    it('money owned jointly', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/money-jointly-owned')
        triggerErrorSummary(done, 'Joint money owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
            checkAccessibility(done)
        });
    });


});


