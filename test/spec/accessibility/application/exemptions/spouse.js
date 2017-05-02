var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../../spec-helpers/reporter.js');
var Browser = require('../../../../spec-helpers/browser.js');
var accessibilityhelper = require('../../../../spec-helpers/check-accessibility-helper.js');
var loginhelper = require('../../../../spec-helpers/login-helper.js');
var actionHelper = require('../../../../spec-helpers/action-helper.js');
var behaves = require('../../../../spec-helpers/behaviour.js');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


describe('Assets left to spouse (Exemptions), accessibility : ', function() {
    var driver;

    beforeEach(function(done) {
      driver = Browser.startBrowser();

      loginhelper.authenticate(done, driver, 'report')
    });

    // Close website after each test is run (so it is opened fresh each time)
    afterEach(function(done) {
      driver.quit().then(function () {
          done();
      });
    });

    function fillAssetsLeftToSpouse(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/any-assets-left-to-partner')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillPermenentHome(done, driver){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/partner-residence')
        driver.findElement(By.css('#yes-label')).click();
        actionHelper.submitPageHelper(done, driver);
    }

    function fillRequired(done, driver){
        fillAssetsLeftToSpouse(done, driver)
        fillPermenentHome(done, driver)
    }


    it('assets left to spouse yes/no', function (done) {
        behaves.actsAsYesNo(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/any-assets-left-to-partner',
            pageTitle: 'Any assets left to partner'
        })
    });

    it('spouse permanent home yes/no', function (done) {
        fillAssetsLeftToSpouse(done, driver)

        behaves.actsAsYesNo(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/partner-residence',
            pageTitle: "Partner’s residence"
        })
    });

    it('name of spouse', function(done){
        fillRequired(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/partners-name',
            pageTitle: "Partner’s name"
        })
    });

    it('spouse dob', function(done){
        fillRequired(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/partners-date-of-birth',
            pageTitle: "Partner’s date of birth"
        })
    });

    it('spouse nino', function(done){
        fillRequired(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/partners-national-insurance-number',
            pageTitle: "Partner’s National Insurance number"
        })
    });

    it('value of assets left to spouse', function(done){
        fillRequired(done, driver)

        behaves.actsAsStandardForm(done, driver, {
            url: 'http://localhost:9070/inheritance-tax/estate-report/assets-value-left-to-partner',
            pageTitle: "Value of assets left to partner"
        })
    });
});