var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;


describe('Registration accessibility', function() {
  var driver;

  beforeEach(function(done) {
      driver = new selenium.Builder()
          .forBrowser('chrome')
          .build();

      driver.manage().timeouts().setScriptTimeout(60000);

      driver.get('http://localhost:9949/auth-login-stub/gg-sign-in');
      driver.findElement(By.name("authorityId")).sendKeys('1');
      driver.findElement(By.name("redirectionUrl")).sendKeys('http://localhost:9070/inheritance-tax/registration/registration-checklist');
      driver.findElement(By.name("credentialStrength")).sendKeys('strong');
      driver.findElement(By.name("confidenceLevel")).sendKeys('200');
      driver.findElement(By.name("nino")).sendKeys('CS700100A');
      driver.findElement(By.css('[type="submit"]')).click();
      driver.wait(until.titleContains('Before you start registration'), 1000);
      driver.findElement(By.css('#start-registration')).click();
      driver.wait(until.titleContains('Date of death'), 1000)
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

  function checkAccessibility(done) {
        AxeBuilder(driver)
         .include('#content')
         .analyze(function(results) {
              console.log('Accessibility Violations: '.bold.bgYellow.black, results.violations.length);
              if (results.violations.length > 0) {
                  results.violations.forEach(function(violation){
                      console.log(violation);
                      console.log('============================================================'.yellow);
                  });
              }
              expect(results.violations.length).toBe(0);
              done();
       })

  }

  function fillDateOfDeath(done){
    driver.get('http://localhost:9070/inheritance-tax/registration/date-of-death')
    driver.findElement(By.name("dateOfDeath.day")).sendKeys('1');
    driver.findElement(By.name("dateOfDeath.month")).sendKeys('12');
    driver.findElement(By.name("dateOfDeath.year")).sendKeys('2016');
    driver.findElement(By.css('#continue-button')).click();
  }

  function fillPermanentHome(done){
    driver.get('http://localhost:9070/inheritance-tax/registration/permanent-home-location')
    driver.findElement(By.css("#domicile-england_or_wales")).click();
    driver.findElement(By.css('#continue-button')).click();
  }


    it('registration checklist', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/registration/registration-checklist')
        driver.wait(until.titleContains('Before you start registration'), 1000)
        .then(function(){
            checkAccessibility(done)
        });
    });

    it('when did the deceased die', function (done) {
        driver.wait(until.titleContains('Date of death'), 1000)
        .then(function(){
            checkAccessibility(done)
        });
    });

    it('deceased permanent home', function (done) {
        fillDateOfDeath();

        driver.get('http://localhost:9070/inheritance-tax/registration/permanent-home-location')
        driver.wait(until.titleContains('Permanent home'), 1000)
        .then(function(){
            checkAccessibility(done)
        });
    });

    it('deceased details', function (done) {
        fillDateOfDeath();
        fillPermanentHome();

        driver.get('http://localhost:9070/inheritance-tax/registration/deceaseds-details')
        driver.wait(until.titleContains('About the deceased'), 1000)
        .then(function(){
            checkAccessibility(done)
        });
    });
//
//    it('deceased contact address', function (done) {
//        driver.get('http://localhost:9070/inheritance-tax/registration/location-of-contact-address')
//        driver.wait(until.titleContains('Contact address'), 1000)
//        .then(function(){
//            checkAccessibility(done)
//        });
//    });

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