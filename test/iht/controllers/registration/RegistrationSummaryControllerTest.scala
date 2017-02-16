/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package iht.controllers.registration

import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.{FieldMappings, IhtProperties}
import iht.controllers.registration.applicant.{routes => applicantRoutes}
import iht.controllers.registration.deceased.{routes => deceasedRoutes}
import iht.controllers.registration.executor.{routes => executorRoutes}
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.debts._
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils.StringHelper
import org.joda.time.LocalDate
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{ConflictException, GatewayTimeoutException}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class RegistrationSummaryControllerTest extends RegistrationControllerTest{

  val mockIhtConnector = mock[IhtConnector]

  def controller = new RegistrationSummaryController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val metrics:Metrics = Metrics
    override val isWhiteListEnabled = false
  }

  def controllerNotAuthorised = new RegistrationSummaryController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val metrics:Metrics = Metrics
    override val isWhiteListEnabled = false
  }

  def anchorLink(route: String, postfix: String) = s"$route#$postfix"

  val testDod = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
  val testAd = CommonBuilder.buildApplicantDetails
  val testDd = CommonBuilder.buildDeceasedDetails

  def fullyCompletedRegistrationDetails = {
    RegistrationDetails(Some(testDod), Some(testAd), Some(testDd), areOthersApplyingForProbate = Some(false))
  }

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  "Summary controller" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on Submit if the user is not logged in" in {
      val result = controllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "Load the RegistrationSummary page with basic data" in {

      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails),
                                                  Some(deceasedDetails), areOthersApplyingForProbate = Some(false))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      val content = contentAsString(result)

      content should include(messagesApi("iht.registration.checkYourAnswers"))
      content should include(messagesApi("page.iht.registration.registrationSummary.subTitle"))
      content should include(messagesApi("page.iht.registration.registrationSummary.deceasedTable.title"))
      content should include(messagesApi("iht.name.upperCaseInitial"))
      content should include(deceasedDetails.firstName.get)
      content should include(deceasedDetails.lastName.get)
      content should include(anchorLink(deceasedRoutes.AboutDeceasedController.onEditPageLoad().url, "firstName"))

      content should include(messagesApi("iht.dateOfDeath"))
      content should include(deceasedDateOfDeath.dateOfDeath.toString(IhtProperties.dateFormatForDisplay))
      content should include(anchorLink(deceasedRoutes.DeceasedDateOfDeathController.onEditPageLoad().url, "date-of-death"))

      content should include(messagesApi("iht.dateofbirth"))
      content should include(deceasedDetails.dateOfBirth.get.toString(IhtProperties.dateFormatForDisplay))
      content should include(anchorLink(deceasedRoutes.AboutDeceasedController.onEditPageLoad().url, "date-of-birth"))

      content should include(messagesApi("iht.nationalInsuranceNo"))
      content should include(deceasedDetails.nino.getOrElse(""))
      content should include(anchorLink(deceasedRoutes.AboutDeceasedController.onEditPageLoad().url, "nino"))

      content should include(messagesApi("iht.registration.contactAddress"))
      content should include(deceasedDetails.ukAddress.get.ukAddressLine1)
      content should include(deceasedDetails.ukAddress.get.ukAddressLine2)
      content should include(deceasedDetails.ukAddress.get.ukAddressLine3.getOrElse(""))
      content should include(deceasedDetails.ukAddress.get.ukAddressLine4.getOrElse(""))
      content should include(deceasedDetails.ukAddress.get.postCode)
      content should include(anchorLink(deceasedRoutes.DeceasedAddressDetailsUKController.onEditPageLoad().url, "details"))
      content should not include deceasedRoutes.DeceasedAddressDetailsOutsideUKController.onEditPageLoad().url

      content should include(messagesApi("iht.registration.deceased.locationOfPermanentHome"))
      content should include(deceasedDetails.domicile.get)
      content should include(anchorLink(deceasedRoutes.DeceasedPermanentHomeController.onEditPageLoad().url, "country"))

      content should include(messagesApi("page.iht.registration.registrationSummary.deceasedInfo.maritalStatus.label"))
      content should include(FieldMappings.maritalStatusMap(deceasedDetails.maritalStatus.get))
      content should include(anchorLink(deceasedRoutes.AboutDeceasedController.onEditPageLoad().url, "relationship-status"))

      content should include(messagesApi("page.iht.registration.registrationSummary.applicantTable.title"))

      content should include(messagesApi("iht.name.upperCaseInitial"))
      content should include(applicantDetails.firstName.get)
      content should include(applicantDetails.lastName.get)

      content should include(messagesApi("iht.dateofbirth"))
      content should include(applicantDetails.dateOfBirth.get.toString(IhtProperties.dateFormatForDisplay))

      content should include(messagesApi("iht.nationalInsuranceNo"))
      content should include(applicantDetails.nino.getOrElse(""))

      content should include(messagesApi("iht.registration.checklist.phoneNo.upperCaseInitial"))
      content should include(applicantDetails.phoneNo.get)
      content should include(anchorLink(applicantRoutes.ApplicantTellUsAboutYourselfController.onEditPageLoad().url, "phoneNo"))

      content should include(messagesApi("iht.address.upperCaseInitial"))
      content should include(applicantDetails.ukAddress.get.ukAddressLine1)
      content should include(applicantDetails.ukAddress.get.ukAddressLine2)
      content should include(applicantDetails.ukAddress.get.ukAddressLine3.getOrElse(""))
      content should include(applicantDetails.ukAddress.get.ukAddressLine4.getOrElse(""))
      content should include(applicantDetails.ukAddress.get.postCode)
      content should include(iht.utils.countryName(applicantDetails.ukAddress.get.countryCode))
      content should include(anchorLink(applicantRoutes.ApplicantAddressController.onEditPageLoadUk().url, "details"))
      content should not include applicantRoutes.ApplicantAddressController.onEditPageLoadAbroad().url

      content should include(messagesApi("iht.registration.applicant.applyingForProbate"))
      content should include(StringHelper.yesNoFormat(applicantDetails.isApplyingForProbate))
      content should include(anchorLink(applicantRoutes.ApplyingForProbateController.onEditPageLoad().url, "applying-for-probate"))

      content should include(messagesApi("page.iht.registration.applicant.probateLocation.title"))
      content should include(applicantDetails.country.get)
      content should include(anchorLink(applicantRoutes.ProbateLocationController.onEditPageLoad().url, "country"))

      content should include(messagesApi("iht.registration.othersApplyingForProbate"))
      content should include(messagesApi("page.iht.registration.registrationSummary.coExecutorTable.none"))
      content should include(anchorLink(executorRoutes.OthersApplyingForProbateController.onPageLoadFromOverview().url, "answer"))
    }

    "Load the RegistrationSummary page with addresses abroad" in {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val applicantAddress = UkAddress("App Addr 1", "App Addr 2", Some("App Addr 3"), Some("App Addr 4"), "", "AU")
      val deceasedAddress = UkAddress("Dec Addr 1", "Dec Addr 2", Some("Dec Addr 3"), Some("Dec Addr 4"), "", "US")

      val applicantDetails = CommonBuilder.buildApplicantDetails copy (ukAddress = Some(applicantAddress), doesLiveInUK = Some(false))
      val deceasedDetails = CommonBuilder.buildDeceasedDetails copy (ukAddress = Some(deceasedAddress), isAddressInUK = Some(false))

      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails),
        Some(deceasedDetails), areOthersApplyingForProbate = Some(false))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      val content = contentAsString(result)

      content should include(deceasedAddress.ukAddressLine1)
      content should include(deceasedAddress.ukAddressLine2)
      content should include(deceasedAddress.ukAddressLine3.getOrElse(""))
      content should include(deceasedAddress.ukAddressLine4.getOrElse(""))
      content should include(iht.utils.countryName(deceasedAddress.countryCode))
      content should include(anchorLink(deceasedRoutes.DeceasedAddressDetailsOutsideUKController.onEditPageLoad().url, "details"))
      content should not include deceasedRoutes.DeceasedAddressDetailsUKController.onEditPageLoad().url

      content should include(applicantAddress.ukAddressLine1)
      content should include(applicantAddress.ukAddressLine2)
      content should include(applicantAddress.ukAddressLine3.getOrElse(""))
      content should include(applicantAddress.ukAddressLine4.getOrElse(""))
      content should include(iht.utils.countryName(applicantAddress.countryCode))
      content should include(anchorLink(applicantRoutes.ApplicantAddressController.onEditPageLoadAbroad().url, "details"))
      content should not include applicantRoutes.ApplicantAddressController.onEditPageLoadUk().url
    }

    "Load the RegistrationSummary page with 1 co-executor defined" in {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val coExec1 = CommonBuilder.buildCoExecutor copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator, dateOfBirth = new LocalDate(1980, 4, 12),
        nino = CommonBuilder.DefaultNino, isAddressInUk = Some(true),
        ukAddress = Some(UkAddress("X1", "X2", Some("X3"), None, "aa1 1aa", "GB")),
        contactDetails = ContactDetails("0123 456789", Some("")))

      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails), Some(deceasedDetails),
        Seq(coExec1), areOthersApplyingForProbate = Some(true))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(createFakeRequest())
      status(result) should be(OK)

      val content = contentAsString(result)
      content shouldNot include(messagesApi("page.iht.registration.registrationSummary.coExecutorTable.none"))

      content should include(executorRoutes.ExecutorOverviewController.onPageLoad().url)

      content should include(messagesApi("page.iht.registration.registrationSummary.coExecutorTable.changeOthersApplying.link"))
      content should include(messagesApi("page.iht.registration.registrationSummary.coExecutorTable.sectionTitle", "1"))
      content should include(messagesApi("iht.name.upperCaseInitial"))
      content should include(coExec1.firstName)
      content should include(coExec1.lastName)
      content should include(anchorLink(executorRoutes.CoExecutorPersonalDetailsController.onEditPageLoad("1").url, "firstName"))

      content should include(messagesApi("iht.dateofbirth"))
      content should include(coExec1.dateOfBirth.toString(IhtProperties.dateFormatForDisplay))
      content should include(anchorLink(executorRoutes.CoExecutorPersonalDetailsController.onEditPageLoad("1").url, "date-of-birth"))

      content should include(messagesApi("iht.nationalInsuranceNo"))
      content should include(coExec1.nino)
      content should include(anchorLink(executorRoutes.CoExecutorPersonalDetailsController.onEditPageLoad("1").url, "nino"))

      content should include(messagesApi("iht.address.upperCaseInitial"))
      content should include(coExec1.ukAddress.get.ukAddressLine1)
      content should include(coExec1.ukAddress.get.ukAddressLine2)
      content should include(coExec1.ukAddress.get.ukAddressLine3.getOrElse(""))
      content should include(coExec1.ukAddress.get.ukAddressLine4.getOrElse(""))
      content should include(coExec1.ukAddress.get.postCode)
      content should include(iht.utils.countryName(coExec1.ukAddress.get.countryCode))
      content should include(anchorLink(executorRoutes.OtherPersonsAddressController.onEditPageLoadUK("1").url, "details"))

      content should include(messagesApi("iht.registration.checklist.phoneNo.upperCaseInitial"))
      content should include(coExec1.contactDetails.phoneNo)
    }

    "Load the RegistrationSummary page with 2 co-executors defined" in {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val coExec1 = CommonBuilder.buildCoExecutor copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator, dateOfBirth = new LocalDate(1980, 4, 12),
        nino = CommonBuilder.DefaultNino, isAddressInUk = Some(true),
        ukAddress = Some(UkAddress("xX1", "Xx2", Some("x3"), None, "aa1 1aa", "GB")),
        contactDetails = ContactDetails("0123 456789", Some("")))

      val coExec2 = CommonBuilder.buildCoExecutor copy (id = Some("2"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator, dateOfBirth = new LocalDate(1981, 5, 13),
        nino = CommonBuilder.DefaultNino, isAddressInUk = Some(false),
        ukAddress = Some(UkAddress("Z1", "Z2", Some("Z3"), Some("Z4"), "", "AU")),
        contactDetails = ContactDetails("987654321", Some("")))

      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails), Some(deceasedDetails),
        Seq(coExec1, coExec2), areOthersApplyingForProbate = Some(true))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(createFakeRequest())
      status(result) should be(OK)

      val content = contentAsString(result)
      content shouldNot include(messagesApi("page.iht.registration.registrationSummary.coExecutorTable.none"))

      content should include(executorRoutes.ExecutorOverviewController.onPageLoad().url)

      content should include(messagesApi("iht.name.upperCaseInitial"))
      content should include(coExec1.firstName)
      content should include(coExec1.lastName)
      content should include(anchorLink(executorRoutes.CoExecutorPersonalDetailsController.onEditPageLoad("1").url, "firstName"))

      content should include(messagesApi("iht.dateofbirth"))
      content should include(coExec1.dateOfBirth.toString(IhtProperties.dateFormatForDisplay))

      content should include(messagesApi("iht.nationalInsuranceNo"))
      content should include(coExec1.nino)

      content should include(messagesApi("iht.address.upperCaseInitial"))
      content should include(coExec1.ukAddress.get.ukAddressLine1)
      content should include(coExec1.ukAddress.get.ukAddressLine2)
      content should include(coExec1.ukAddress.get.ukAddressLine3.getOrElse(""))
      content should include(coExec1.ukAddress.get.ukAddressLine4.getOrElse(""))
      content should include(coExec1.ukAddress.get.postCode)
      content should include(iht.utils.countryName(coExec1.ukAddress.get.countryCode))
      content should include(anchorLink(executorRoutes.OtherPersonsAddressController.onEditPageLoadUK("1").url, "details"))

      content should include(messagesApi("iht.registration.checklist.phoneNo.upperCaseInitial"))
      content should include(coExec1.contactDetails.phoneNo)

      content should include(coExec2.ukAddress.get.ukAddressLine1)
      content should include(coExec2.ukAddress.get.ukAddressLine2)
      content should include(coExec2.ukAddress.get.ukAddressLine3.getOrElse(""))
      content should include(coExec2.ukAddress.get.ukAddressLine4.getOrElse(""))
      content should include(iht.utils.countryName(coExec2.ukAddress.get.countryCode))
      content should include(executorRoutes.OtherPersonsAddressController.onEditPageLoadAbroad("2").url)

      content should include(coExec2.contactDetails.phoneNo)
    }

    "onSubmit for valid input should redirect to completed registration" in {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val coExec1 = CommonBuilder.buildCoExecutor copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val coExec2 = CommonBuilder.buildCoExecutor copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails),
        Some(deceasedDetails), Seq(coExec1, coExec2))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToSaveApplicationDetails(mockIhtConnector)
      createMockToSubmitRegistration(mockIhtConnector)

      val result = controller.onSubmit(createFakeRequest())
      redirectLocation(result) shouldBe
        Some(iht.controllers.registration.routes.CompletedRegistrationController.onPageLoad().url)
      status(result) should be(SEE_OTHER)
    }

    "onSubmit for valid input where no registration details should throw exception" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, None)

      a[RuntimeException] shouldBe thrownBy {
        Await.result(controller.onSubmit(createFakeRequest()), Duration.Inf)
      }
    }

    "onSubmit duplicate registration throw ConflictException" in {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val coExec1 = CommonBuilder.buildCoExecutor copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val coExec2 = CommonBuilder.buildCoExecutor copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath),
        Some(applicantDetails), Some(deceasedDetails), Seq(coExec1, coExec2))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToSubmitRegistration(mockIhtConnector)

      when(mockIhtConnector.saveApplication(any(), any(), any())(any()))
        .thenAnswer(new Answer[Future[Option[ApplicationDetails]]] {
          override def answer(invocation: InvocationOnMock): Future[Option[ApplicationDetails]] = {
            Future.failed(new ConflictException("test"))
          }})

      val result = controller.onSubmit(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) shouldBe Some(routes.DuplicateRegistrationController.onPageLoad("IHT Reference").url)
    }

    "onSubmit GatewayTimeoutException" in {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val coExec1 = CommonBuilder.buildCoExecutor copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val coExec2 = CommonBuilder.buildCoExecutor copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails),
        Some(deceasedDetails), Seq(coExec1, coExec2))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToSubmitRegistration(mockIhtConnector)

      when(mockIhtConnector.saveApplication(any(), any(), any())(any()))
        .thenAnswer(new Answer[Future[Option[ApplicationDetails]]] {
        override def answer(invocation: InvocationOnMock): Future[Option[ApplicationDetails]] = {
          Future.failed(new GatewayTimeoutException("test"))
        }})

      val result = controller.onSubmit(createFakeRequest())
      status(result) should be(OK)

      contentAsString(result) should include(messagesApi("error.cannotSend"))
    }

    "raise an error when accessing the screen without first entering the deceased's date of death" in {
      val rd = fullyCompletedRegistrationDetails copy (deceasedDateOfDeath = None)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      intercept[java.lang.RuntimeException] {
        val result = await(controller.onPageLoad(createFakeRequest()))
      }
    }

    "raise an error when accessing the screen without first entering the deceased's name" in {
      val dd = testDd copy (firstName = None)
      val rd = RegistrationDetails(Some(testDod), Some(testAd), Some(dd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      intercept[java.lang.RuntimeException] {
        val result = await(controller.onPageLoad(createFakeRequest()))
      }
    }

    "raise an error when accessing the screen without first entering the deceased's address location" in {
      val dd = testDd copy (isAddressInUK = None)
      val rd = RegistrationDetails(Some(testDod), Some(testAd), Some(dd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      intercept[java.lang.RuntimeException] {
        val result = await(controller.onPageLoad(createFakeRequest()))
      }
    }

    "raise an error when accessing the screen without first entering the deceased's address" in {
      val dd = testDd copy (ukAddress = None)
      val rd = RegistrationDetails(Some(testDod), Some(testAd), Some(dd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      intercept[java.lang.RuntimeException] {
        val result = await(controller.onPageLoad(createFakeRequest()))
      }
    }

    "raise an error when accessing the screen without first answering the 'applying for probate' question" in {
      val ad = testAd copy (isApplyingForProbate = None)
      val rd = RegistrationDetails(Some(testDod), Some(ad), Some(testDd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      intercept[java.lang.RuntimeException] {
        val result = await(controller.onPageLoad(createFakeRequest()))
      }
    }

    "raise an error when accessing the screen without first entering the probate location" in {
      val ad = testAd copy (country = None)
      val rd = RegistrationDetails(Some(testDod), Some(ad), Some(testDd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      intercept[java.lang.RuntimeException] {
        val result = await(controller.onPageLoad(createFakeRequest()))
      }
    }

    "raise an error when accessing the screen without first entering a contact number" in {
      val ad = testAd copy (phoneNo = None)
      val rd = RegistrationDetails(Some(testDod), Some(ad), Some(testDd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      intercept[java.lang.RuntimeException] {
        val result = await(controller.onPageLoad(createFakeRequest()))
      }
    }

    "raise an error when accessing the screen without first entering an address" in {
      val ad = testAd copy (ukAddress = None)
      val rd = RegistrationDetails(Some(testDod), Some(ad), Some(testDd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      intercept[java.lang.RuntimeException] {
        val result = await(controller.onPageLoad(createFakeRequest()))
      }
    }

    "raise an error when accessing the screen without first answering the 'are others applying for probate' question" in {
      val rd = RegistrationDetails(Some(testDod), Some(testAd), Some(testDd), areOthersApplyingForProbate = None)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      intercept[java.lang.RuntimeException] {
        val result = await(controller.onPageLoad(createFakeRequest()))
      }
    }

    "onSubmit RuntimeException" in {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val coExec1 = CommonBuilder.buildCoExecutor copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val coExec2 = CommonBuilder.buildCoExecutor copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails),
        Some(deceasedDetails), Seq(coExec1, coExec2))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToSubmitRegistration(mockIhtConnector)

      when(mockIhtConnector.saveApplication(any(), any(), any())(any()))
        .thenAnswer(new Answer[Future[Option[ApplicationDetails]]] {
        override def answer(invocation: InvocationOnMock): Future[Option[ApplicationDetails]] = {
          Future.failed(new RuntimeException("Request timed out"))
        }})

      val result = controller.onSubmit(createFakeRequest())
      status(result) should be(OK)

      contentAsString(result) should include(messagesApi("error.cannotSend"))
    }

    "onSubmit RuntimeException not timeout" in {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val coExec1 = CommonBuilder.buildCoExecutor copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val coExec2 = CommonBuilder.buildCoExecutor copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails),
        Some(deceasedDetails), Seq(coExec1, coExec2))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToSubmitRegistration(mockIhtConnector)

      when(mockIhtConnector.saveApplication(any(), any(), any())(any()))
        .thenAnswer(new Answer[Future[Option[ApplicationDetails]]] {
        override def answer(invocation: InvocationOnMock): Future[Option[ApplicationDetails]] = {
          Future.failed(new RuntimeException("testing"))
        }})

      val result = controller.onSubmit(createFakeRequest())
      status(result) should be(OK)

      contentAsString(result) should include(messagesApi("error.cannotSend"))
    }

    "onSubmit for valid input should produce an internal server error if the storage fails" in {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val coExec1 = CommonBuilder.buildCoExecutor copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val coExec2 = CommonBuilder.buildCoExecutor copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails),
        Some(deceasedDetails), Seq(coExec1, coExec2))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector, Some(registrationDetails))
      createMockToSaveApplicationDetails(mockIhtConnector)
      createMockToSubmitRegistration(mockIhtConnector)

      val result = controller.onSubmit(createFakeRequest())
      status(result) should be(INTERNAL_SERVER_ERROR)
    }
  }
}
