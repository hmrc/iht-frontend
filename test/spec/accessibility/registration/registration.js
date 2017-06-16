var selenium = require('selenium-webdriver'),
     
    AxeBuilder = require('axe-webdriverjs'); 
var By = selenium.By,
    until = selenium.until; 
var colors = require('colors'); 
var TestReporter = require('../../../spec-helpers/reporter.js'); 
var Browser = require('../../../spec-helpers/browser.js');
var accessibilityhelper = require('../../../spec-helpers/check-accessibility-helper.js'); 
var loginhelper = require('../../../spec-helpers/login-helper.js'); 
var actionHelper = require('../../../spec-helpers/action-helper.js'); 
var behaves = require('../../../spec-helpers/behaviour.js');
var Reporter = new TestReporter();  


jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000; 

jasmine.getEnv().clearReporters(); 
jasmine.getEnv().addReporter(Reporter.reporter);   

fdescribe('Registration accessibility : ', function() { 
            var driver;  
            beforeEach(function(done) { 
                driver = Browser.startBrowser();
                loginhelper.authenticate(done, driver, 'reg') 
            });  

            // Close website after each test is run (so it is opened fresh each time) 

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
                driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                var data = require('../../../spec-json/registration/DateOfDeath');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                behaves.actsAsStandardForm(done, driver, {
                  url: Browser.baseUrl + '/registration/permanent-home-location',
                  pageTitle: "Permanent home",
                  button: '#continue-button'

                })

            });  

              it('deceased details', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/PermanentHomeLocation');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                  behaves.actsAsStandardForm(done, driver, {
                      url: Browser.baseUrl + '/registration/deceaseds-details',
                      pageTitle: "About the person who has died",
                      button: '#continue-button'

                  })
              });  

              it('deceased contact address', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/DeceasedsDetails');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                  behaves.actsAsStandardForm(done, driver, {
                      url: Browser.baseUrl + '/registration/location-of-contact-address',
                      pageTitle: "Contact address",
                      button: '#continue-button'

                  })
              });  

              it('deceased contact address UK', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/DeceasedLastAddressUK');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                  behaves.actsAsStandardForm(done, driver, {
                      url: Browser.baseUrl + '/registration/uk-contact-address',
                      pageTitle: "Contact address",
                      button: '#continue-button'

                  })
              });  

              it('deceased contact address outside UK', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/DeceasedLastAddressOutsideUK');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                  behaves.actsAsStandardForm(done, driver, {
                      url: Browser.baseUrl + '/registration/contact-address',
                      pageTitle: "Contact address",
                      button: '#continue-button'

                  })
              });  

              it('applying for probate', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/UKContactAddress');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                  behaves.actsAsStandardForm(done, driver, {
                      url: Browser.baseUrl + '/registration/applying-for-probate',
                      pageTitle: "Apply for probate",
                      button: '#continue-button'

                  })
              });  

              it('applying for probate location', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/ApplyingForProbate');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                  behaves.actsAsStandardForm(done, driver, {
                      url: Browser.baseUrl + '/registration/probate-location',
                      pageTitle: "Probate location",
                      button: '#continue-button'

                  })
              });  
              it('your contact details', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/ProbateLocation');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                  behaves.actsAsStandardForm(done, driver, {
                      url: Browser.baseUrl + '/registration/your-contact-details',
                      pageTitle: "Your contact details",
                      button: '#continue-button'

                  })
              });  

              it('your address in the UK', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/ApplicantsContact');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                  behaves.actsAsStandardForm(done, driver, {
                      url: Browser.baseUrl + '/registration/your-uk-address',
                      pageTitle: "Your address",
                      button: '#continue-button'

                  })
              })  

              it('your address outside the UK', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/YourOutsideUKAddress');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                  behaves.actsAsStandardForm(done, driver, {
                      url: Browser.baseUrl + '/registration/your-address',
                      pageTitle: "Your address",
                      button: '#continue-button'

                  })
              })  

              it('any other applicants', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/YourUKAddress');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                  behaves.actsAsStandardForm(done, driver, {
                      url: Browser.baseUrl + '/registration/any-other-applicants',
                      pageTitle: "Other probate applicants",
                      button: '#continue-button'

                  })
              })  

              it('other applicant details', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/AnyOtherApplicants');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                  behaves.actsAsStandardForm(done, driver, {
                      url: Browser.baseUrl + '/registration/applicants-details',
                      pageTitle: "Other person’s details",
                      button: '#continue-button'

                  })
              })  

              it('other applicant address UK', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/ApplicantsDetailsIndicatingAUKAddress');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                  behaves.actsAsStandardForm(done, driver, {
                      url: Browser.baseUrl + '/registration/applicants-uk-address/1',
                      pageTitle: "Other person’s address",
                      button: '#continue-button'

                  })
              })  

              it('other applicant address outside UK', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/ApplicantsDetails');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();


                    behaves.actsAsStandardForm(done, driver, {
                        url: Browser.baseUrl + '/registration/applicants-uk-address/1',
                        pageTitle: "Other person’s address",
                        button: '#continue-button'

                    })
              })  

              it('other people applying for probate', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/ApplicantsDetails');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                  behaves.actsAsStandardForm(done, driver, {
                      url: Browser.baseUrl + '/registration/other-probate-applicants',
                      pageTitle: "Other applicants",
                      button: '#continue-button'

                  })
              })  

              it('delete other applicant', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/CompleteWith2CoExecutors');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                   behaves.actsAsBasicPage(done, driver, {
                       url: Browser.baseUrl + '/registration/delete-applicant/1',
                       pageTitle: "Delete applicant"
                   });

              })  

              it('check your answers', function(done) { 
                  driver.get(Browser.baseUrl + '/test-only/store-registration-details')
                  var data = require('../../../spec-json/registration/CompleteWith2CoExecutors');
                var json = JSON.stringify(data)
                driver.executeScript(function(args) {
                    document.querySelector('#registrationDetails').innerText = args;
                }, json);
                driver.findElement(By.css('[type="submit"]')).click();

                   behaves.actsAsBasicPage(done, driver, {
                       url: Browser.baseUrl + '/registration/check-your-answers',
                       pageTitle: "Check your answers"
                   });

              })   
          });         