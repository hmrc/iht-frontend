var checkAccessibility = function(done, driver) {
    var AxeBuilder = require('axe-webdriverjs');
    AxeBuilder(driver)
    .include('#content')
    .analyze(function(results) {
        if (results.violations.length > 0) {
            console.log('Accessibility Violations: '.bold.bgRed.white, results.violations.length);
            results.violations.forEach(function(violation){
                console.log(violation);
                console.log('============================================================'.red);
            });
        }
        expect(results.violations.length).toBe(0);
        done();
    })

};
exports.checkAccessibility = checkAccessibility;