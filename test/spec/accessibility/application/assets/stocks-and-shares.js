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

describe('Stocks and Shares (Assets) accessibility : ', function() {
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


    function fillStocksOwned(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/listed-stocks-and-shares-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("valueListed")).sendKeys('5000');
        submitPage();
    }
    function fillStocksJointlyOwned(done){
        driver.get('http://localhost:9070/inheritance-tax/estate-report/unlisted-stocks-and-shares-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("valueNotListed")).sendKeys('8000');
        submitPage();
    }


    it('stocks and shares overview', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/stocks-and-shares-owned')
        driver.wait(until.titleContains('Stocks and shares'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('stocks and shares overview, filled', function (done) {
        fillStocksOwned();
        fillStocksJointlyOwned();

        driver.get('http://localhost:9070/inheritance-tax/estate-report/stocks-and-shares-owned')
        driver.wait(until.titleContains('Stocks and shares'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('stocks and shares listed on an exchange', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/listed-stocks-and-shares-owned')
        triggerErrorSummary(done, 'Listed stocks and shares')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('stocks and shares not listed on an exchange', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/estate-report/unlisted-stocks-and-shares-owned')
        triggerErrorSummary(done, 'Unlisted stocks and shares')
        driver.findElement(By.css('#yes-label')).click();
        driver.then(function(){
           accessibilityhelper.checkAccessibility(done, driver)
        });
    });


});



