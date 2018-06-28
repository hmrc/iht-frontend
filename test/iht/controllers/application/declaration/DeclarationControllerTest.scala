/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.ControllerHelper
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.assets._
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.models.application.exemptions.{AllExemptions, PartnerExemption}
import iht.models.application.tnrb.TnrbEligibiltyModel
import iht.models.enums.StatsSource
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import iht.testhelpers.MockObjectBuilder._
import iht.utils.ApplicationStatus
import org.mockito.ArgumentMatchers._
import play.api.http.Status.OK
import org.mockito.Mockito.when
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier, Upstream5xxResponse}

import scala.concurrent.Future

class DeclarationControllerTest extends ApplicationControllerTest {

  // Implicit objects required by play framework.
  implicit val headerCarrier = FakeHeaders()
  implicit val request = FakeRequest()
  implicit val hc = new HeaderCarrier

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]
  val ihtReferenceNo = "XXX"

  def declarationController = new DeclarationController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override lazy val metrics:Metrics = mock[Metrics]

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def declarationControllerNotAuthorised = new DeclarationController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override lazy val metrics:Metrics = mock[Metrics]

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def mockForApplicationStatus(requiredStatus: String) = {
    val regDetails = CommonBuilder.buildRegistrationDetails copy(
      ihtReference=Some(ihtReferenceNo),
      status = requiredStatus
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

      await(declarationController.realTimeRiskingMessage(appDetails, regDetails.ihtReference.get, "AB123456C", testConnector)) shouldBe Some("Risk Message")
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

      await(declarationController.realTimeRiskingMessage(appDetails, regDetails.ihtReference.get, "AB123456C", testConnector)) shouldBe Some("Risk Message")
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

      await(declarationController.realTimeRiskingMessage(appDetails, regDetails.ihtReference.get, "AB123456C", testConnector)) shouldBe None
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

      await(declarationController.realTimeRiskingMessage(appDetails, regDetails.ihtReference.get, "AB123456C", testConnector)) shouldBe riskMessage
    }

    "return None when there is an error in getting risk message" in {
      val testConnector = mock[IhtConnector]
      val regDetails = CommonBuilder.buildRegistrationDetails
      val appDetails = CommonBuilder.buildApplicationDetails
      val riskMessage = Some("Risk Message")
      createMockToGetRealtimeRiskMessage(testConnector, riskMessage)
      when(testConnector.getRealtimeRiskingMessage(any(), any())(any()))
        .thenThrow(new RuntimeException("error"))

      a[RuntimeException] shouldBe thrownBy{
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

      await(declarationController.realTimeRiskingMessage(ad, regDetails.ihtReference.get, "AB123456C", testConnector)) shouldBe riskMessage
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

      await(declarationController.realTimeRiskingMessage(ad, regDetails.ihtReference.get, "AB123456C", testConnector)) shouldBe riskMessage
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

      await(declarationController.realTimeRiskingMessage(ad, regDetails.ihtReference.get, "AB123456C", mockIhtConnector)) shouldBe empty
    }

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = declarationController.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = declarationControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result).get should be(loginUrl)
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

      status(result) shouldBe OK
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

      contentAsString(result) should include(testRiskMessage)
      status(result) shouldBe OK

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

      contentAsString(result) should include(expectedRiskMessage)
      status(result) shouldBe OK

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
      status(result) shouldBe SEE_OTHER
    }

    "respond with redirect on page submit for valueLessThanNilRateBand, single executor" in {

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
      createMockToGetSingleValueFromCache(mockCachingConnector, same("declarationType"), Some("valueLessThanNilRateBand"))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("isMultipleExecutor"), Some("false"))
      createMockToGetApplicationDetails(mockIhtConnector)
      createMockToSaveApplicationDetails(mockIhtConnector)
      createMockToSubmitApplication(mockIhtConnector)
      createMockToGetProbateDetails(mockIhtConnector)

      val result = declarationController.onSubmit()(createFakeRequest())
      status(result) shouldBe SEE_OTHER
    }

    "respond with INTERNAL_SERVER_ERROR when exception contains 'Service Unavailable' and upstreamResponseCode 502" in {
      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.failed(Upstream5xxResponse("Service Unavailable", 502, 502)))

      val result = declarationController.onSubmit()(createFakeRequest())
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "respond with redirect to Received Declaration page after the successful submission " in {

      createMockToGetSingleValueFromCache(mockCachingConnector, same("declarationType"), Some("valueLessThanNilRateBand"))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("isMultipleExecutor"), Some("false"))
      createMockToGetApplicationDetails(mockIhtConnector)
      createMockToSaveApplicationDetails(mockIhtConnector)
      createMockToSubmitApplication(mockIhtConnector)
      createMockToGetProbateDetails(mockIhtConnector)

      mockForApplicationStatus(ApplicationStatus.AwaitingReturn)

      val result = declarationController.onSubmit()(createFakeRequest())
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be(Some(iht.controllers.application.declaration.routes.DeclarationReceivedController.onPageLoad().url))
    }

    "should increase the stats counter metric for ADDITIONAL_EXECUTOR_APP " in {
      val regDetails = CommonBuilder.buildRegistrationDetails copy(ihtReference=Some("XXX"),
        coExecutors = Seq(CommonBuilder.buildCoExecutor,
          CommonBuilder.buildCoExecutor))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(regDetails)))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("declarationType"), Some("valueLessThanNilRateBand"))
      createMockToGetSingleValueFromCache(mockCachingConnector, same("isMultipleExecutor"), Some("true"))
      createMockToGetApplicationDetails(mockIhtConnector)
      createMockToSubmitApplication(mockIhtConnector)
      createMockToGetProbateDetails(mockIhtConnector)

      val applicantDetailsForm1 = declarationForm.fill(true)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(applicantDetailsForm1.data.toSeq: _*)

      val result = declarationController.onSubmit()(request)
      status(result) shouldBe SEE_OTHER
    }

    "statsSource should return Some assets only" in {
      val result = declarationController.statsSource(
        CommonBuilder.buildApplicationDetails, BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(1))
      result should be(Some(StatsSource.ASSETS_ONLY_APP))
    }

    "statsSource should return Some assets and debts only" in {
      val result = declarationController.statsSource(
        CommonBuilder.buildApplicationDetails, BigDecimal(0), BigDecimal(1), BigDecimal(0), BigDecimal(1))
      result should be(Some(StatsSource.ASSETS_AND_DEBTS_ONLY_APP))
    }

    "statsSource should return Some assets, debts, exemptions and TNRB" in {
      val tnrbEligibiltyModel = TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None)
      val ad = CommonBuilder.buildApplicationDetails copy( increaseIhtThreshold = Some(tnrbEligibiltyModel) )
      val result = declarationController.statsSource(ad, BigDecimal(1), BigDecimal(1), BigDecimal(1), BigDecimal(1))
      result should be(Some(StatsSource.ASSET_DEBTS_EXEMPTIONS_TNRB_APP))
    }

    "statsSource should return None" in {
      val result = declarationController.statsSource(CommonBuilder.buildApplicationDetails, BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(-1))
      result should be(None)
    }

    "calculateReasonForBeingBelowLimit should return Some ReasonForBeingBelowLimitSpouseCivilPartnerOrCharityExemption" in {

      val ad = CommonBuilder.buildApplicationDetails copy(
        allAssets =Some(AllAssets(money=Some(ShareableBasicEstateElement(Some(BigDecimal(325001)), Some(BigDecimal(0)))))),
        allExemptions = Some(AllExemptions(partner=Some(PartnerExemption(None, None, None, None, None, None, Some(BigDecimal(2))))))
      )

      val result = declarationController.calculateReasonForBeingBelowLimit(ad)
      result should be(Some(ControllerHelper.ReasonForBeingBelowLimitSpouseCivilPartnerOrCharityExemption))
    }

    "calculateReasonForBeingBelowLimit should return Some ReasonForBeingBelowLimitTNRB" in {

      val ad = CommonBuilder.buildApplicationDetails copy(
        allAssets =Some(AllAssets(money=Some(ShareableBasicEstateElement(Some(BigDecimal(326001)), Some(BigDecimal(0)))))),
        allExemptions = Some(AllExemptions(partner=Some(PartnerExemption(None, None, None, None, None, None, Some(BigDecimal(2))))))
      )

      val result = declarationController.calculateReasonForBeingBelowLimit(ad)
      result should be(Some(ControllerHelper.ReasonForBeingBelowLimitTNRB))
    }

    "calculateReasonForBeingBelowLimit should return None" in {
      val ad = CommonBuilder.buildApplicationDetails copy(
        allAssets =Some(AllAssets(money=Some(ShareableBasicEstateElement(Some(BigDecimal(1000001)), Some(BigDecimal(0))))))
        )
      val result = declarationController.calculateReasonForBeingBelowLimit(ad)
      result should be(None)
    }

    "submissionException should return errorServiceUnavailable" in {
      val ex = new GatewayTimeoutException("")
      declarationController.submissionException(ex) should be(ControllerHelper.errorServiceUnavailable)
    }

    "submissionException should return errorServiceUnavailable also" in {
      Seq("Request timed out", "Connection refused", "Service Unavailable", ControllerHelper.desErrorCode503) foreach { exceptionText =>
        val ex = new RuntimeException(exceptionText)
        declarationController.submissionException(ex) should be(ControllerHelper.errorServiceUnavailable)
      }
    }

    "submissionException should return errorRequestTimeout" in {
      Seq(ControllerHelper.desErrorCode502, ControllerHelper.desErrorCode504) foreach { exceptionText =>
        val ex = new RuntimeException(exceptionText)
        declarationController.submissionException(ex) should be(ControllerHelper.errorRequestTimeOut)
      }
    }

    "submissionException should return errorSystem" in {
      val ex = new RuntimeException("test")
      declarationController.submissionException(ex) should be(ControllerHelper.errorSystem)
    }

    "submissionException should return errorSystem also" in {
      val ex = new Throwable
      declarationController.submissionException(ex) should be(ControllerHelper.errorSystem)
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

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be(Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      declarationController.onPageLoad(createFakeRequest()))
  }
}
