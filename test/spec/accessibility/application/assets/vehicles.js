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


describe('Vehicles (Assets) accessibility : ', function() {
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

    function fillVehiclesOwned(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/own-vehicles-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("value")).sendKeys('5000');
        actionHelper.submitPageHelper(done, driver);
    }
    function fillVehiclesJointlyOwned(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/motor-vehicles-jointly-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("shareValue")).sendKeys('8000');
        actionHelper.submitPageHelper(done, driver);
    }

    it('vehicles overview', function (done) {
        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/motor-vehicles-owned',
            pageTitle: "Motor vehicles"

        })
    });

    it('vehicles overview, filled', function (done) {
        fillVehiclesOwned(done, driver);
        fillVehiclesJointlyOwned(done, driver);

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/motor-vehicles-owned',
            pageTitle: "Motor vehicles"

        })
    });

    it('vehicles owned by deceased', function (done) {
        behaves.actsAsYesNo(done, driver, {
            url: Browser.baseUrl + '/estate-report/own-vehicles-owned',
            pageTitle: "Motor vehicles owned"

        })
    });

    it('vehicles owned jointly', function (done) {
        behaves.actsAsYesNo(done, driver, {
            url: Browser.baseUrl + '/estate-report/motor-vehicles-jointly-owned',
            pageTitle: "Motor vehicles jointly owned"

        })
    });


});



