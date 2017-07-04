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

package iht.viewmodels.application

import iht.connector.IhtConnector
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.assets.Properties
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.testhelpers.CommonBuilder
import iht.testhelpers.CommonBuilder._
import iht.testhelpers.MockObjectBuilder._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.http.HeaderCarrier


/**
  * Created by vineet on 29/09/16.
  */
class DeclarationViewModelTest extends ApplicationControllerTest{

  var mockIhtConnector = mock[IhtConnector]
  implicit val hc = new HeaderCarrier

  before {
    mockIhtConnector = mock[IhtConnector]
    createMockToGetApplicationDetails(mockIhtConnector)
    createMockToGetRealtimeRiskMessage(mockIhtConnector, None)
  }

  "DeclarationViewModel" must {

    val appDetails = buildApplicationDetails
    val form = Form(single("value" -> boolean))
    val nino = ""

    "create declaration form" in {

      val regDetails = buildRegistrationDetails
      implicit val fakeRequest = createFakeRequest()

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector).declarationForm should be (form)
    }

    "create executors" in {

      val executors = Seq(buildCoExecutor, buildCoExecutor)
      val regDetails = buildRegistrationDetails.copy(coExecutors = executors)
      implicit val fakeRequest = createFakeRequest()

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector).executors should be (executors)
    }

    "create isMultipleExecutor with true value when there are more than one executors " in {

      val executors = Seq(buildCoExecutor, buildCoExecutor)
      val regDetails = buildRegistrationDetails.copy(coExecutors = executors)
      implicit val fakeRequest = createFakeRequest()

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector).isMultipleExecutor should be (true)
    }

    "create isMultipleExecutor with false value when there is one executor " in {

      val executors = Nil
      val regDetails = buildRegistrationDetails.copy(coExecutors = executors)
      implicit val fakeRequest = createFakeRequest()

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector).isMultipleExecutor should be (false)
    }

    "create isMultipleExecutor with true value when there is only one coExecutor " in {

      val executors = Seq(buildCoExecutor)
      val regDetails = buildRegistrationDetails.copy(coExecutors = executors)
      implicit val fakeRequest = createFakeRequest()

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector).isMultipleExecutor should be (true)
    }

    "create registrationDetails " in {

      val regDetails = buildRegistrationDetails
      implicit val fakeRequest = createFakeRequest()

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector).registrationDetails should be (regDetails)
    }

    "return correct riskMessageFromEdh when there is no money entered" in {

      val regDetails = buildRegistrationDetails
      implicit val fakeRequest = createFakeRequest()
      val riskMessage = Some("Risk Message")
      createMockToGetRealtimeRiskMessage(mockIhtConnector, riskMessage)

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector).riskMessageFromEdh should be (riskMessage)
    }

    "return correct riskMessageFromEdh when there is money value of zero" in {
      val regDetails = buildRegistrationDetails
      implicit val fakeRequest = createFakeRequest()
      val riskMessage = Some("Risk Message")
      createMockToGetRealtimeRiskMessage(mockIhtConnector, riskMessage)

      val appDetails = {
        val allAssets = buildAllAssets.copy(
          money = Some(buildShareableBasicElementExtended.copy(
            Some(BigDecimal(0)), None, Some(true), Some(false)))
        )
        buildApplicationDetails.copy(allAssets = Some(allAssets))
      }

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector).riskMessageFromEdh should be (riskMessage)
    }

    "return None when there is money value which is non-zero" in {
      val regDetails = buildRegistrationDetails
      implicit val fakeRequest = createFakeRequest()
      val riskMessage = Some("Risk Message")
      createMockToGetRealtimeRiskMessage(mockIhtConnector, riskMessage)

      val appDetails = {
        val allAssets = buildAllAssets.copy(
          money = Some(buildShareableBasicElementExtended.copy(
            Some(BigDecimal(10)), None, Some(true), Some(false)))
        )
        buildApplicationDetails.copy(allAssets = Some(allAssets))
      }

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector).riskMessageFromEdh shouldBe None
    }

    "return correct riskMessageFromEdh when there is money value of None" in {
      val regDetails = buildRegistrationDetails
      implicit val fakeRequest = createFakeRequest()
      val riskMessage = Some("Risk Message")
      createMockToGetRealtimeRiskMessage(mockIhtConnector, riskMessage)

      val appDetails = {
        val allAssets = buildAllAssets.copy(
          money = Some(buildShareableBasicElementExtended.copy(
            None, None, Some(true), Some(false)))
        )
        buildApplicationDetails.copy(allAssets = Some(allAssets))
      }

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector).riskMessageFromEdh should be (riskMessage)
    }

    "return None when there is an error in getting risk message" in {
      val regDetails = buildRegistrationDetails
      implicit val fakeRequest = createFakeRequest()
      val riskMessage = Some("Risk Message")
      createMockToGetRealtimeRiskMessage(mockIhtConnector, riskMessage)
      when(mockIhtConnector.getRealtimeRiskingMessage(any(), any())(any()))
          .thenThrow(new RuntimeException("error"))

      a[RuntimeException] shouldBe thrownBy{
        DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector).riskMessageFromEdh
      }
    }



    "return correct riskMessageFromEdh when the money entered is of value 0 and shared value is None" in {

      val regDetails = buildRegistrationDetails
      val ad = appDetails.copy(allAssets = Some(buildAllAssets.copy(
                                          money = Some(ShareableBasicEstateElement(
                                            value = Some(BigDecimal(0)),
                                            shareValue = None,
                                            isOwned = None,
                                            isOwnedShare = None)))))

      implicit val fakeRequest = createFakeRequest()
      val riskMessage = Some("Risk Message")
      createMockToGetRealtimeRiskMessage(mockIhtConnector, riskMessage)

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector).riskMessageFromEdh should be (riskMessage)
    }

    "return correct riskMessageFromEdh when the money owed and shared value are entered as 0" in {

      val regDetails = buildRegistrationDetails
      val ad = appDetails.copy(allAssets = Some(buildAllAssets.copy(
                                money = Some(ShareableBasicEstateElement(
                                  value = Some(BigDecimal(0)),
                                  shareValue = Some(BigDecimal(0)),
                                  isOwned = None,
                                  isOwnedShare = None)))))

      implicit val fakeRequest = createFakeRequest()
      val riskMessage = Some("Risk Message")
      createMockToGetRealtimeRiskMessage(mockIhtConnector, riskMessage)

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector).riskMessageFromEdh should be (riskMessage)
    }

    "return riskMessageFromEdh as None when there is non zero money value" in {

      val regDetails = buildRegistrationDetails
      val ad = appDetails.copy(allAssets = Some(buildAllAssets.copy(
                                money = Some(ShareableBasicEstateElement(
                                  value = Some(BigDecimal(100)),
                                  shareValue = None,
                                  isOwned = None,
                                  isOwnedShare = None)))))

      implicit val fakeRequest = createFakeRequest()

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector).riskMessageFromEdh should be (empty)
    }

  }
}
