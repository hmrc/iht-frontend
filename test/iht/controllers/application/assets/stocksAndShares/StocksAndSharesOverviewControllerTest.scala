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

package iht.controllers.application.assets.stocksAndShares

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import iht.views.html.application.asset.stocksAndShares.stocks_and_shares_overview
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

/**
 * Created by yasar on 30/06/16.
 */
class StocksAndSharesOverviewControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with StocksAndSharesOverviewController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val stocksAndSharesOverviewView: stocks_and_shares_overview = app.injector.instanceOf[stocks_and_shares_overview]
  }

  "StocksAndSharesOverviewControllerTest" must {
    def stocksAndSharesOverviewController = new TestController {
      override val authConnector = mockAuthConnector
      override val cachingConnector = mockCachingConnector
      override val ihtConnector = mockIhtConnector
    }

    def stocksAndSharesOverviewControllerNotAuthorised = new TestController {
      override val authConnector = mockAuthConnector
      override val cachingConnector = mockCachingConnector
      override val ihtConnector = mockIhtConnector
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = stocksAndSharesOverviewController.onPageLoad(createFakeRequest())
      status(result) mustBe (OK)
    }

    "redirect to login page on PageLoad if the user is not logged in" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = stocksAndSharesOverviewControllerNotAuthorised.onPageLoad(createFakeRequest(false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      stocksAndSharesOverviewController.onPageLoad(createFakeRequest()))
  }
  
}
