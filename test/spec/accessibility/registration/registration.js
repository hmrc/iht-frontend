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

describe('Registration accessibility : ', function() { 
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

            function fillDateOfDeath(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/date-of-death') 
                driver.findElement(By.name("dateOfDeath.day")).sendKeys('1'); 
                driver.findElement(By.name("dateOfDeath.month")).sendKeys('12'); 
                driver.findElement(By.name("dateOfDeath.year")).sendKeys('2016'); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillPermanentHome(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/permanent-home-location') 
                driver.findElement(By.css("#domicile-england_or_wales")).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillDeceasedDetails(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/deceaseds-details') 
                driver.findElement(By.name("firstName")).sendKeys('Harriet'); 
                driver.findElement(By.name("lastName")).sendKeys('McDonald'); 
                driver.findElement(By.name("dateOfBirth.day")).sendKeys('1'); 
                driver.findElement(By.name("dateOfBirth.month")).sendKeys('12'); 
                driver.findElement(By.name("dateOfBirth.year")).sendKeys('1980'); 
                driver.findElement(By.name("nino")).sendKeys('QQ123456'); 
                driver.findElement(By.css('#maritalStatus-married_or_in_civil_partnership-label')).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillDeceasedLastAddressUK(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/location-of-contact-address') 
                driver.findElement(By.css("#yes-label")).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillDeceasedLastAddressOutsideUK(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/location-of-contact-address') 
                driver.findElement(By.css("#no-label")).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillLastContactUK(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/uk-contact-address'); 
                driver.findElement(By.name("ukAddress.addressLine1")).sendKeys('10 Downing Street'); 
                driver.findElement(By.name("ukAddress.addressLine2")).sendKeys('London'); 
                driver.findElement(By.name("ukAddress.postCode")).sendKeys('NE12 3ER'); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillLastContactOutsideUK(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/contact-address'); 
                driver.findElement(By.name("ukAddress.addressLine1")).sendKeys('8 Rue de la Concorde'); 
                driver.findElement(By.name("ukAddress.ukAddressLine2")).sendKeys('St Germain'); 
                driver.findElement(By.name("ukAddress.addressLine3")).sendKeys('Paris'); 
                driver.findElement(By.name("iht-auto-complete")).sendKeys('Fr');  
                driver.findElements(selenium.By.css(".suggestion")) .then(function(elements) { 
                    expect(elements.length).toEqual(4); 
                });  
                driver.findElement(By.css('#iht-suggestions-list li:first-child')).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillApplyingForProbate(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/applying-for-probate'); 
                driver.findElement(By.css('#yes-label')).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillApplyingForProbateLocation(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/probate-location'); 
                driver.findElement(By.css('#country-england_or_wales-label')).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillYourContactDetailsUK(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/your-contact-details'); 
                driver.findElement(By.name("phoneNo")).sendKeys('01234 567 789'); 
                driver.findElement(By.css('#yes-label')).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillYourAddressUK(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/your-uk-address'); 
                driver.findElement(By.name("ukAddressLine1")).sendKeys('10 Downing Street'); 
                driver.findElement(By.name("ukAddressLine2")).sendKeys('London'); 
                driver.findElement(By.name("postCode")).sendKeys('NE12 3ER'); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillYourContactDetailsOutsideUK(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/your-contact-details'); 
                driver.findElement(By.name("phoneNo")).sendKeys('01234 567 789'); 
                driver.findElement(By.css('#no-label')).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillYourAddressOutsideUK(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/your-address'); 
                driver.findElement(By.name("ukAddressLine1")).sendKeys('8 Rue de la Concorde'); 
                driver.findElement(By.name("ukAddressLine2")).sendKeys('St Germain'); 
                driver.findElement(By.name("ukAddressLine3")).sendKeys('Paris'); 
                driver.findElement(By.name("iht-auto-complete")).sendKeys('Fr');  
                driver.findElements(selenium.By.css(".suggestion")) .then(function(elements) { 
                    expect(elements.length).toEqual(4); 
                });  
                driver.findElement(By.css('#iht-suggestions-list li:first-child')).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillAnyOtherApplicants(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/any-other-applicants'); 
                driver.findElement(By.css('#yes-label')).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillOtherPersonApplyingForProbate(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/applicants-details'); 
                driver.findElement(By.name("firstName")).sendKeys('Peter'); 
                driver.findElement(By.name("lastName")).sendKeys('Kingsman'); 
                driver.findElement(By.name("dateOfBirth.day")).sendKeys('28'); 
                driver.findElement(By.name("dateOfBirth.month")).sendKeys('12'); 
                driver.findElement(By.name("dateOfBirth.year")).sendKeys('1980'); 
                driver.findElement(By.name("nino")).sendKeys('QQ123456A'); 
                driver.findElement(By.name("phoneNo")).sendKeys('0181 152 456'); 
            }  
            function fillOtherPersonApplyingForProbateUK(done, driver) { 
                fillOtherPersonApplyingForProbate(done, driver); 
                driver.findElement(By.css('#yes-label')).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillOtherPersonApplyingForProbateOutsideUK(done, driver) { 
                fillOtherPersonApplyingForProbate(done, driver); 
                driver.findElement(By.css('#yes-label')).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillApplicantAddressUK(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/applicants-uk-address/1'); 
                driver.findElement(By.name("ukAddressLine1")).sendKeys('10 Downing Street'); 
                driver.findElement(By.name("ukAddressLine2")).sendKeys('London'); 
                driver.findElement(By.name("postCode")).sendKeys('NE12 3ER'); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillApplicantAddressOutsideUK(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/applicants-address/1'); 
                driver.findElement(By.name("ukAddressLine1")).sendKeys('8 Rue de la Concorde'); 
                driver.findElement(By.name("ukAddressLine2")).sendKeys('St Germain'); 
                driver.findElement(By.name("ukAddressLine3")).sendKeys('Paris'); 
                driver.findElement(By.name("iht-auto-complete")).sendKeys('Fr');  
                driver.findElements(selenium.By.css(".suggestion")) .then(function(elements) { 
                    expect(elements.length).toEqual(4); 
                });  
                driver.findElement(By.css('#iht-suggestions-list li:first-child')).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function fillOtherPeopleApplyingForProbate(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/other-probate-applicants'); 
                driver.findElement(By.css('#no-label')).click(); 
                actionHelper.submitPageHelper(done, driver, '#continue-button'); 
            }  
            function gotoDeleteOtherApplicant(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/other-probate-applicants'); 
                driver.findElement(By.css('#delete-executor-1')).click(); 
            }  
            function gotoCheckYourAnswers(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/check-your-answers'); 
            }  
            function gotoConfirmDetails(done, driver) { 
                driver.get('http://localhost:9070/inheritance-tax/registration/check-your-answers'); 
                driver.findElement(By.css('[type="submit"]')).click(); 
            }   


            it('registration checklist', function(done) { 
                 behaves.actsAsBasicPage(done, driver, {
                     url: 'http://localhost:9070/inheritance-tax/registration/registration-checklist',
                     pageTitle: "Before you start registration"
                 })
            });  

            it('when did the deceased die', function(done) { 
                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/date-of-death',
                      pageTitle: "Date of death",
                      button: '#continue-button'
                  })
            });  

            it('deceased permanent home', function(done) { 
                fillDateOfDeath(done, driver); 

                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/permanent-home-location',
                      pageTitle: "Permanent home",
                      button: '#continue-button'

                  })

            });  

              it('deceased details', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver);  

                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/deceaseds-details',
                      pageTitle: "About the deceased",
                      button: '#continue-button'

                  })
              });  

              it('deceased contact address', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver);  

                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/location-of-contact-address',
                      pageTitle: "Contact address",
                      button: '#continue-button'

                  })
              });  

              it('deceased contact address UK', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver); 
                  fillDeceasedLastAddressUK(done, driver);  

                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/uk-contact-address',
                      pageTitle: "Contact address",
                      button: '#continue-button'

                  })
              });  
              it('deceased contact address outside UK', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver); 
                  fillDeceasedLastAddressOutsideUK(done, driver);  

                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/contact-address',
                      pageTitle: "Contact address",
                      button: '#continue-button'

                  })
              });  
              it('applying for probate', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver); 
                  fillDeceasedLastAddressOutsideUK(done, driver); 
                  fillLastContactOutsideUK(done, driver);  

                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/applying-for-probate',
                      pageTitle: "Apply for probate",
                      button: '#continue-button'

                  })
              });  
              it('applying for probate location', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver); 
                  fillDeceasedLastAddressOutsideUK(done, driver); 
                  fillLastContactOutsideUK(done, driver); 
                  fillApplyingForProbate(done, driver);  

                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/probate-location',
                      pageTitle: "Probate location",
                      button: '#continue-button'

                  })
              });  
              it('your contact details', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver); 
                  fillDeceasedLastAddressOutsideUK(done, driver); 
                  fillLastContactOutsideUK(done, driver); 
                  fillApplyingForProbate(done, driver); 
                  fillApplyingForProbateLocation(done, driver);  

                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/your-contact-details',
                      pageTitle: "Your contact details",
                      button: '#continue-button'

                  })
              });  

              it('your address in the UK', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver); 
                  fillDeceasedLastAddressOutsideUK(done, driver); 
                  fillLastContactOutsideUK(done, driver); 
                  fillApplyingForProbate(done, driver); 
                  fillApplyingForProbateLocation(done, driver); 
                  fillYourContactDetailsUK(done, driver);  

                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/your-uk-address',
                      pageTitle: "Your address",
                      button: '#continue-button'

                  })
              })  

              it('your address outside the UK', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver); 
                  fillDeceasedLastAddressOutsideUK(done, driver); 
                  fillLastContactOutsideUK(done, driver); 
                  fillApplyingForProbate(done, driver); 
                  fillApplyingForProbateLocation(done, driver); 
                  fillYourContactDetailsOutsideUK(done, driver);  

                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/your-address',
                      pageTitle: "Your address",
                      button: '#continue-button'

                  })
              })  

              it('any other applicants', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver); 
                  fillDeceasedLastAddressOutsideUK(done, driver); 
                  fillLastContactOutsideUK(done, driver); 
                  fillApplyingForProbate(done, driver); 
                  fillApplyingForProbateLocation(done, driver); 
                  fillYourContactDetailsOutsideUK(done, driver); 
                  fillYourAddressOutsideUK(done, driver);  

                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/any-other-applicants',
                      pageTitle: "Other probate applicants",
                      button: '#continue-button'

                  })
              })  

              it('other applicant details', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver); 
                  fillDeceasedLastAddressOutsideUK(done, driver); 
                  fillLastContactOutsideUK(done, driver); 
                  fillApplyingForProbate(done, driver); 
                  fillApplyingForProbateLocation(done, driver); 
                  fillYourContactDetailsOutsideUK(done, driver); 
                  fillYourAddressOutsideUK(done, driver); 
                  fillAnyOtherApplicants(done, driver);  

                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/applicants-details',
                      pageTitle: "Other person’s details",
                      button: '#continue-button'

                  })
              })  

              it('other applicant address UK', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver); 
                  fillDeceasedLastAddressOutsideUK(done, driver); 
                  fillLastContactOutsideUK(done, driver); 
                  fillApplyingForProbate(done, driver); 
                  fillApplyingForProbateLocation(done, driver); 
                  fillYourContactDetailsOutsideUK(done, driver); 
                  fillYourAddressOutsideUK(done, driver); 
                  fillAnyOtherApplicants(done, driver); 
                  fillOtherPersonApplyingForProbateUK(done, driver);  

                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/applicants-uk-address/1',
                      pageTitle: "Other person’s address",
                      button: '#continue-button'

                  })
              })  

              it('other applicant address outside UK', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver); 
                  fillDeceasedLastAddressOutsideUK(done, driver); 
                  fillLastContactOutsideUK(done, driver); 
                  fillApplyingForProbate(done, driver); 
                  fillApplyingForProbateLocation(done, driver); 
                  fillYourContactDetailsOutsideUK(done, driver); 
                  fillYourAddressOutsideUK(done, driver); 
                  fillAnyOtherApplicants(done, driver); 
                  fillOtherPersonApplyingForProbateOutsideUK(done, driver);  


                    behaves.actsAsStandardForm(done, driver, {
                        url: 'http://localhost:9070/inheritance-tax/registration/applicants-uk-address/1',
                        pageTitle: "Other person’s address",
                        button: '#continue-button'

                    })
              })  

              it('other people applying for probate', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver); 
                  fillDeceasedLastAddressOutsideUK(done, driver); 
                  fillLastContactOutsideUK(done, driver); 
                  fillApplyingForProbate(done, driver); 
                  fillApplyingForProbateLocation(done, driver); 
                  fillYourContactDetailsOutsideUK(done, driver); 
                  fillYourAddressOutsideUK(done, driver); 
                  fillAnyOtherApplicants(done, driver); 
                  fillOtherPersonApplyingForProbateOutsideUK(done, driver); 
                  fillApplicantAddressOutsideUK(done, driver);  

                  behaves.actsAsStandardForm(done, driver, {
                      url: 'http://localhost:9070/inheritance-tax/registration/other-probate-applicants',
                      pageTitle: "Other applicants",
                      button: '#continue-button'

                  })
              })  

              it('delete other applicant', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver); 
                  fillDeceasedLastAddressOutsideUK(done, driver); 
                  fillLastContactOutsideUK(done, driver); 
                  fillApplyingForProbate(done, driver); 
                  fillApplyingForProbateLocation(done, driver); 
                  fillYourContactDetailsOutsideUK(done, driver); 
                  fillYourAddressOutsideUK(done, driver); 
                  fillAnyOtherApplicants(done, driver); 
                  fillOtherPersonApplyingForProbateOutsideUK(done, driver); 
                  fillApplicantAddressOutsideUK(done, driver); 
                  gotoDeleteOtherApplicant(done, driver);  

                   behaves.actsAsBasicPage(done, driver, {
                       url: 'http://localhost:9070/inheritance-tax/registration/delete-applicant/1',
                       pageTitle: "Delete applicant"
                   });

              })  

              it('check your answers', function(done) { 
                  fillDateOfDeath(done, driver); 
                  fillPermanentHome(done, driver); 
                  fillDeceasedDetails(done, driver); 
                  fillDeceasedLastAddressOutsideUK(done, driver); 
                  fillLastContactOutsideUK(done, driver); 
                  fillApplyingForProbate(done, driver); 
                  fillApplyingForProbateLocation(done, driver); 
                  fillYourContactDetailsOutsideUK(done, driver); 
                  fillYourAddressOutsideUK(done, driver); 
                  fillAnyOtherApplicants(done, driver); 
                  fillOtherPersonApplyingForProbateOutsideUK(done, driver); 
                  fillApplicantAddressOutsideUK(done, driver); 
                  fillOtherPeopleApplyingForProbate(done, driver); 
                  gotoCheckYourAnswers(done, driver);  

                   behaves.actsAsBasicPage(done, driver, {
                       url: 'http://localhost:9070/inheritance-tax/registration/check-your-answers',
                       pageTitle: "Check your answers"
                   });

              })   
          });         