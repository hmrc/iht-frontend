var selenium = require('selenium-webdriver')
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var checkAccessibility = function(done, driver) {

    var tags;

    // check if wcag false positives are present, if so switch to section508
    driver.findElements(selenium.By.css("[data-exclude]"))  
    .then(function(elements) {  
        if(elements.length > 0){ 
            tags = ['section508', 'best-practice']
        } else { 
            tags = ['wcag2a', 'wcag2aa', 'best-practice']
        }  
    })


    var AxeBuilder = require('axe-webdriverjs');
    driver.then(function(){
        AxeBuilder(driver)
        .withTags(tags)
        .include('#content')
        //.exclude('[data-exclude="true"]')
        .analyze(function(results) {
            var report = "";
            if (results.violations.length > 0) {
                results.violations.forEach(function(violation){
                    //console.log(violation)
                    report = report + " " + violation.help
                     violation.nodes.forEach(function(node){
                        report = report + "\n    " + node.html;
                     })
                });
            }

            expect(results.violations.length).toBe(0, report);
            //console.log(colors.yellow("     " + tags))
        })
        done();
    })
};
exports.checkAccessibility = checkAccessibility;