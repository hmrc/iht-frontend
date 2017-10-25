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
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder, ContentChecker}
import iht.testhelpers.MockObjectBuilder._
import iht.utils.StringHelper
import iht.utils.CommonHelper._
import org.joda.time.LocalDate
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import uk.gov.hmrc.http.{ ConflictException, GatewayTimeoutException }

class RegistrationSummaryControllerTest extends RegistrationControllerTest{

  val mockIhtConnector = mock[IhtConnector]

  def controller = new RegistrationSummaryController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val metrics:Metrics = mock[Metrics]

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def controllerNotAuthorised = new RegistrationSummaryController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val metrics:Metrics = mock[Metrics]

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
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

    "Load the RegistrationSummary page with title" in {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails),
        Some(deceasedDetails), areOthersApplyingForProbate = Some(false))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      val content = ContentChecker.stripLineBreaks(contentAsString(result))

      content should include(messagesApi("iht.registration.checkYourAnswers"))
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

    "redirect to the estate report page if the RegistrationDetails does not contain deceased's date of death" in {
      val rd = fullyCompletedRegistrationDetails copy (deceasedDateOfDeath = None)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest()))
      status(result) shouldBe SEE_OTHER
    }

    "redirect to the estate report page if the RegistrationDetails does not contain the deceased's name" in {
      val dd = testDd copy (firstName = None)
      val rd = RegistrationDetails(Some(testDod), Some(testAd), Some(dd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest()))
      status(result) shouldBe SEE_OTHER
    }

    "redirect to the estate report page if the RegistrationDetails does not contain the deceased's address location" in {
      val dd = testDd copy (isAddressInUK = None)
      val rd = RegistrationDetails(Some(testDod), Some(testAd), Some(dd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest()))
      status(result) shouldBe SEE_OTHER
    }

    "redirect to the estate report page if the RegistrationDetails does not contain the deceased's address" in {
      val dd = testDd copy (ukAddress = None)
      val rd = RegistrationDetails(Some(testDod), Some(testAd), Some(dd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest()))
      status(result) shouldBe SEE_OTHER
    }

    "redirect to the estate report page if the RegistrationDetails does not contain an answer to 'applying for probate' question" in {
      val ad = testAd copy (isApplyingForProbate = None)
      val rd = RegistrationDetails(Some(testDod), Some(ad), Some(testDd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest()))
      status(result) shouldBe SEE_OTHER
    }

    "redirect to the estate report page if the RegistrationDetails does not contain the probate location" in {
      val ad = testAd copy (country = None)
      val rd = RegistrationDetails(Some(testDod), Some(ad), Some(testDd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest()))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url)
    }

    "redirect to the estate report page if the RegistrationDetails does not contain contact number" in {
      val ad = testAd copy (phoneNo = None)
      val rd = RegistrationDetails(Some(testDod), Some(ad), Some(testDd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest()))
      status(result) shouldBe SEE_OTHER
    }

    "redirect to the estate report page if the RegistrationDetails does not contain an address" in {
      val ad = testAd copy (ukAddress = None)
      val rd = RegistrationDetails(Some(testDod), Some(ad), Some(testDd), areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest()))
      status(result) shouldBe SEE_OTHER
    }

    "redirect to the estate report page if the RegistrationDetails does not contain an answer to 'are others applying for probate' question" in {
      val rd = RegistrationDetails(Some(testDod), Some(testAd), Some(testDd), areOthersApplyingForProbate = None)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = await(controller.onPageLoad(createFakeRequest()))
      status(result) shouldBe SEE_OTHER

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
