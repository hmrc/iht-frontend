var selenium = require('selenium-webdriver');
var AxeBuilder = require('axe-webdriverjs'); 
var By = selenium.By;
var until = selenium.until; 
var colors = require('colors'); 
var TestReporter = require('../../../spec-helpers/reporter'); 
var Browser = require('../../../spec-helpers/browser');
var accessibilityhelper = require('../../../spec-helpers/check-accessibility-helper'); 
var loginhelper = require('../../../spec-helpers/login-helper'); 
var actionHelper = require('../../../spec-helpers/action-helper'); 
var behaves = require('../../../spec-helpers/behaviour');
var Reporter = new TestReporter();  


jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000; 

jasmine.getEnv().clearReporters(); 
jasmine.getEnv().addReporter(Reporter.reporter);   

describe('Registration accessibility : ', function() { 
    var driver;  
    beforeEach(function(done) { 
        driver = Browser.startBrowser();
        loginhelper.authenticate(done, driver, 'reg') 
    });  

    afterEach(function(done) { 
        driver.quit().then(function() { 
            done(); 
        }); 
    });   




    it('registration checklist', function(done) { 
        behaves.actsAsBasicPage(done, driver, {
         url: Browser.baseUrl + '/registration/registration-checklist',
         pageTitle: "Before you start registration"
        })
    });  

    it('when did the deceased die', function(done) { 
        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/date-of-death',
          pageTitle: "Date of death",
          button: '#continue-button'
        })
    });  

    it('deceased permanent home', function(done) { 
        actionHelper.populateRegistrationData(driver, 'DateOfDeath');

        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/permanent-home-location',
          pageTitle: "Permanent home",
          button: '#continue-button'
        })
    });  

      it('deceased details', function(done) { 
        actionHelper.populateRegistrationData(driver, 'PermanentHomeLocation');

        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/deceaseds-details',
          pageTitle: "About the person who has died",
          button: '#continue-button'
        })
      });  

      it('deceased contact address', function(done) { 
        actionHelper.populateRegistrationData(driver, 'DeceasedsDetails');

        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/location-of-contact-address',
          pageTitle: "Contact address",
          button: '#continue-button'
        })
      });  

      it('deceased contact address UK', function(done) { 
        actionHelper.populateRegistrationData(driver, 'DeceasedLastAddressUK');

        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/uk-contact-address',
          pageTitle: "Contact address",
          button: '#continue-button'
        })
      });  

      it('deceased contact address outside UK', function(done) { 
        actionHelper.populateRegistrationData(driver, 'DeceasedLastAddressOutsideUK');

        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/contact-address',
          pageTitle: "Contact address",
          button: '#continue-button'
        })
      });  

      it('applying for probate', function(done) { 
        actionHelper.populateRegistrationData(driver, 'UKContactAddress');

        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/applying-for-probate',
          pageTitle: "Apply for probate",
          button: '#continue-button'
        })
      });  

      it('applying for probate location', function(done) { 
        actionHelper.populateRegistrationData(driver, 'ApplyingForProbate');

        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/probate-location',
          pageTitle: "Probate location",
          button: '#continue-button'
        })
      });  

      it('your contact details', function(done) { 
        actionHelper.populateRegistrationData(driver, 'ProbateLocation');

        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/your-contact-details',
          pageTitle: "Your contact details",
          button: '#continue-button'
        })
      });  

      it('your address in the UK', function(done) { 
        actionHelper.populateRegistrationData(driver, 'ApplicantsContact');

        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/your-uk-address',
          pageTitle: "Your address",
          button: '#continue-button'
        })
      })  

      it('your address outside the UK', function(done) { 
        actionHelper.populateRegistrationData(driver, 'YourOutsideUKAddress');

        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/your-address',
          pageTitle: "Your address",
          button: '#continue-button'
        })
      })  

      it('any other applicants', function(done) { 
        actionHelper.populateRegistrationData(driver, 'YourUKAddress');

        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/any-other-applicants',
          pageTitle: "Other probate applicants",
          button: '#continue-button'
        })
      })  

      it('other applicant details', function(done) { 
        actionHelper.populateRegistrationData(driver, 'AnyOtherApplicants');

        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/applicants-details',
          pageTitle: "Other person’s details",
          button: '#continue-button'
        })
      })  

      it('other applicant address UK', function(done) { 
        actionHelper.populateRegistrationData(driver, 'ApplicantsDetailsIndicatingAUKAddress');

        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/applicants-uk-address/1',
          pageTitle: "Other person’s address",
          button: '#continue-button'
        })
      })  

      it('other applicant address outside UK', function(done) { 
        actionHelper.populateRegistrationData(driver, 'ApplicantsDetails');

        behaves.actsAsStandardForm(done, driver, {
            url: Browser.baseUrl + '/registration/applicants-uk-address/1',
            pageTitle: "Other person’s address",
            button: '#continue-button'
        })
      })  

      it('other people applying for probate', function(done) { 
        actionHelper.populateRegistrationData(driver, 'ApplicantsDetails');

        behaves.actsAsStandardForm(done, driver, {
          url: Browser.baseUrl + '/registration/other-probate-applicants',
          pageTitle: "Other applicants",
          button: '#continue-button'

        })
      })  

      it('delete other applicant', function(done) { 
        actionHelper.populateRegistrationData(driver, 'CompleteWith2CoExecutors');

        behaves.actsAsBasicPage(done, driver, {
           url: Browser.baseUrl + '/registration/delete-applicant/1',
           pageTitle: "Delete applicant"
        });
      })  

      it('check your answers', function(done) { 
        actionHelper.populateRegistrationData(driver, 'CompleteWith2CoExecutors');

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/registration/check-your-answers',
            pageTitle: "Check your answers"
        });
      })   
});         