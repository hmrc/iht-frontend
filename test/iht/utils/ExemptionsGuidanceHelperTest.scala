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

package iht.utils

import iht.{FakeIhtApp, TestUtils}
import iht.connector.CachingConnector
import iht.constants.Constants
import iht.testhelpers.{CommonBuilder, MockObjectBuilder}
import org.mockito.Matchers._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class ExemptionsGuidanceHelperTest extends UnitSpec with MockitoSugar with FakeIhtApp with TestUtils with BeforeAndAfter {

  var mockCachingConnector = mock[CachingConnector]

  implicit val headerCarrier = FakeHeaders()
  implicit val request = FakeRequest()
  implicit val hc = new HeaderCarrier

  lazy val ihtRef = "ihtRef"
  
  lazy val finalDestinationURL = "final-destination-url"

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  val GuidanceAppropriateMessage = "guidance is to be shown (the estate value is over the current lower " +
    "threshold and the guidance flag is not set and the continue URL is not set in key store)"

  "guidanceRedirect" must {
    "return None when the estate value is under the current lower threshold" in {
      MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      val result = await(ExemptionsGuidanceHelper.guidanceRedirect(Call("",""),
        CommonBuilder.buildApplicationDetailsUnderLowerThreshold(ihtRef), mockCachingConnector))
      result shouldBe None
    }

    "return None when the estate value is over the current lower threshold but the guidance seen flag is set" in {
      MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      val result = await(ExemptionsGuidanceHelper.guidanceRedirect(Call("",""),
        CommonBuilder.buildApplicationDetailsOverLowerThresholdAndGuidanceSeenFlagSet(ihtRef), mockCachingConnector))
      result shouldBe None
    }

    "return None when the estate value is over the current lower threshold and the guidance seen flag is not set but " +
      "the continue URL is set in the key store" in {

      MockObjectBuilder.createMockToGetSingleValueFromCache(mockCachingConnector,
        same(Constants.ExemptionsGuidanceContinueUrlKey), Some("url"))
      MockObjectBuilder.createMockToDeleteKeyFromCache(mockCachingConnector, Constants.ExemptionsGuidanceContinueUrlKey)

      val result = await(ExemptionsGuidanceHelper.guidanceRedirect(Call("",""),
        CommonBuilder.buildApplicationDetailsOverLowerThresholdAndGuidanceSeenFlagNotSet(ihtRef), mockCachingConnector))
      result shouldBe None
    }

    "return guidance page call when " + GuidanceAppropriateMessage in {
      val guidancePage: Call = iht.controllers.application.exemptions.routes.ExemptionsGuidanceIncreasingThresholdController.onPageLoad(ihtRef)

      MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      val result = await(ExemptionsGuidanceHelper.guidanceRedirect(Call("",""),
        CommonBuilder.buildApplicationDetailsOverLowerThresholdAndGuidanceSeenFlagNotSet(ihtRef), mockCachingConnector))
      result shouldBe Some(guidancePage)
    }

    "set the final destination url in keystore when " + GuidanceAppropriateMessage in {
      MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      await(ExemptionsGuidanceHelper.guidanceRedirect(Call(Constants.GET,finalDestinationURL),
        CommonBuilder.buildApplicationDetailsOverLowerThresholdAndGuidanceSeenFlagNotSet(ihtRef), mockCachingConnector))

      val storeResult = verifyAndReturnStoredSingleValue(mockCachingConnector)
      storeResult._1 shouldBe Constants.ExemptionsGuidanceContinueUrlKey
      storeResult._2 shouldBe finalDestinationURL
    }

    "clear the final destination url from the keystore when returning None" in {
      MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      await(ExemptionsGuidanceHelper.guidanceRedirect(Call("",""), CommonBuilder.buildApplicationDetailsUnderLowerThreshold(ihtRef), mockCachingConnector))

      val deleteResult  = await(verifyDeleteKeyFromStore(mockCachingConnector))

      deleteResult shouldBe Constants.ExemptionsGuidanceContinueUrlKey
    }
  }

  "finalDestination" must {
    "return EstateOverviewController onPageLoad url if there is nothing in the key store" in {
      MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
      val expectedResult = iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef)
      val result = await(ExemptionsGuidanceHelper.finalDestination(ihtRef, mockCachingConnector))
      result shouldBe expectedResult
    }

    "return final-destination-url as a call when it is stored in the key store" in {
      MockObjectBuilder.createMockToGetSingleValueFromCache(mockCachingConnector,
        same(Constants.ExemptionsGuidanceContinueUrlKey), Some(finalDestinationURL))
      val result = await(ExemptionsGuidanceHelper.finalDestination(ihtRef, mockCachingConnector))
      result shouldBe Call(Constants.GET, finalDestinationURL)
    }
  }
}
