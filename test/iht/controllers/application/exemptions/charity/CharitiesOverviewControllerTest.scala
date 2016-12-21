/*
 * Copyright 2016 HM Revenue & Customs
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
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils._
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

class CharitiesOverviewControllerTest extends ApplicationControllerTest {

  implicit val hc = new HeaderCarrier()
  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  val applicationDetailsWithCharityLeftTrue = CommonBuilder.buildApplicationDetails.copy(
    allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
      charity = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = Some(true))))))

  def charitiesOverviewController = new CharitiesOverviewController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
  }

  def charitiesOverviewControllerNotAuthorised = new CharitiesOverviewController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
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
      status(result) should be(OK)
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
      status(result) should be(OK)
      contentAsString(result) should include(charityName)
      contentAsString(result) should include(CommonHelper.numberWithCommas(charityValue))
    }

    "redirect to ida login page on PageLoad if the user is not logged in" in {
      val result = charitiesOverviewControllerNotAuthorised.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "onPageLoad validate test Internal Server error" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      a[RuntimeException] shouldBe thrownBy {
        await(charitiesOverviewController.onPageLoad()(createFakeRequest()))
      }
    }

    "show no charities added message when no charities are added in exemptions" in {

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsWithCharityLeftTrue),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = charitiesOverviewController.onPageLoad()(createFakeRequest())
      contentAsString(result) should include(CommonHelper.escapeApostrophes(
        Messages("page.iht.application.exemptions.charityOverview.noCharities.text")))
    }

    "show no charity name and number given message when no charitie name and number added in charities" in {
      val charityName = None
      val charityNumber = None
      val charityValue = BigDecimal(2345)
      val applicationDetails = applicationDetailsWithCharityLeftTrue.copy(
        charities = Seq(Charity(Some("1"), charityName, charityNumber, Some(charityValue)))
      )

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = charitiesOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(CommonHelper.numberWithCommas(charityValue))
      contentAsString(result) should include(CommonHelper.escapeApostrophes(
        Messages("site.noCharityNameAndNumberGiven")))
    }

    "show no charity name given message when no charitie name added but charity number added in charities" in {
      val charityName = None
      val charityNumber = Some("C12345")
      val charityValue = BigDecimal(2345)
      val applicationDetails = applicationDetailsWithCharityLeftTrue.copy(
        charities = Seq(Charity(Some("1"), charityName, charityNumber, Some(charityValue)))
      )

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = charitiesOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(CommonHelper.numberWithCommas(charityValue))
      contentAsString(result) should include(CommonHelper.escapeApostrophes(
        Messages("site.noCharityNameGiven")))
    }

    "show no charity number given message when no charity number added but charity name added in charities" in {
      val charityName = Some("Charity1")
      val charityNumber = None
      val charityValue = BigDecimal(2345)
      val applicationDetails = applicationDetailsWithCharityLeftTrue.copy(
        charities = Seq(Charity(Some("1"), charityName, charityNumber, Some(charityValue)))
      )

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = charitiesOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(CommonHelper.numberWithCommas(charityValue))
      contentAsString(result) should include(CommonHelper.escapeApostrophes(
        Messages("site.noCharityNumberGiven")))
    }

    "show asset left to charity question text" in {

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsWithCharityLeftTrue),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = charitiesOverviewController.onPageLoad()(createFakeRequest())
      contentAsString(result) should include(Messages("iht.estateReport.exemptions.charities.assetLeftToCharity.question"))
    }
  }
}
