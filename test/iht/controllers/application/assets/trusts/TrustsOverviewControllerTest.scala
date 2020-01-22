/*
 * Copyright 2020 HM Revenue & Customs
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

package iht.controllers.application.assets.trusts

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

/**
 * Created by jennygj on 30/06/16.
 */
class TrustsOverviewControllerTest extends ApplicationControllerTest {


  protected abstract class TestController extends FrontendController(mockControllerComponents) with TrustsOverviewController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  "TrustsOverviewControllerTest" must {



    def trustsOverviewController = new TestController  {
      override val authConnector = mockAuthConnector
      override val cachingConnector = mockCachingConnector
      override val ihtConnector = mockIhtConnector
      override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
    }

    def trustsOverviewControllerNotAuthorised = new TestController  {
      override val authConnector = mockAuthConnector
      override val cachingConnector = mockCachingConnector
      override val ihtConnector = mockIhtConnector
      override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = trustsOverviewController.onPageLoad(createFakeRequest())
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

      val result = trustsOverviewControllerNotAuthorised.onPageLoad(createFakeRequest(false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "display the correct content title" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = trustsOverviewController.onPageLoad(createFakeRequest())
      status(result) mustBe (OK)
      contentAsString(result) must include(messagesApi("iht.estateReport.assets.heldInTrust.title"))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      trustsOverviewController.onPageLoad(createFakeRequest()))
  }
}
