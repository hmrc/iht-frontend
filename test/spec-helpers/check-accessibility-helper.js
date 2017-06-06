var selenium = require('selenium-webdriver')
var By = selenium.By, until = selenium.until;

var checkAccessibility = function(done, driver) {
    var AxeBuilder = require('axe-webdriverjs');
    AxeBuilder(driver)
    .include('#content')
    .exclude('[data-exclude="true"]')
    .analyze(function(results) {
        var report = "";
        if (results.violations.length > 0) {
            results.violations.forEach(function(violation){
                report = report + " " + violation.help
                 violation.nodes.forEach(function(node){
                    report = report + "\n    " + node.html;
                 })
            });
        }
        var addendum = "";
              driver.findElements(selenium.By.css("[data-exclude]")) .then(function(elements) { 
                  if(elements.length > 0){
                      addendum = "This page contains a skipped component";
                  } 
              })
        expect(results.violations.length).toBe(0,report, "extra info");
        done();
    })

};
exports.checkAccessibility = checkAccessibility;