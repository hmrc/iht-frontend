/*
 * Copyright 2022 HM Revenue & Customs
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

package iht.controllers.application.declaration

import iht.config.AppConfig
import iht.connector.IhtConnector
import iht.controllers.ControllerHelper
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.metrics.IhtMetrics
import iht.models.application.assets._
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.models.application.exemptions.{AllExemptions, PartnerExemption}
import iht.models.application.tnrb.TnrbEligibiltyModel
import iht.models.enums.StatsSource
import iht.models.{CoExecutor, ContactDetails}
import iht.testhelpers.CommonBuilder
import iht.utils.ApplicationStatus
import iht.views.html.application.declaration.declaration
import iht.views.html.estateReports.estateReports_error_serviceUnavailable
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import play.api.http.Status.OK
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class DeclarationControllerTest extends ApplicationControllerTest {

  // Implicit objects required by play framework.
  implicit val headerCarrier = FakeHeaders()
  implicit val request = FakeRequest()
  implicit val hc = new HeaderCarrier


  val ihtReferenceNo = "XXX"

  protected abstract class TestController extends FrontendController(mockControllerComponents) with DeclarationController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val declarationView: declaration = app.injector.instanceOf[declaration]
    override val estateReportsErrorServiceUnavailableView: estateReports_error_serviceUnavailable = app.injector.instanceOf[estateReports_error_serviceUnavailable]
  }

  def declarationController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector
    override lazy val metrics: IhtMetrics = mock[IhtMetrics]
  }

  def declarationControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector
    override lazy val metrics: IhtMetrics = mock[IhtMetrics]

  }

  def mockForApplicationStatus(requiredStatus: String, coExecutorsEnabled: Boolean = false) = {

    val coExecutorsFromLookup = if (coExecutorsEnabled) {
      Seq(CoExecutor(None, "firstName", None, "lastName", LocalDate.now(), "nino", None, None, ContactDetails("phoneNo"), None, None))
    } else {
      Seq.empty[CoExecutor]
    }

    val regDetails = CommonBuilder.buildRegistrationDetails copy(
      ihtReference = Some(ihtReferenceNo),
      status = requiredStatus,
      coExecutors = coExecutorsFromLookup
    )

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(regDetails)))
    createMockToGetCaseDetails(mockIhtConnector, regDetails)
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(regDetails))
  }



  "declaration controller" must {

    "return correct riskMessageFromEdh when there is no money entered" in {
      val testConnector = mock[IhtConnector]
      val regDetails = CommonBuilder.buildRegistrationDetails
      val appDetails = CommonBuilder.buildApplicationDetails
      createMockToGetRealtimeRiskMessage(testConnector, Option("Risk Message"))

      await(declarationController.realTimeRiskingMessage(appDetails, regDetails.ihtReference.get, "AB123456C", testConnector)) mustBe Some("Risk Message")
    }

    "return correct riskMessageFromEdh when there is money value of zero" in {
      val testConnector = mock[IhtConnector]
      val regDetails = CommonBuilder.buildRegistrationDetails
      createMockToGetRealtimeRiskMessage(testConnector, Option("Risk Message"))

      val appDetails = {
        val allAssets = CommonBuilder.buildAllAssets.copy(
          money = Some(CommonBuilder.buildShareableBasicElementExtended.copy(
            Some(BigDecimal(0)), None, Some(true), Some(false)))
        )
        CommonBuilder.buildApplicationDetails.copy(allAssets = Some(allAssets))
      }

      await(declarationController.realTimeRiskingMessage(appDetails, regDetails.ihtReference.get, "AB123456C", testConnector)) mustBe Some("Risk Message")
    }

    "return None when there is money value which is non-zero" in {
      val testConnector = mock[IhtConnector]
      val regDetails = CommonBuilder.buildRegistrationDetails
      val riskMessage = Some("Risk Message")
      createMockToGetRealtimeRiskMessage(testConnector, riskMessage)

      val appDetails = {
        val allAssets = CommonBuilder.buildAllAssets.copy(
          money = Some(CommonBuilder.buildShareableBasicElementExtended.copy(
            Some(BigDecimal(10)), None, Some(true), Some(false)))
        )
        CommonBuilder.buildApplicationDetails.copy(allAssets = Some(allAssets))
      }

      await(declarationController.realTimeRiskingMessage(appDetails, regDetails.ihtReference.get, "AB123456C", testConnector)) mustBe None
    }

    "return correct riskMessageFromEdh when there is money value of None" in {
      val testConnector = mock[IhtConnector]
      val regDetails = CommonBuilder.buildRegistrationDetails
      val riskMessage = Some("Risk Message")
      createMockToGetRealtimeRiskMessage(testConnector, riskMessage)

      val appDetails = {
        val allAssets = CommonBuilder.buildAllAssets.copy(
          money = Some(CommonBuilder.buildShareableBasicElementExtended.copy(
            None, None, Some(true), Some(false)))
        )
        CommonBuilder.buildApplicationDetails.copy(allAssets = Some(allAssets))
      }

      await(declarationController.realTimeRiskingMessage(appDetails, regDetails.ihtReference.get, "AB123456C", testConnector)) mustBe riskMessage
    }

    "return None when there is an error in getting risk message" in {
      val testConnector = mock[IhtConnector]
      val regDetails = CommonBuilder.buildRegistrationDetails
      val appDetails = CommonBuilder.buildApplicationDetails
      val riskMessage = Some("Risk Message")
      createMockToGetRealtimeRiskMessage(testConnector, riskMessage)
      when(testConnector.getRealtimeRiskingMessage(any(), any())(any()))
        .thenThrow(new RuntimeException("error"))

      a[RuntimeException] mustBe thrownBy{
        await(declarationController.realTimeRiskingMessage(appDetails, regDetails.ihtReference.get, "AB123456C", testConnector))
      }
    }

    "return correct riskMessageFromEdh when the money entered is of value 0 and shared value is None" in {
      val testConnector = mock[IhtConnector]
      val regDetails = CommonBuilder.buildRegistrationDetails
      val appDetails = CommonBuilder.buildApplicationDetails
      val riskMessage = Some("Risk Message")
      createMockToGetRealtimeRiskMessage(testConnector, riskMessage)

      val ad = appDetails.copy(allAssets = Some(CommonBuilder.buildAllAssets.copy(
        money = Some(ShareableBasicEstateElement(
          value = Some(BigDecimal(0)),
          shareValue = None,
          isOwned = None,
          isOwnedShare = None)))))

      await(declarationController.realTimeRiskingMessage(ad, regDetails.ihtReference.get, "AB123456C", testConnector)) mustBe riskMessage
    }

    "return correct riskMessageFromEdh when the money owed and shared value are entered as 0" in {
      val testConnector = mock[IhtConnector]
      val regDetails = CommonBuilder.buildRegistrationDetails
      val appDetails = CommonBuilder.buildApplicationDetails
      val ad = appDetails.copy(allAssets = Some(CommonBuilder.buildAllAssets.copy(
        money = Some(ShareableBasicEstateElement(
          value = Some(BigDecimal(0)),
          shareValue = Some(BigDecimal(0)),
          isOwned = None,
          isOwnedShare = None)))))

      val riskMessage = Some("Risk Message")
      createMockToGetRealtimeRiskMessage(testConnector, riskMessage)

      await(declarationController.realTimeRiskingMessage(ad, regDetails.ihtReference.get, "AB123456C", testConnector)) mustBe riskMessage
    }

    "return riskMessageFromEdh as None when there is non zero money value" in {

      val regDetails = CommonBuilder.buildRegistrationDetails
      val appDetails = CommonBuilder.buildApplicationDetails
      val ad = appDetails.copy(allAssets = Some(CommonBuilder.buildAllAssets.copy(
        money = Some(ShareableBasicEstateElement(
          value = Some(BigDecimal(100)),
          shareValue = None,
          isOwned = None,
          isOwnedShare = None)))))

      await(declarationController.realTimeRiskingMessage(ad, regDetails.ihtReference.get, "AB123456C", mockIhtConnector)) mustBe empty
    }

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = declarationController.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = declarationControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result).get must be(loginUrl)
    }

    "respond with OK on page load for valueLessThanNilRateBand, single executor" in {

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
      createMockToGetSingleValueFromCache(mockCachingConnector, same("declarationType"), Some("valueLessThanNilRateBand"))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("isMultipleExecutor"), Some("false"))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("shouldDisplayRealtimeRiskingMessage"), Some("false"))
      createMockToGetApplicationDetails(mockIhtConnector, Some(CommonBuilder.buildApplicationDetailsWithAllAssets.copy(
        allAssets = Some(CommonBuilder.buildAllAssets.copy(money = None)))))
      createMockToGetRealtimeRiskMessage(mockIhtConnector)

      val result = declarationController.onPageLoad()(createFakeRequest())

      status(result) mustBe OK
    }


    "respond with OK on page load for valueLessThanNilRateBand, single executor and show risk message where message not found in messages file" in {

      val testRiskMessage = "Risk message is present"

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(CommonBuilder.buildRegistrationDetails)))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("declarationType"), Some("valueLessThanNilRateBand"))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("isMultipleExecutor"), Some("false"))
      createMockToGetApplicationDetails(mockIhtConnector, Some(CommonBuilder.buildApplicationDetailsWithAllAssets.copy(
        allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(money = None)))))
      createMockToGetRealtimeRiskMessage(mockIhtConnector, Some(testRiskMessage))

      val rd = CommonBuilder.buildRegistrationDetailsWithDeceasedDetails

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))


      val result = declarationController.onPageLoad()(createFakeRequest())

      contentAsString(result) must include(testRiskMessage)
      status(result) mustBe OK

    }

    "respond with OK on page load for valueLessThanNilRateBand, single executor and show risk message where message is found in messages file" in {

      val testRiskMessage = messagesApi("iht.application.declaration.risking.money.message")

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(CommonBuilder.buildRegistrationDetails)))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("declarationType"), Some("valueLessThanNilRateBand"))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("isMultipleExecutor"), Some("false"))
      createMockToGetApplicationDetails(mockIhtConnector, Some(CommonBuilder.buildApplicationDetailsWithAllAssets.copy(
        allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(money = None)))))
      createMockToGetRealtimeRiskMessage(mockIhtConnector, Some(testRiskMessage))

      val rd = CommonBuilder.buildRegistrationDetailsWithDeceasedDetails

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val deceasedName = rd.deceasedDetails.map(_.name).getOrElse("")

      val result = declarationController.onPageLoad()(createFakeRequest())
      val expectedRiskMessage = messagesApi("iht.application.declaration.risking.money.message.amended", deceasedName)

      contentAsString(result) must include(expectedRiskMessage)
      status(result) mustBe OK

    }

    "respond with NOT_IMPLEMENTED on page submit for valueLessThanNilRateBand, multiple executor, tick in box" in {

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
      createMockToGetSingleValueFromCache(mockCachingConnector, same("declarationType"), Some("valueLessThanNilRateBand"))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("isMultipleExecutor"), Some("true"))
      createMockToGetApplicationDetails(mockIhtConnector)
      createMockToSaveApplicationDetails(mockIhtConnector)
      createMockToSubmitApplication(mockIhtConnector)
      createMockToGetProbateDetails(mockIhtConnector)
      createMockToStoreProbateDetailsInCache(mockCachingConnector)

      mockForApplicationStatus(ApplicationStatus.AwaitingReturn)

      val applicantDetailsForm1 = declarationForm.fill(true)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(applicantDetailsForm1.data.toSeq: _*)

      val result = declarationController.onSubmit()(request)
      status(result) mustBe SEE_OTHER
    }

    "respond with redirect on page submit for valueLessThanNilRateBand, single executor" in {

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
      createMockToGetSingleValueFromCache(mockCachingConnector, same("declarationType"), Some("valueLessThanNilRateBand"))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("isMultipleExecutor"), Some("false"))
      createMockToGetApplicationDetails(mockIhtConnector)
      createMockToSaveApplicationDetails(mockIhtConnector)
      createMockToSubmitApplication(mockIhtConnector)
      createMockToGetProbateDetails(mockIhtConnector)
      createMockToGetCaseDetails(mockIhtConnector)
      createMockToStoreProbateDetailsInCache(mockCachingConnector)

      val result = declarationController.onSubmit()(createFakeRequest())
      status(result) mustBe SEE_OTHER
    }

    "respond with INTERNAL_SERVER_ERROR when exception contains 'Service Unavailable' and statusCode 502" in {
      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.failed(UpstreamErrorResponse("Service Unavailable", 502, 502)))

      val result = declarationController.onSubmit()(createFakeRequest())
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "respond with redirect to Received Declaration page after the successful submission " in {

      createMockToGetSingleValueFromCache(mockCachingConnector, same("declarationType"), Some("valueLessThanNilRateBand"))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("isMultipleExecutor"), Some("false"))
      createMockToGetApplicationDetails(mockIhtConnector)
      createMockToSaveApplicationDetails(mockIhtConnector)
      createMockToSubmitApplication(mockIhtConnector)
      createMockToGetProbateDetails(mockIhtConnector)
      createMockToStoreProbateDetailsInCache(mockCachingConnector)

      mockForApplicationStatus(ApplicationStatus.AwaitingReturn)

      val result = declarationController.onSubmit()(createFakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(iht.controllers.application.declaration.routes.DeclarationReceivedController.onPageLoad().url))
    }

    "must increase the stats counter metric for ADDITIONAL_EXECUTOR_APP " in {
      val regDetails = CommonBuilder.buildRegistrationDetails copy(ihtReference=Some("XXX"),
        coExecutors = Seq(CommonBuilder.buildCoExecutor,
          CommonBuilder.buildCoExecutor))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(regDetails)))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("declarationType"), Some("valueLessThanNilRateBand"))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("isMultipleExecutor"), Some("true"))
      createMockToGetApplicationDetails(mockIhtConnector)
      createMockToSaveApplicationDetails(mockIhtConnector)
      createMockToSubmitApplication(mockIhtConnector)
      createMockToGetProbateDetails(mockIhtConnector)
      createMockToGetCaseDetails(mockIhtConnector)
      createMockToStoreProbateDetailsInCache(mockCachingConnector)

      val applicantDetailsForm1 = declarationForm.fill(true)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(applicantDetailsForm1.data.toSeq: _*)

      val result = declarationController.onSubmit()(request)
      status(result) mustBe SEE_OTHER
    }

    "must redirect to non lead executor page when submitApplication return 403" in {
      val regDetails = CommonBuilder.buildRegistrationDetails copy(ihtReference=Some("XXX"),
        coExecutors = Seq(CommonBuilder.buildCoExecutor,
          CommonBuilder.buildCoExecutor))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(regDetails)))
      createMockToGetApplicationDetails(mockIhtConnector)
      createMockToSaveApplicationDetails(mockIhtConnector)
      when(mockIhtConnector.submitApplication(any(),any(),any())(any(), any())).thenReturn(Future.successful(None))
      mockForApplicationStatus(ApplicationStatus.AwaitingReturn)

      val result = declarationController.onSubmit()(createFakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(iht.controllers.routes.NonLeadExecutorController.onPageLoad().url)
    }

    "statsSource should return Some assets only" in {
      val result = declarationController.statsSource(
        CommonBuilder.buildApplicationDetails, BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(1))
      result must be(Some(StatsSource.ASSETS_ONLY_APP))
    }

    "statsSource should return Some assets and debts only" in {
      val result = declarationController.statsSource(
        CommonBuilder.buildApplicationDetails, BigDecimal(0), BigDecimal(1), BigDecimal(0), BigDecimal(1))
      result must be(Some(StatsSource.ASSETS_AND_DEBTS_ONLY_APP))
    }

    "statsSource should return Some assets, debts, exemptions and TNRB" in {
      val tnrbEligibiltyModel = TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None)
      val ad = CommonBuilder.buildApplicationDetails copy( increaseIhtThreshold = Some(tnrbEligibiltyModel) )
      val result = declarationController.statsSource(ad, BigDecimal(1), BigDecimal(1), BigDecimal(1), BigDecimal(1))
      result must be(Some(StatsSource.ASSET_DEBTS_EXEMPTIONS_TNRB_APP))
    }

    "statsSource should return None" in {
      val result = declarationController.statsSource(CommonBuilder.buildApplicationDetails, BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(-1))
      result must be(None)
    }

    "calculateReasonForBeingBelowLimit should return Some ReasonForBeingBelowLimitSpouseCivilPartnerOrCharityExemption" in {

      val ad = CommonBuilder.buildApplicationDetails copy(
        allAssets =Some(AllAssets(money=Some(ShareableBasicEstateElement(Some(BigDecimal(325001)), Some(BigDecimal(0)))))),
        allExemptions = Some(AllExemptions(partner=Some(PartnerExemption(None, None, None, None, None, None, Some(BigDecimal(2))))))
      )

      val result = declarationController.calculateReasonForBeingBelowLimit(ad)
      result must be(Some(ControllerHelper.ReasonForBeingBelowLimitSpouseCivilPartnerOrCharityExemption))
    }

    "calculateReasonForBeingBelowLimit should return Some ReasonForBeingBelowLimitTNRB" in {

      val ad = CommonBuilder.buildApplicationDetails copy(
        allAssets =Some(AllAssets(money=Some(ShareableBasicEstateElement(Some(BigDecimal(326001)), Some(BigDecimal(0)))))),
        allExemptions = Some(AllExemptions(partner=Some(PartnerExemption(None, None, None, None, None, None, Some(BigDecimal(2))))))
      )

      val result = declarationController.calculateReasonForBeingBelowLimit(ad)
      result must be(Some(ControllerHelper.ReasonForBeingBelowLimitTNRB))
    }

    "calculateReasonForBeingBelowLimit should return None" in {
      val ad = CommonBuilder.buildApplicationDetails copy(
        allAssets =Some(AllAssets(money=Some(ShareableBasicEstateElement(Some(BigDecimal(1000001)), Some(BigDecimal(0))))))
        )
      val result = declarationController.calculateReasonForBeingBelowLimit(ad)
      result must be(None)
    }

    "submissionException should return errorServiceUnavailable" in {
      val ex = new GatewayTimeoutException("")
      declarationController.submissionException(ex) must be(ControllerHelper.errorServiceUnavailable)
    }

    "submissionException should return errorServiceUnavailable also" in {
      Seq("Request timed out", "Connection refused", "Service Unavailable", ControllerHelper.desErrorCode503) foreach { exceptionText =>
        val ex = new RuntimeException(exceptionText)
        declarationController.submissionException(ex) must be(ControllerHelper.errorServiceUnavailable)
      }
    }

    "submissionException should return errorRequestTimeout" in {
      Seq(ControllerHelper.desErrorCode502, ControllerHelper.desErrorCode504) foreach { exceptionText =>
        val ex = new RuntimeException(exceptionText)
        declarationController.submissionException(ex) must be(ControllerHelper.errorRequestTimeOut)
      }
    }

    "submissionException should return errorSystem" in {
      val ex = new RuntimeException("test")
      declarationController.submissionException(ex) must be(ControllerHelper.errorSystem)
    }

    "submissionException should return errorSystem also" in {
      val ex = new Throwable
      declarationController.submissionException(ex) must be(ControllerHelper.errorSystem)
    }

    "on submit redirect to estate overview if the application status is not AwaitingReturn" in {
      createMockToGetSingleValueFromCache(mockCachingConnector, same("declarationType"), Some("valueLessThanNilRateBand"))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("isMultipleExecutor"), Some("false"))
      createMockToGetApplicationDetails(mockIhtConnector)
      createMockToSaveApplicationDetails(mockIhtConnector)
      createMockToSubmitApplication(mockIhtConnector)
      createMockToGetProbateDetails(mockIhtConnector)

      mockForApplicationStatus(ApplicationStatus.InReview)

      val result = declarationController.onSubmit()(createFakeRequest())

      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      declarationController.onPageLoad(createFakeRequest()))
  }

  "on submit make sure error handling code works" in {
    createMockToGetSingleValueFromCache(mockCachingConnector, same("declarationType"), Some("valueLessThanNilRateBand"))
    createMockToGetSingleValueFromCache(mockCachingConnector, same("isMultipleExecutor"), Some("false"))
    createMockToGetApplicationDetails(mockIhtConnector)
    createMockToSaveApplicationDetails(mockIhtConnector)
    createMockToSubmitApplication(mockIhtConnector)
    createMockToGetProbateDetails(mockIhtConnector)
    mockForApplicationStatus(ApplicationStatus.InReview, true)

    when(mockIhtConnector.getRealtimeRiskingMessage(any(), any())(any())).thenReturn(Future.successful(Some("result")))

    val result = declarationController.onSubmit()(createFakeRequest())

    status(result) mustBe BAD_REQUEST
  }

}
