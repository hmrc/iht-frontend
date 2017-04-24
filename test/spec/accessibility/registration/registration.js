var selenium = require('selenium-webdriver'),
    AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../spec-helpers/reporter.js');
var accessibilityhelper = require('../../../spec-helpers/check-accessibility-helper.js');
var loginhelper = require('../../../spec-helpers/login-helper.js');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


describe('Registration accessibility : ', function() {
    var driver;

    beforeEach(function(done) {
      driver = new selenium.Builder()
          .forBrowser('chrome')
          .build();

        loginhelper.authenticate(done, driver, 'reg')
    });

    // Close website after each test is run (so it is opened fresh each time)
    afterEach(function(done) {
      driver.quit().then(function () {
          done();
      });
    });
    
    function submitPage(done){
        driver.findElement(By.css('#continue-button')).click();
    }

    function fillDateOfDeath(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/date-of-death')
        driver.findElement(By.name("dateOfDeath.day")).sendKeys('1');
        driver.findElement(By.name("dateOfDeath.month")).sendKeys('12');
        driver.findElement(By.name("dateOfDeath.year")).sendKeys('2016');
        submitPage();
    }

    function fillPermanentHome(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/permanent-home-location')
        driver.findElement(By.css("#domicile-england_or_wales")).click();
        submitPage();
    }

    function fillDeceasedDetails(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/deceaseds-details')
        driver.findElement(By.name("firstName")).sendKeys('Harriet');
        driver.findElement(By.name("lastName")).sendKeys('McDonald');
        driver.findElement(By.name("dateOfBirth.day")).sendKeys('1');
        driver.findElement(By.name("dateOfBirth.month")).sendKeys('12');
        driver.findElement(By.name("dateOfBirth.year")).sendKeys('1980');
        driver.findElement(By.name("nino")).sendKeys('QQ123456');
        driver.findElement(By.css('#maritalStatus-married_or_in_civil_partnership-label')).click();
        submitPage();
    }

    function fillDeceasedLastAddressUK(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/location-of-contact-address')
        driver.findElement(By.css("#yes-label")).click();
        submitPage();
    }

    function fillDeceasedLastAddressOutsideUK(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/location-of-contact-address')
        driver.findElement(By.css("#no-label")).click();
        submitPage();
    }

    function fillLastContactUK(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/uk-contact-address');
        driver.findElement(By.name("ukAddress.addressLine1")).sendKeys('10 Downing Street');
        driver.findElement(By.name("ukAddress.addressLine2")).sendKeys('London');
        driver.findElement(By.name("ukAddress.postCode")).sendKeys('NE12 3ER');
        submitPage();
    }

    function fillLastContactOutsideUK(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/contact-address');
        driver.findElement(By.name("ukAddress.addressLine1")).sendKeys('8 Rue de la Concorde');
        driver.findElement(By.name("ukAddress.ukAddressLine2")).sendKeys('St Germain');
        driver.findElement(By.name("ukAddress.addressLine3")).sendKeys('Paris');
        driver.findElement(By.name("iht-auto-complete")).sendKeys('Fr');

        driver.findElements(selenium.By.css(".suggestion"))
        .then(function (elements) {
        expect(elements.length).toEqual(4);
        });

        driver.findElement(By.css('#iht-suggestions-list li:first-child')).click();
        submitPage();
    }

    function fillApplyingForProbate(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/applying-for-probate');
        driver.findElement(By.css('#yes-label')).click();
        submitPage();
    }

    function fillApplyingForProbateLocation(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/probate-location');
        driver.findElement(By.css('#country-england_or_wales-label')).click();
        submitPage();
    }

    function fillYourContactDetailsUK(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/your-contact-details');
        driver.findElement(By.name("phoneNo")).sendKeys('01234 567 789');
        driver.findElement(By.css('#yes-label')).click();
        submitPage();
    }

    function fillYourAddressUK(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/your-uk-address');
        driver.findElement(By.name("ukAddressLine1")).sendKeys('10 Downing Street');
        driver.findElement(By.name("ukAddressLine2")).sendKeys('London');
        driver.findElement(By.name("postCode")).sendKeys('NE12 3ER');
        submitPage();
    }

    function fillYourContactDetailsOutsideUK(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/your-contact-details');
        driver.findElement(By.name("phoneNo")).sendKeys('01234 567 789');
        driver.findElement(By.css('#no-label')).click();
        submitPage();
    }

    function fillYourAddressOutsideUK(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/your-address');
        driver.findElement(By.name("ukAddressLine1")).sendKeys('8 Rue de la Concorde');
        driver.findElement(By.name("ukAddressLine2")).sendKeys('St Germain');
        driver.findElement(By.name("ukAddressLine3")).sendKeys('Paris');
        driver.findElement(By.name("iht-auto-complete")).sendKeys('Fr');

        driver.findElements(selenium.By.css(".suggestion"))
        .then(function (elements) {
        expect(elements.length).toEqual(4);
        });

        driver.findElement(By.css('#iht-suggestions-list li:first-child')).click();
        submitPage();
    }

    function fillAnyOtherApplicants(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/any-other-applicants');
        driver.findElement(By.css('#yes-label')).click();
        submitPage();
    }

    function fillOtherPersonApplyingForProbate(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/applicants-details');
        driver.findElement(By.name("firstName")).sendKeys('Peter');
        driver.findElement(By.name("lastName")).sendKeys('Kingsman');
        driver.findElement(By.name("dateOfBirth.day")).sendKeys('28');
        driver.findElement(By.name("dateOfBirth.month")).sendKeys('12');
        driver.findElement(By.name("dateOfBirth.year")).sendKeys('1980');
        driver.findElement(By.name("nino")).sendKeys('QQ123456');
        driver.findElement(By.name("phoneNo")).sendKeys('0181 152 456');
    }

    function fillOtherPersonApplyingForProbateUK(done){
        fillOtherPersonApplyingForProbate();
        driver.findElement(By.css('#yes-label')).click();
        submitPage();
    }

    function fillOtherPersonApplyingForProbateOutsideUK(done){
        fillOtherPersonApplyingForProbate();
        driver.findElement(By.css('#yes-label')).click();
        submitPage();
    }

    function fillApplicantAddressUK(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/applicants-uk-address/1');
        driver.findElement(By.name("ukAddressLine1")).sendKeys('10 Downing Street');
        driver.findElement(By.name("ukAddressLine2")).sendKeys('London');
        driver.findElement(By.name("postCode")).sendKeys('NE12 3ER');
        submitPage();
    }

    function fillApplicantAddressOutsideUK(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/applicants-address/1');
        driver.findElement(By.name("ukAddressLine1")).sendKeys('8 Rue de la Concorde');
        driver.findElement(By.name("ukAddressLine2")).sendKeys('St Germain');
        driver.findElement(By.name("ukAddressLine3")).sendKeys('Paris');
        driver.findElement(By.name("iht-auto-complete")).sendKeys('Fr');

        driver.findElements(selenium.By.css(".suggestion"))
        .then(function (elements) {
        expect(elements.length).toEqual(4);
        });

        driver.findElement(By.css('#iht-suggestions-list li:first-child')).click();
        submitPage();
    }

    function fillOtherPeopleApplyingForProbate(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/other-probate-applicants');
        driver.findElement(By.css('#no-label')).click();
        submitPage();
    }

    function gotoDeleteOtherApplicant(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/other-probate-applicants');
        driver.findElement(By.css('#delete-executor-1')).click();
    }

    function gotoCheckYourAnswers(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/check-your-answers');

    }

    function gotoConfirmDetails(done){
        driver.get('http://localhost:9070/inheritance-tax/registration/check-your-answers');
        driver.findElement(By.css('[type="submit"]')).click();
    }

    function triggerErrorSummary(done, title){
        driver.wait(until.titleContains(title), 2000)
        submitPage();
        driver.wait(until.titleContains(title), 2000)
    }

    it('registration checklist', function (done) {
        driver.get('http://localhost:9070/inheritance-tax/registration/registration-checklist')
        driver.wait(until.titleContains('Before you start registration'), 2000)
        .then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('when did the deceased die', function (done) {
        triggerErrorSummary(done, 'Date of death')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('deceased permanent home', function (done) {
        fillDateOfDeath();

        driver.get('http://localhost:9070/inheritance-tax/registration/permanent-home-location')
        triggerErrorSummary(done, 'Permanent home')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('deceased details', function (done) {
        fillDateOfDeath();
        fillPermanentHome();

        driver.get('http://localhost:9070/inheritance-tax/registration/deceaseds-details')
        triggerErrorSummary(done, 'About the deceased')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('deceased contact address', function (done) {
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();

        driver.get('http://localhost:9070/inheritance-tax/registration/location-of-contact-address')
        triggerErrorSummary(done, 'Contact address')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('deceased contact address UK', function (done) {
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();
        fillDeceasedLastAddressUK();

        driver.get('http://localhost:9070/inheritance-tax/registration/uk-contact-address')
        triggerErrorSummary(done, 'Contact address')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('deceased contact address outside UK', function (done) {
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();
        fillDeceasedLastAddressOutsideUK();

        driver.get('http://localhost:9070/inheritance-tax/registration/contact-address')
        triggerErrorSummary(done, 'Contact address')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('applying for probate', function (done) {
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();
        fillDeceasedLastAddressOutsideUK();
        fillLastContactOutsideUK();

        driver.get('http://localhost:9070/inheritance-tax/registration/applying-for-probate')
        triggerErrorSummary(done, 'Apply for probate')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('applying for probate location', function(done){
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();
        fillDeceasedLastAddressOutsideUK();
        fillLastContactOutsideUK();
        fillApplyingForProbate();

        driver.get('http://localhost:9070/inheritance-tax/registration/probate-location')
        triggerErrorSummary(done, 'Probate location')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('your contact details', function(done){
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();
        fillDeceasedLastAddressOutsideUK();
        fillLastContactOutsideUK();
        fillApplyingForProbate();
        fillApplyingForProbateLocation();

        driver.get('http://localhost:9070/inheritance-tax/registration/your-contact-details')
        triggerErrorSummary(done, 'Your contact details')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    });

    it('your address in the UK', function(done){
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();
        fillDeceasedLastAddressOutsideUK();
        fillLastContactOutsideUK();
        fillApplyingForProbate();
        fillApplyingForProbateLocation();
        fillYourContactDetailsUK();

        driver.get('http://localhost:9070/inheritance-tax/registration/your-uk-address')
        triggerErrorSummary(done, 'Your address')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });

    })

    it('your address outside the UK', function(done){
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();
        fillDeceasedLastAddressOutsideUK();
        fillLastContactOutsideUK();
        fillApplyingForProbate();
        fillApplyingForProbateLocation();
        fillYourContactDetailsOutsideUK();

        driver.get('http://localhost:9070/inheritance-tax/registration/your-address')
        triggerErrorSummary(done, 'Your address')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });

    })

    it('any other applicants', function(done){
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();
        fillDeceasedLastAddressOutsideUK();
        fillLastContactOutsideUK();
        fillApplyingForProbate();
        fillApplyingForProbateLocation();
        fillYourContactDetailsOutsideUK();
        fillYourAddressOutsideUK();

        driver.get('http://localhost:9070/inheritance-tax/registration/any-other-applicants')
        triggerErrorSummary(done, 'Other probate applicants')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    })

    it('other applicant details', function(done){
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();
        fillDeceasedLastAddressOutsideUK();
        fillLastContactOutsideUK();
        fillApplyingForProbate();
        fillApplyingForProbateLocation();
        fillYourContactDetailsOutsideUK();
        fillYourAddressOutsideUK();
        fillAnyOtherApplicants();

        driver.get('http://localhost:9070/inheritance-tax/registration/applicants-details')
        triggerErrorSummary(done, 'Other person’s details')
        driver.then(function(){
          accessibilityhelper.checkAccessibility(done, driver)
        });
    })

    it('other applicant address UK', function(done){
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();
        fillDeceasedLastAddressOutsideUK();
        fillLastContactOutsideUK();
        fillApplyingForProbate();
        fillApplyingForProbateLocation();
        fillYourContactDetailsOutsideUK();
        fillYourAddressOutsideUK();
        fillAnyOtherApplicants();
        fillOtherPersonApplyingForProbateUK();

        driver.get('http://localhost:9070/inheritance-tax/registration/applicants-uk-address/1')
        triggerErrorSummary(done, 'Other person’s address')
        driver.then(function(){
          accessibilityhelper.checkAccessibility(done, driver)
        });
    })

    it('other applicant address outside UK', function(done){
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();
        fillDeceasedLastAddressOutsideUK();
        fillLastContactOutsideUK();
        fillApplyingForProbate();
        fillApplyingForProbateLocation();
        fillYourContactDetailsOutsideUK();
        fillYourAddressOutsideUK();
        fillAnyOtherApplicants();
        fillOtherPersonApplyingForProbateOutsideUK();

        driver.get('http://localhost:9070/inheritance-tax/registration/applicants-uk-address/1')
        triggerErrorSummary(done, 'Other person’s address')
        driver.then(function(){
          accessibilityhelper.checkAccessibility(done, driver)
        });
    })

    it('other people applying for probate', function(done){
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();
        fillDeceasedLastAddressOutsideUK();
        fillLastContactOutsideUK();
        fillApplyingForProbate();
        fillApplyingForProbateLocation();
        fillYourContactDetailsOutsideUK();
        fillYourAddressOutsideUK();
        fillAnyOtherApplicants();
        fillOtherPersonApplyingForProbateOutsideUK();
        fillApplicantAddressOutsideUK();

        driver.get('http://localhost:9070/inheritance-tax/registration/other-probate-applicants')
        triggerErrorSummary(done, 'Other applicants')
        driver.then(function(){
            accessibilityhelper.checkAccessibility(done, driver)
        });
    })

    it('delete other applicant', function(done){
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();
        fillDeceasedLastAddressOutsideUK();
        fillLastContactOutsideUK();
        fillApplyingForProbate();
        fillApplyingForProbateLocation();
        fillYourContactDetailsOutsideUK();
        fillYourAddressOutsideUK();
        fillAnyOtherApplicants();
        fillOtherPersonApplyingForProbateOutsideUK();
        fillApplicantAddressOutsideUK();
        gotoDeleteOtherApplicant();

        driver.get('http://localhost:9070/inheritance-tax/registration/delete-applicant/1')
        driver.wait(until.titleContains("Delete applicant"), 2000)
        .then(function(){
        accessibilityhelper.checkAccessibility(done, driver)
        });
    })

    it('check your answers', function(done){
        fillDateOfDeath();
        fillPermanentHome();
        fillDeceasedDetails();
        fillDeceasedLastAddressOutsideUK();
        fillLastContactOutsideUK();
        fillApplyingForProbate();
        fillApplyingForProbateLocation();
        fillYourContactDetailsOutsideUK();
        fillYourAddressOutsideUK();
        fillAnyOtherApplicants();
        fillOtherPersonApplyingForProbateOutsideUK();
        fillApplicantAddressOutsideUK();
        fillOtherPeopleApplyingForProbate();
        gotoCheckYourAnswers();

        driver.get('http://localhost:9070/inheritance-tax/registration/check-your-answers')
        driver.wait(until.titleContains("Check your answers"), 2000)
        .then(function(){
        accessibilityhelper.checkAccessibility(done, driver)
        });
    })

//it('confirm details', function(done){
//        fillDateOfDeath();
//      fillPermanentHome();
//      fillDeceasedDetails();
//      fillDeceasedLastAddressOutsideUK();
//      fillLastContactOutsideUK();
//      fillApplyingForProbate();
//      fillApplyingForProbateLocation();
//      fillYourContactDetailsOutsideUK();
//      fillYourAddressOutsideUK();
//      fillAnyOtherApplicants();
//      fillOtherPersonApplyingForProbateOutsideUK();
//      fillApplicantAddressOutsideUK();
//      fillOtherPeopleApplyingForProbate();
//      gotoCheckYourAnswers();
//      gotoConfirmDetails();
//
//      driver.get('http://localhost:9070/inheritance-tax/registration/complete')
//        driver.wait(until.titleContains("Check your answers"), 2000)
//        .then(function(){
//            accessibilityhelper.checkAccessibility(done, driver)
//        });
//    })

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