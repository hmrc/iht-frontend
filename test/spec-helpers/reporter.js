var TestReporter = function(){
    var rep = this;
    var Report = require('jasmine-spec-reporter').DisplayProcessor;
    Report.prototype.displaySuite = function (suite, log) {
      return log;
    };

    Report.prototype.displaySuccessfulSpec = function (spec, log) {
      return log;
    };

    Report.prototype.displayFailedSpec = function (spec, log) {
      return log;
    };

    Report.prototype.displayPendingSpec = function (spec, log) {
      return log;
    };
    var SpecReporter = require('jasmine-spec-reporter').SpecReporter;
    rep.reporter = new SpecReporter({
        customProcessors: [Report]
    });
}
module.exports = function() {
    return new TestReporter();
};