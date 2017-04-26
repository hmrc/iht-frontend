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


describe('Insurance (Assets) accessibility : ', function() {
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


    function fillInsuranceOwned(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/insurance-policies-paying-to-deceased')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("value")).sendKeys('5000');
        actionHelper.submitPageHelper(done, driver);
    }
    function fillInsuranceJointlyOwned(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/jointly-owned-insurance-policies')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("shareValue")).sendKeys('8000');
        actionHelper.submitPageHelper(done, driver);
    }
    function fillPolicyGifted(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-insurance-policies-gifted')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillPolicyGiftedValue(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-gifted-policies')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillPolicyAnnuity(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-annuities')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillPolicyInTrust(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/insurance-policies-placed-in-trust')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }


    it('insurance overview', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/insurance-policies-owned')
        driver.wait(until.titleContains('Insurance policies'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('insurance overview, filled', function (done) {
        fillInsuranceOwned(done, driver);
        fillInsuranceJointlyOwned(done, driver);
        fillPolicyGifted(done, driver);
        fillPolicyGiftedValue(done, driver);
        fillPolicyAnnuity(done, driver);
        fillPolicyInTrust(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/insurance-policies-owned')
        driver.wait(until.titleContains('Insurance policies'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('insurance policies paying to the deceased', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/insurance-policies-paying-to-deceased')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Own insurance policies')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('joint insurance policies', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/jointly-owned-insurance-policies')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Joint insurance policies')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('insurance policy gifted yes/no', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-insurance-policies-gifted')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Premiums paid for someone else')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('insurance policy gifted value', function (done) {
        fillPolicyGifted(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/value-of-gifted-policies')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Value of gifted policies')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('insurance policy annuity', function (done) {
        fillPolicyGifted(done, driver);
        fillPolicyGiftedValue(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-annuities')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Any annuities bought')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('insurance policy in trust', function (done) {
        fillPolicyGifted(done, driver);
        fillPolicyGiftedValue(done, driver);
        fillPolicyAnnuity(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/insurance-policies-placed-in-trust')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Policies placed in trust')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });


    it('insurance policies are gifts', function (done) {
        fillPolicyGifted(done, driver);
        fillPolicyGiftedValue(done, driver);
        fillPolicyAnnuity(done, driver);
        fillPolicyInTrust(done, driver);

        driver.get('http://localhost:9070/inheritance-tax/estate-report/insurance-policies-are-gifts')
        driver.wait(until.titleContains('Insurance premiums'), 2000)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });
});



