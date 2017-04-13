var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');

describe('Accessibility', function() {
  var driver;

  beforeEach(function(done) {
      driver = new selenium.Builder()
          .forBrowser('chrome')
          .build();

      driver.get('http://localhost:9949/auth-login-stub/gg-sign-in')
          .then(function () {
              done();
          });
  });

  // Close website after each test is run (so it is opened fresh each time)
  afterEach(function(done) {
      driver.quit().then(function () {
          done();
      });
  });

//  it('should change state with the keyboard', function() {
//    var selector = 'span[role="radio"][aria-labelledby="radiogroup-0-label-0"]';
//
//    driver.findElement(selenium.By.css(selector))
//      .then(function (element) {
//          element.sendKeys(Key.SPACE);
//          return element;
//      })
//      .then(function (element) {
//          return element.getAttribute('aria-checked')
//      })
//      .then(function (attr) {
//          expect(attr).toEqual('true');
//      });
//  });

  it('should analyze the page with aXe', function (done) {
     AxeBuilder(driver)
       .analyze(function(results) {
            console.log('Accessibility Violations: ', results.violations.length);
            if (results.violations.length > 0) {
                console.log(results.violations);
            }
            expect(results.violations.length).toBe(0);
            done();
        })
  });
});



//var AxeBuilder = require('axe-webdriverjs');
//var WebDriver = require('selenium-webdriver');
//var jasmine = require('jasmine');
//var By = WebDriver.By, until = WebDriver.until;
//
//var browser;
//
//jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
//
//describe('Estate Report', function() {
//    beforeEach(function() {
//        browser = new WebDriver.Builder()
//          .forBrowser('chrome')
//          .build();
//
////        browser.manage().timeouts().setScriptTimeout(60000);
////
////        browser.get('http://localhost:9949/auth-login-stub/gg-sign-in');
////        browser.findElement(By.name("authorityId")).sendKeys('1');
////        browser.findElement(By.name("redirectionUrl")).sendKeys('http://localhost:9070/inheritance-tax/estate-report');
////        browser.findElement(By.name("credentialStrength")).sendKeys('strong');
////        browser.findElement(By.name("confidenceLevel")).sendKeys('200');
////        browser.findElement(By.name("nino")).sendKeys('CS700100A');
////        browser.findElement(By.css('[type="submit"]')).click();
//    });
//
//    // Close the website after each test is run (so that it is opened fresh each time)
////    afterEach(function() {
////        browser.quit();
////    });
//
//    it('estate report', function(){
//        browser.get('http://localhost:9949/auth-login-stub/gg-sign-in');
//        //browser.get('http://localhost:9070/inheritance-tax/estate-report');
//        browser.wait(until.titleIs('Your estate reports - GOV.UK'), 1000);
//        browser.then(function () {
//            AxeBuilder(browser)
//              .analyze(function (results) {
//
//                console.log('Accessibility Violations: ', results.violations.length);
//                if (results.violations.length > 0) {
//                    console.log(results.violations);
//                }
//                expect(results.violations.length).toBe(0);
//                //done();
//              });
//        });
//    });
//
//
//
//});