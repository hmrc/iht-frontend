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

package iht.controllers.application.exemptions.charity

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.ApplicationDetails
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils.CommonHelper
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

class CharityDetailsOverviewControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def charityDetailsOverviewController = new CharityDetailsOverviewController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
  }

  def charityDetailsOverviewControllerNotAuthorised = new CharityDetailsOverviewController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
  }

  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier

  "Charity details overview controller" must {

    "return OK on page load" in {

      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = charityDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
    }

    "display the page title on page load" in {
      val result = charityDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(
        messagesApi("page.iht.application.exemptions.overview.charity.detailsOverview.title"))
    }

    "throw RuntimeException when charity ID is accessed that does not exist" in {

      val firstCharity = iht.testhelpers.CommonBuilder.charity
      val applicationModel = new ApplicationDetails(charities = Seq(firstCharity))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationModel),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      intercept[RuntimeException] {
        val result = charityDetailsOverviewController.onEditPageLoad("2")(createFakeRequest())
        status(result) should be (INTERNAL_SERVER_ERROR)
      }
    }

    "contain appropriate routes when page is accessed with charity ID 1" in {

      val firstCharity = iht.testhelpers.CommonBuilder.charity.copy(name = None, totalValue = None)
      val applicationModel = new ApplicationDetails(charities = Seq(firstCharity))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationModel),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = charityDetailsOverviewController.onEditPageLoad("1")(createFakeRequest())
      contentAsString(result) should include(
        iht.controllers.application.exemptions.charity.routes.CharityNameController.onEditPageLoad("1").url)
      contentAsString(result) should include(
        iht.controllers.application.exemptions.charity.routes.CharityValueController.onEditPageLoad("1").url)
      contentAsString(result) should include(
        iht.controllers.application.exemptions.charity.routes.CharityNumberController.onEditPageLoad("1").url)
    }

    "contain appropriate routes when page is accessed with charity ID 2" in {

      val firstCharity = iht.testhelpers.CommonBuilder.charity.copy(name = None, totalValue = None)
      val secondCharity = iht.testhelpers.CommonBuilder.charity.copy(id=Some("2"), number = None, totalValue = None)
      val applicationModel = new ApplicationDetails(charities = Seq(firstCharity, secondCharity))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationModel),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = charityDetailsOverviewController.onEditPageLoad("2")(createFakeRequest())
      contentAsString(result) should include(
        iht.controllers.application.exemptions.charity.routes.CharityNameController.onEditPageLoad("2").url)
      contentAsString(result) should include(
        iht.controllers.application.exemptions.charity.routes.CharityValueController.onEditPageLoad("2").url)
      contentAsString(result) should include(
        iht.controllers.application.exemptions.charity.routes.CharityNameController.onEditPageLoad("2").url)
    }


    "contain appropriate routes when page is accessed with no charity ID" in {

      val applicationModel = new ApplicationDetails
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationModel),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = charityDetailsOverviewController.onPageLoad()(createFakeRequest())
      contentAsString(result) should include(
        iht.controllers.application.exemptions.charity.routes.CharityNameController.onPageLoad().url)
      contentAsString(result) should include(
        iht.controllers.application.exemptions.charity.routes.CharityValueController.onPageLoad().url)
      contentAsString(result) should include(
        iht.controllers.application.exemptions.charity.routes.CharityNameController.onPageLoad().url)
    }

  }
}
