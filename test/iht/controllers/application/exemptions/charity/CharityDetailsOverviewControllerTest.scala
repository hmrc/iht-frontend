/*
 * Copyright 2021 HM Revenue & Customs
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

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.ApplicationDetails
import iht.testhelpers.CommonBuilder
import iht.views.html.application.exemption.charity.charity_details_overview
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class CharityDetailsOverviewControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with CharityDetailsOverviewController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val charityDetailsOverviewView: charity_details_overview = app.injector.instanceOf[charity_details_overview]
  }

  def charityDetailsOverviewController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector
  }

  def charityDetailsOverviewControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

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
      status(result) must be(OK)
    }

    "display the page title on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = charityDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) must be(OK)
      contentAsString(result) must include(
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
        status(result) must be (INTERNAL_SERVER_ERROR)
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
      contentAsString(result) must include(
        iht.controllers.application.exemptions.charity.routes.CharityNameController.onEditPageLoad("1").url)
      contentAsString(result) must include(
        iht.controllers.application.exemptions.charity.routes.CharityValueController.onEditPageLoad("1").url)
      contentAsString(result) must include(
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
      contentAsString(result) must include(
        iht.controllers.application.exemptions.charity.routes.CharityNameController.onEditPageLoad("2").url)
      contentAsString(result) must include(
        iht.controllers.application.exemptions.charity.routes.CharityValueController.onEditPageLoad("2").url)
      contentAsString(result) must include(
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
      contentAsString(result) must include(
        iht.controllers.application.exemptions.charity.routes.CharityNameController.onPageLoad().url)
      contentAsString(result) must include(
        iht.controllers.application.exemptions.charity.routes.CharityValueController.onPageLoad().url)
      contentAsString(result) must include(
        iht.controllers.application.exemptions.charity.routes.CharityNameController.onPageLoad().url)
    }
  }
}
