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


describe('TNRB, accessibility : ', function() {
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

    function fillDeceasedEverWidowed(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/deceased-ever-widowed')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillDateOfDeath(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/date-of-death')
        driver.executeScript("document.querySelector('[name=\"dateOfPreDeceased.day\"]').setAttribute('value', '1')");
        driver.executeScript("document.querySelector('[name=\"dateOfPreDeceased.month\"]').setAttribute('value', '12')");
        driver.executeScript("document.querySelector('[name=\"dateOfPreDeceased.year\"]').setAttribute('value', '2001')");
        actionHelper.submitPageHelper(done, driver);
    }

    function fillNameOfSpouse(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/name-of-spouse-or-civil-partner')
        driver.executeScript("document.querySelector('[name=\"firstName\"]').setAttribute('value', 'Jo')");
        driver.executeScript("document.querySelector('[name=\"lastName\"]').setAttribute('value', 'Higgins')");
        actionHelper.submitPageHelper(done, driver);
    }

    function fillDateOfMarriage(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/marriage-or-civil-partnership-date')
        driver.executeScript("document.querySelector('[name=\"dateOfMarriage.day\"]').setAttribute('value', '1')");
        driver.executeScript("document.querySelector('[name=\"dateOfMarriage.month\"]').setAttribute('value', '12')");
        driver.executeScript("document.querySelector('[name=\"dateOfMarriage.year\"]').setAttribute('value', '1990')");
        actionHelper.submitPageHelper(done, driver);
    }

    function fillPermanentHome(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/permanent-home-location')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillFullyExempt(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/fully-exempt-estate')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillJointAssets(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/joint-assets-in-estate')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillReliefClaimed(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-relief-claimed')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillBenefitFromTrust(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-trusts-in-estate')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillNonExemptGifts(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-non-exempt-gifts')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillTypeGifts(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/gift-types-given-away')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillRequired(done, driver){
        fillDeceasedEverWidowed(done, driver)
        fillDateOfDeath(done, driver)
    }

    function fillTnrb(done, driver){
        fillRequired(done, driver)
        fillNameOfSpouse(done, driver)
        fillDateOfMarriage(done, driver)
        fillPermanentHome(done, driver)
        fillFullyExempt(done, driver)
        fillJointAssets(done, driver)
        fillReliefClaimed(done, driver)
        fillBenefitFromTrust(done, driver)
        fillNonExemptGifts(done, driver)
        fillTypeGifts(done, driver)
    }


    it('tnrb overview, filled', function (done) {
        fillTnrb(done, driver)

        driver.get('http://localhost:9070/inheritance-tax/estate-report/increasing-the-threshold')
        driver.wait(until.titleContains('Increasing the threshold'), 2000)
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('threshold increased', function (done) {
        fillTnrb(done, driver)

        driver.get('http://localhost:9070/inheritance-tax/estate-report/threshold-increased')
        driver.wait(until.titleContains('Threshold increased'), 2000) .then(function() { 
           accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('guidance', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/increase-the-threshold')
        driver.wait(until.titleContains('Increasing the threshold'), 2000) 
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('deceased ever widowed', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/deceased-ever-widowed')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Increasing the Inheritance Tax threshold')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('date of death', function (done) {
        fillDeceasedEverWidowed(done, driver)

        driver.get('http://localhost:9070/inheritance-tax/estate-report/date-of-death')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Increasing the Inheritance Tax threshold')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('name of spouse', function (done) {
        fillRequired(done, driver)

        driver.get('http://localhost:9070/inheritance-tax/estate-report/name-of-spouse-or-civil-partner')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Name of the spouse')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('date of marriage', function (done) {
        fillRequired(done, driver)

        driver.get('http://localhost:9070/inheritance-tax/estate-report/marriage-or-civil-partnership-date')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Date of marriage')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('permanent home', function (done) {
        fillRequired(done, driver)

        driver.get('http://localhost:9070/inheritance-tax/estate-report/permanent-home-location')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Location of permanent home')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('fully exempt estate', function (done) {
        fillRequired(done, driver)

        driver.get('http://localhost:9070/inheritance-tax/estate-report/fully-exempt-estate')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Fully exempt estate')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('joint assets in estate', function (done) {
        fillRequired(done, driver)

        driver.get('http://localhost:9070/inheritance-tax/estate-report/joint-assets-in-estate')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Joint assets in estate')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('any relief claimed', function (done) {
        fillRequired(done, driver)

        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-relief-claimed')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Any relief claimed')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('trust benefit', function (done) {
        fillRequired(done, driver)

        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-trusts-in-estate')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Any trust benefitted')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('non-exempt gifts', function (done) {
        fillRequired(done, driver)

        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-non-exempt-gifts')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Any non-exempt gifts')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('gifts with reservation of benefit', function (done) {
        fillRequired(done, driver)

        driver.get('http://localhost:9070/inheritance-tax/estate-report/gift-types-given-away')
        actionHelper.triggerErrorSummaryHelper(done, driver, 'Type of gifts')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });



});