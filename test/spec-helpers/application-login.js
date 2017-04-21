(function(){
    var AppLogin = function(driver, selenium){
        var app = this;
        var By = selenium.By, until = selenium.until;

        app.login = function(driver){
            console.log(driver)

            driver.manage().timeouts().setScriptTimeout(60000);

            driver.get('http://localhost:9949/auth-login-stub/gg-sign-in');
            driver.findElement(By.name("authorityId")).sendKeys('1');
            driver.findElement(By.name("redirectionUrl")).sendKeys('http://localhost:9070/inheritance-tax/estate-report');
            driver.findElement(By.name("credentialStrength")).sendKeys('strong');
            driver.findElement(By.name("confidenceLevel")).sendKeys('200');
            driver.findElement(By.name("nino")).sendKeys('CS700100A');
            driver.findElement(By.css('[type="submit"]')).click();
            driver.wait(until.titleContains('Your estate reports'), 1000)
              .then(function () {
                  rep.driver.get('http://localhost:9070/inheritance-tax/test-only/drop');
                  done();
              });
return driver;

         }
    }
    module.exports = function() {
        return new AppLogin();
      };
}());