var checkAccessibility = function(done, driver) {
    var AxeBuilder = require('axe-webdriverjs');
    AxeBuilder(driver)
    .include('#content')
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
        expect(results.violations.length).toBe(0,report);
        done();
    })

};
exports.checkAccessibility = checkAccessibility;