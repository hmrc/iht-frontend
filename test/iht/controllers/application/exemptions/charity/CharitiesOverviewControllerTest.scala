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

package iht.controllers.application.exemptions

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.controllers.application.exemptions.charity.CharitiesOverviewController
import iht.models.application.exemptions.Charity
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder, ContentChecker}
import iht.testhelpers.MockObjectBuilder._
import iht.utils._
import org.mockito.Mockito.when
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.http.HeaderCarrier

class CharitiesOverviewControllerTest extends ApplicationControllerTest {

  implicit val hc = new HeaderCarrier()

  implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

  val applicationDetailsWithCharityLeftTrue = CommonBuilder.buildApplicationDetails.copy(
    allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
      charity = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = Some(true))))))

  def charitiesOverviewController = new CharitiesOverviewController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def charitiesOverviewControllerNotAuthorised = new CharitiesOverviewController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "Charity exemptions controller" must {
    "load page successfully" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsWithCharityLeftTrue),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = charitiesOverviewController.onPageLoad()(createFakeRequest())
      status(result) must be(OK)
    }

    "display charities on screen" in {
      val charityName = "A Charity I have donated to"
      val charityValue = BigDecimal(2345)
      val applicationDetails = applicationDetailsWithCharityLeftTrue.copy(
        charities = Seq(Charity(Some("1"), Some(charityName), Some("234423"), Some(charityValue)))
      )

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = charitiesOverviewController.onPageLoad()(createFakeRequest())
      status(result) must be(OK)
      contentAsString(result) must include(charityName)
      contentAsString(result) must include(CommonHelper.numberWithCommas(charityValue))
    }

    "redirect to ida login page on PageLoad if the user is not logged in" in {
      val result = charitiesOverviewControllerNotAuthorised.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "onPageLoad validate test Internal Server error" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      a[RuntimeException] mustBe thrownBy {
        await(charitiesOverviewController.onPageLoad()(createFakeRequest()))
      }
    }

    "show name validation message when one mocked on the charity object" in {
      val validationMessage = "Test validation message"
      val mockCharity = mock[Charity]
      val applicationDetails = applicationDetailsWithCharityLeftTrue.copy(
        charities = Seq(mockCharity)
      )

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      when(mockCharity.name).thenReturn(Some(CommonBuilder.DefaultString))
      when(mockCharity.number).thenReturn(Some(CommonBuilder.DefaultString))
      when(mockCharity.totalValue).thenReturn(Some(CommonBuilder.DefaultTotalAssets))
      when(mockCharity.id).thenReturn(Some(CommonBuilder.DefaultId))
      when(mockCharity.nameValidationMessage).thenReturn(Some(validationMessage))

      val result = charitiesOverviewController.onPageLoad()(createFakeRequest())
      status(result) must be(OK)
      contentAsString(result) must include(CommonBuilder.escapeApostrophes(validationMessage))
    }

    "show name when no name validation message mocked on the charity object" in {
      val charityName = "Test charity name"
      val mockCharity = mock[Charity]
      val applicationDetails = applicationDetailsWithCharityLeftTrue.copy(
        charities = Seq(mockCharity)
      )

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      when(mockCharity.name).thenReturn(Some(charityName))
      when(mockCharity.number).thenReturn(Some(CommonBuilder.DefaultString))
      when(mockCharity.totalValue).thenReturn(Some(CommonBuilder.DefaultTotalAssets))
      when(mockCharity.id).thenReturn(Some(CommonBuilder.DefaultId))
      when(mockCharity.nameValidationMessage).thenReturn(None)

      val result = charitiesOverviewController.onPageLoad()(createFakeRequest())
      status(result) must be(OK)
      contentAsString(result) must include(CommonBuilder.escapeApostrophes(charityName))
    }

    "show asset left to charity question text" in {
      val regDetails = buildRegistrationDetailsWithDeceasedAndIhtRefDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = regDetails,
        appDetails = Some(applicationDetailsWithCharityLeftTrue),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = charitiesOverviewController.onPageLoad()(createFakeRequest())
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("iht.estateReport.exemptions.charities.assetLeftToCharity.question",
                                                DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)))
    }
  }
}
