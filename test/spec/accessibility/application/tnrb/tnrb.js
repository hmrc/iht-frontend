var selenium = require('selenium-webdriver');
var AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../../spec-helpers/reporter');
var Browser = require('../../../../spec-helpers/browser');
var accessibilityhelper = require('../../../../spec-helpers/check-accessibility-helper');
var loginhelper = require('../../../../spec-helpers/login-helper');
var actionHelper = require('../../../../spec-helpers/action-helper');
var behaves = require('../../../../spec-helpers/behaviour');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


describe('TNRB, accessibility : ', function() {
    var driver;

    beforeEach(function(done) {
      driver = Browser.startBrowser();

      loginhelper.authenticate(done, driver, 'report')
    });

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
        driver.get(Browser.baseUrl + '/estate-report/date-of-death')
        driver.executeScript("document.querySelector('[name=\"dateOfPreDeceased.day\"]').setAttribute('value', '1')");
        driver.executeScript("document.querySelector('[name=\"dateOfPreDeceased.month\"]').setAttribute('value', '12')");
        driver.executeScript("document.querySelector('[name=\"dateOfPreDeceased.year\"]').setAttribute('value', '2001')");
        actionHelper.submitPageHelper(done, driver);
    }

    function fillNameOfSpouse(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/name-of-spouse-or-civil-partner')
        driver.executeScript("document.querySelector('[name=\"firstName\"]').setAttribute('value', 'Jo')");
        driver.executeScript("document.querySelector('[name=\"lastName\"]').setAttribute('value', 'Higgins')");
        actionHelper.submitPageHelper(done, driver);
    }

    function fillDateOfMarriage(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/marriage-or-civil-partnership-date')
        driver.executeScript("document.querySelector('[name=\"dateOfMarriage.day\"]').setAttribute('value', '1')");
        driver.executeScript("document.querySelector('[name=\"dateOfMarriage.month\"]').setAttribute('value', '12')");
        driver.executeScript("document.querySelector('[name=\"dateOfMarriage.year\"]').setAttribute('value', '1990')");
        actionHelper.submitPageHelper(done, driver);
    }

    function fillPermanentHome(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/permanent-home-location')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillFullyExempt(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/fully-exempt-estate')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillJointAssets(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/joint-assets-in-estate')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillReliefClaimed(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/any-relief-claimed')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillBenefitFromTrust(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/any-trusts-in-estate')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillNonExemptGifts(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/any-non-exempt-gifts')
        driver.findElement(By.css('#no-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillTypeGifts(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/gift-types-given-away')
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

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/increasing-the-threshold',
            pageTitle: "Increasing the threshold"
        })
    });

    it('threshold increased', function (done) {
        fillTnrb(done, driver)

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/threshold-increased',
            pageTitle: "Threshold increased"
        })
    });

    it('guidance', function (done) {
        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/increase-the-threshold',
            pageTitle: "Increasing the threshold"
        })
    });

    it('deceased ever widowed', function (done) {
        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/deceased-ever-widowed',
            pageTitle: "Increasing the Inheritance Tax threshold"

        })
    });

    it('date of death', function (done) {
        fillDeceasedEverWidowed(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/date-of-death',
            pageTitle: "Increasing the Inheritance Tax threshold"

        })
    });

    it('name of spouse', function (done) {
        fillRequired(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/name-of-spouse-or-civil-partner',
            pageTitle: "Name of the spouse"

        })
    });

    it('date of marriage', function (done) {
        fillRequired(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/marriage-or-civil-partnership-date',
            pageTitle: "Date of marriage"

        })
    });

    it('permanent home', function (done) {
        fillRequired(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/permanent-home-location',
            pageTitle: "Location of permanent home"

        })
    });

    it('fully exempt estate', function (done) {
        fillRequired(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/fully-exempt-estate',
            pageTitle: "Fully exempt estate"

        })
    });

    it('joint assets in estate', function (done) {
        fillRequired(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/joint-assets-in-estate',
            pageTitle: "Joint assets in estate"

        })
    });

    it('any relief claimed', function (done) {
        fillRequired(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/any-relief-claimed',
            pageTitle: "Any relief claimed"

        })
    });

    it('trust benefit', function (done) {
        fillRequired(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/any-trusts-in-estate',
            pageTitle: "Any trust benefitted"

        })
    });

    it('non-exempt gifts', function (done) {
        fillRequired(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/any-non-exempt-gifts',
            pageTitle: "Any non-exempt gifts"

        })
    });

    it('gifts with reservation of benefit', function (done) {
        fillRequired(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/estate-report/gift-types-given-away',
            pageTitle: "Type of gifts"

        })
    });



});