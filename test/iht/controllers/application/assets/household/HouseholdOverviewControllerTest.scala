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

package iht.controllers.application.assets.household

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import iht.views.html.application.asset.household.household_overview
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class HouseholdOverviewControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with HouseholdOverviewController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val householdOverviewView: household_overview = app.injector.instanceOf[household_overview]
  }

  def householdOverviewController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  val allAssets=CommonBuilder.buildAllAssets
  val applicationDetails=CommonBuilder.buildApplicationDetails copy (allAssets = Some(allAssets))

  "HouseholdOverviewController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = householdOverviewController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {

      createMocksForApplication(cachingConnector= mockCachingConnector,
        ihtConnector = mockIhtConnector ,
        appDetails = Some(applicationDetails),
        getAppDetails = true)

      val result = householdOverviewController.onPageLoad (createFakeRequest())
      status(result) mustBe (OK)
      contentAsString(result) must include(messagesApi("iht.estateReport.assets.householdAndPersonalItems.title"))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      householdOverviewController.onPageLoad(createFakeRequest()))
  }
}
