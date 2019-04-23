/*
 * Copyright 2019 HM Revenue & Customs
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

package iht.utils

import iht.config.AppConfig
import iht.connector.CachingConnector
import iht.constants.Constants
import iht.testhelpers.{CommonBuilder, MockObjectBuilder}
import iht.{FakeIhtApp, TestUtils}
import org.mockito.ArgumentMatchers.same
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Call
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.Mockito._

import scala.concurrent.ExecutionContext.Implicits.global

class ExemptionsGuidanceHelperTest extends FakeIhtApp with MockitoSugar with TestUtils with BeforeAndAfterEach with ExemptionsGuidanceHelper with MockObjectBuilder {

  val mockCachingConnector = mock[CachingConnector]
  implicit val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val appConfig = mockAppConfig

  implicit val headerCarrier = FakeHeaders()
  implicit val request = FakeRequest()
  implicit val hc = new HeaderCarrier

  lazy val ihtRef = "ihtRef"
  
  lazy val finalDestinationURL = "final-destination-url"

  override def beforeEach(): Unit = {
    reset(mockCachingConnector)
    super.beforeEach()
  }

  val GuidanceAppropriateMessage = "guidance is to be shown (the estate value is over the current lower " +
    "threshold and the guidance flag is not set and the continue URL is not set in key store)"

  "guidanceRedirect" must {
    "return None when the estate value is under the current lower threshold" in {
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      val result = await(guidanceRedirect(Call("",""),
        CommonBuilder.buildApplicationDetailsUnderLowerThreshold(ihtRef), mockCachingConnector))
      result mustBe None
    }

    "return None when the estate value is over the current lower threshold but the guidance seen flag is set" in {
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      val result = await(guidanceRedirect(Call("",""),
        CommonBuilder.buildApplicationDetailsOverLowerThresholdAndGuidanceSeenFlagSet(ihtRef), mockCachingConnector))
      result mustBe None
    }

    "return None when the estate value is over the current lower threshold and the guidance seen flag is not set but " +
      "the continue URL is set in the key store" in {

      createMockToGetSingleValueFromCache(mockCachingConnector,
        same(Constants.ExemptionsGuidanceContinueUrlKey), Some("url"))
      createMockToDeleteKeyFromCache(mockCachingConnector, Constants.ExemptionsGuidanceContinueUrlKey)

      val result = await(guidanceRedirect(Call("",""),
        CommonBuilder.buildApplicationDetailsOverLowerThresholdAndGuidanceSeenFlagNotSet(ihtRef), mockCachingConnector))
      result mustBe None
    }

    "return guidance page call when " + GuidanceAppropriateMessage in {
      val guidancePage: Call = iht.controllers.application.exemptions.routes.ExemptionsGuidanceIncreasingThresholdController.onPageLoad(ihtRef)

      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      val result = await(guidanceRedirect(Call("",""),
        CommonBuilder.buildApplicationDetailsOverLowerThresholdAndGuidanceSeenFlagNotSet(ihtRef), mockCachingConnector))
      result mustBe Some(guidancePage)
    }

    "set the final destination url in keystore when " + GuidanceAppropriateMessage in {
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      await(guidanceRedirect(Call(Constants.GET,finalDestinationURL),
        CommonBuilder.buildApplicationDetailsOverLowerThresholdAndGuidanceSeenFlagNotSet(ihtRef), mockCachingConnector))

      val storeResult = verifyAndReturnStoredSingleValue(mockCachingConnector)
      storeResult._1 mustBe Constants.ExemptionsGuidanceContinueUrlKey
      storeResult._2 mustBe finalDestinationURL
    }

    "clear the final destination url from the keystore when returning None" in {
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      await(guidanceRedirect(Call("",""), CommonBuilder.buildApplicationDetailsUnderLowerThreshold(ihtRef), mockCachingConnector))

      val deleteResult  = await(verifyDeleteKeyFromStore(mockCachingConnector))

      deleteResult mustBe Constants.ExemptionsGuidanceContinueUrlKey
    }
  }

  "finalDestination" must {
    "return EstateOverviewController onPageLoad url if there is nothing in the key store" in {
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
      val expectedResult = iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef)
      val result = await(finalDestination(ihtRef, mockCachingConnector))
      result mustBe expectedResult
    }

    "return final-destination-url as a call when it is stored in the key store" in {
      createMockToGetSingleValueFromCache(mockCachingConnector,
        same(Constants.ExemptionsGuidanceContinueUrlKey), Some(finalDestinationURL))
      val result = await(finalDestination(ihtRef, mockCachingConnector))
      result mustBe Call(Constants.GET, finalDestinationURL)
    }
  }
}
