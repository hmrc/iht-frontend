var authenticate = function(done, driver, regOrApp) {
    var redirectUrl, title;
    var selenium = require('selenium-webdriver')
    var By = selenium.By, until = selenium.until;

    if(regOrApp == 'reg'){
        redirectUrl = 'http://localhost:9070/inheritance-tax/registration/date-of-death';
        title = 'Date of death';
    }
    if(regOrApp == 'app'){
        redirectUrl = 'http://localhost:9070/inheritance-tax/estate-report';
        title = 'Your estate reports';
    }
    if(regOrApp == 'report'){
        redirectUrl = 'http://localhost:9070/inheritance-tax/estate-report/estate-overview/CS700100A000001';
        title = 'Estate overview';
    }



    driver.manage().timeouts().setScriptTimeout(60000);

    driver.get('http://localhost:9949/auth-login-stub/gg-sign-in');
    driver.executeScript("document.querySelector('[name=\"authorityId\"]').setAttribute('value', '1')");
    driver.executeScript("document.querySelector('[name=\"redirectionUrl\"]').setAttribute('value', '" + redirectUrl + "')");
    driver.findElement(By.name("credentialStrength")).sendKeys('strong');
    driver.findElement(By.name("confidenceLevel")).sendKeys('200');
    driver.executeScript("document.querySelector('[name=\"nino\"]').setAttribute('value', 'CS700100A')");

    driver.findElement(By.css('[type="submit"]')).click();
    driver.wait(until.titleContains(title), 1000)
    .then(function () {
        if(regOrApp == 'app' || regOrApp == 'report'){
            driver.get('http://localhost:9070/inheritance-tax/test-only/drop');
        }
        done();
    });
}
exports.authenticate = authenticate;