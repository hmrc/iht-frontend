/*
 * Copyright 2020 HM Revenue & Customs
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

import iht.config.AppConfig
import iht.metrics.IhtMetrics
import iht.models._
import iht.models.application.ApplicationDetails

import iht.testhelpers.{CommonBuilder, ContentChecker, MockFormPartialRetriever}
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.{GatewayTimeoutException, Upstream5xxResponse}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class RegistrationSummaryControllerTest extends RegistrationControllerTest{

  protected abstract class TestController extends FrontendController(mockControllerComponents) with RegistrationSummaryController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def controller = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector
    override val metrics: IhtMetrics = mock[IhtMetrics]

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def controllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector
    override val metrics: IhtMetrics = mock[IhtMetrics]

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def anchorLink(route: String, postfix: String) = s"$route#$postfix"

  val testDod = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
  val testAd = CommonBuilder.buildApplicantDetails
  val testDd = CommonBuilder.buildDeceasedDetails

  def fullyCompletedRegistrationDetails = {
    RegistrationDetails(Some(testDod), Some(testAd), Some(testDd), areOthersApplyingForProbate = Some(false))
  }

  "Summary controller" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on Submit if the user is not logged in" in {
      val result = controllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "Load the RegistrationSummary page with title" in {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails),
        Some(deceasedDetails), areOthersApplyingForProbate = Some(false))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be(OK)
      val content = ContentChecker.stripLineBreaks(contentAsString(result))

      content must include(messagesApi("iht.registration.checkYourAnswers"))
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

      val result = controller.onSubmit(createFakeRequest(authRetrieveNino = false))
      redirectLocation(result) mustBe
        Some(iht.controllers.registration.routes.CompletedRegistrationController.onPageLoad().url)
      status(result) must be(SEE_OTHER)
    }

    "onSubmit for valid input where no registration details should throw exception" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, None)

      a[RuntimeException] mustBe thrownBy {
        Await.result(controller.onSubmit(createFakeRequest(authRetrieveNino = false)), Duration.Inf)
      }
    }

    "onSubmit duplicate registration redirect to Duplicate Registration page" in {
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
      createMockToSubmitRegistration(mockIhtConnector, "")

      val result = controller.onSubmit(createFakeRequest(authRetrieveNino = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) mustBe Some(routes.DuplicateRegistrationController.onPageLoad("IHT Reference").url)
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

      when(mockIhtConnector.saveApplication(any(), any(), any())(any(), any()))
        .thenAnswer(new Answer[Future[Option[ApplicationDetails]]] {
          override def answer(invocation: InvocationOnMock): Future[Option[ApplicationDetails]] = {
            Future.failed(new GatewayTimeoutException("test"))
          }})

      val result = controller.onSubmit(createFakeRequest(authRetrieveNino = false))
      status(result) must be(INTERNAL_SERVER_ERROR)

      contentAsString(result) must include(messagesApi("error.cannotSend"))
    }

    "onSubmit Upstream5xxResponse" in {
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

      when(mockIhtConnector.saveApplication(any(), any(), any())(any(), any()))
        .thenAnswer(new Answer[Future[Option[ApplicationDetails]]] {
          override def answer(invocation: InvocationOnMock): Future[Option[ApplicationDetails]] = {
            Future.failed(new Upstream5xxResponse("Service Unavailable", 502, 502))
          }})

      val result = controller.onSubmit(createFakeRequest(authRetrieveNino = false))
      status(result) must be(INTERNAL_SERVER_ERROR)

      contentAsString(result) must include(messagesApi("error.registration.serviceUnavailable.p1"))
    }

    "onSubmit Upstream5xxResponse 502" in {
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

      when(mockIhtConnector.saveApplication(any(), any(), any())(any(), any()))
        .thenAnswer(new Answer[Future[Option[ApplicationDetails]]] {
          override def answer(invocation: InvocationOnMock): Future[Option[ApplicationDetails]] = {
            Future.failed(new Upstream5xxResponse("test", 502, 502))
          }})

      val result = controller.onSubmit(createFakeRequest(authRetrieveNino = false))
      status(result) must be(500)
    }

    "redirect to the estate report page if the RegistrationDetails does not contain deceased's date of death" in {
      val rd = fullyCompletedRegistrationDetails copy (deceasedDateOfDeath = None)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest(authRetrieveNino = false)))
      status(result) mustBe SEE_OTHER
    }

    "redirect to the estate report page if the RegistrationDetails does not contain the deceased's name" in {
      val dd = testDd copy (firstName = None)
      val rd = RegistrationDetails(Some(testDod), Some(testAd), Some(dd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest(authRetrieveNino = false)))
      status(result) mustBe SEE_OTHER
    }

    "redirect to the estate report page if the RegistrationDetails does not contain the deceased's address location" in {
      val dd = testDd copy (isAddressInUK = None)
      val rd = RegistrationDetails(Some(testDod), Some(testAd), Some(dd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest(authRetrieveNino = false)))
      status(result) mustBe SEE_OTHER
    }

    "redirect to the estate report page if the RegistrationDetails does not contain the deceased's address" in {
      val dd = testDd copy (ukAddress = None)
      val rd = RegistrationDetails(Some(testDod), Some(testAd), Some(dd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest(authRetrieveNino = false)))
      status(result) mustBe SEE_OTHER
    }

    "redirect to the estate report page if the RegistrationDetails does not contain an answer to 'applying for probate' question" in {
      val ad = testAd copy (isApplyingForProbate = None)
      val rd = RegistrationDetails(Some(testDod), Some(ad), Some(testDd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest(authRetrieveNino = false)))
      status(result) mustBe SEE_OTHER
    }

    "redirect to the estate report page if the RegistrationDetails does not contain the probate location" in {
      val ad = testAd copy (country = None)
      val rd = RegistrationDetails(Some(testDod), Some(ad), Some(testDd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest(authRetrieveNino = false)))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url)
    }

    "redirect to the estate report page if the RegistrationDetails does not contain contact number" in {
      val ad = testAd copy (phoneNo = None)
      val rd = RegistrationDetails(Some(testDod), Some(ad), Some(testDd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest(authRetrieveNino = false)))
      status(result) mustBe SEE_OTHER
    }

    "redirect to the estate report page if the RegistrationDetails does not contain an address" in {
      val ad = testAd copy (ukAddress = None)
      val rd = RegistrationDetails(Some(testDod), Some(ad), Some(testDd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest(authRetrieveNino = false)))
      status(result) mustBe SEE_OTHER
    }

    "redirect to the estate report page if the RegistrationDetails does not contain an answer to 'are others applying for probate' question" in {
      val rd = RegistrationDetails(Some(testDod), Some(testAd), Some(testDd), areOthersApplyingForProbate = None)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest(authRetrieveNino = false)))
      status(result) mustBe SEE_OTHER

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

      when(mockIhtConnector.saveApplication(any(), any(), any())(any(), any()))
        .thenAnswer(new Answer[Future[Option[ApplicationDetails]]] {
          override def answer(invocation: InvocationOnMock): Future[Option[ApplicationDetails]] = {
            Future.failed(new RuntimeException("Request timed out"))
          }})

      val result = controller.onSubmit(createFakeRequest(authRetrieveNino = false))
      status(result) must be(INTERNAL_SERVER_ERROR)

      contentAsString(result) must include(messagesApi("error.cannotSend"))
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

      when(mockIhtConnector.saveApplication(any(), any(), any())(any(), any()))
        .thenAnswer(new Answer[Future[Option[ApplicationDetails]]] {
          override def answer(invocation: InvocationOnMock): Future[Option[ApplicationDetails]] = {
            Future.failed(new RuntimeException("testing"))
          }})

      val result = controller.onSubmit(createFakeRequest(authRetrieveNino = false))
      status(result) must be(INTERNAL_SERVER_ERROR)

      contentAsString(result) must include(messagesApi("error.cannotSend"))
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

      val result = controller.onSubmit(createFakeRequest(authRetrieveNino = false))
      status(result) must be(INTERNAL_SERVER_ERROR)
    }
  }
}
