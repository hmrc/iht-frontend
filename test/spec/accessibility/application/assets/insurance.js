var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../../spec-helpers/reporter.js');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


describe('Insurance (Assets) accessibility : ', function() {
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
                console.log('      ','Accessibility Violations: '.bold.bgRed.white, results.violations.length);
                results.violations.forEach(function(violation){
                    console.log('      ', violation);
                    console.log('      ============================================================'.red);
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


    function fillInsuranceOwned(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/insurance-policies-paying-to-deceased')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("value")).sendKeys('5000');
        submitPage();
    }
    function fillInsuranceJointlyOwned(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/jointly-owned-insurance-policies')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("shareValue")).sendKeys('8000');
        submitPage();
    }
    function fillPolicyGifted(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-insurance-policies-gifted')
        driver.findElement(By.css('#yes-label')).click();
        submitPage();
    }

    function fillPolicyGiftedValue(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-gifted-policies')
        driver.findElement(By.css('#no-label')).click();
        submitPage();
    }

    function fillPolicyAnnuity(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-annuities')
        driver.findElement(By.css('#no-label')).click();
        submitPage();
    }

    function fillPolicyInTrust(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/insurance-policies-placed-in-trust')
        driver.findElement(By.css('#no-label')).click();
        submitPage();
    }


    it('insurance overview', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/insurance-policies-owned')
        driver.wait(until.titleContains('Insurance policies'), 2000)
        .then(function(){
            checkAccessibility(done)
        });
    });

    it('insurance overview, filled', function (done) {
        fillInsuranceOwned();
        fillInsuranceJointlyOwned();
        fillPolicyGifted();
        fillPolicyGiftedValue();
        fillPolicyAnnuity();
        fillPolicyInTrust();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/insurance-policies-owned')
        driver.wait(until.titleContains('Insurance policies'), 2000)
        .then(function(){
            checkAccessibility(done)
        });
    });

    it('insurance policies paying to the deceased', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/insurance-policies-paying-to-deceased')
        triggerErrorSummary(done, 'Own insurance policies')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
            checkAccessibility(done)
        });
    });

    it('joint insurance policies', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/jointly-owned-insurance-policies')
        triggerErrorSummary(done, 'Joint insurance policies')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
            checkAccessibility(done)
        });
    });

    it('insurance policy gifted yes/no', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-insurance-policies-gifted')
        triggerErrorSummary(done, 'Premiums paid for someone else')
        driver.then(function(){
            checkAccessibility(done)
        });
    });

    it('insurance policy gifted value', function (done) {
        fillPolicyGifted();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-gifted-policies')
        triggerErrorSummary(done, 'Value of gifted policies')
        driver.then(function(){
            checkAccessibility(done)
        });
    });

    it('insurance policy annuity', function (done) {
        fillPolicyGifted();
        fillPolicyGiftedValue();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-annuities')
        triggerErrorSummary(done, 'Any annuities bought')
        driver.then(function(){
            checkAccessibility(done)
        });
    });

    it('insurance policy in trust', function (done) {
        fillPolicyGifted();
        fillPolicyGiftedValue();
        fillPolicyAnnuity();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/insurance-policies-placed-in-trust')
        triggerErrorSummary(done, 'Policies placed in trust')
        driver.then(function(){
            checkAccessibility(done)
        });
    });


    it('insurance policies are gifts', function (done) {
        fillPolicyGifted();
        fillPolicyGiftedValue();
        fillPolicyAnnuity();
        fillPolicyInTrust();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/insurance-policies-are-gifts')
        driver.wait(until.titleContains('Insurance premiums'), 2000)
        driver.then(function(){
            checkAccessibility(done)
        });
    });
});



