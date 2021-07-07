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

package iht.controllers

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import iht.views.html.iv.wayfinderpages.{login_pass, verification_pass}
import play.api.i18n.{Lang, Messages}
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class IVWayfinderControllerTest extends ApplicationControllerTest {

  override implicit val messages: Messages = mockControllerComponents.messagesApi.preferred(Seq(Lang.defaultLang)).messages
  protected abstract class TestController extends FrontendController(mockControllerComponents) with IVWayfinderController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val loginPassView: login_pass = app.injector.instanceOf[login_pass]
    override val verificationPassView: verification_pass = app.injector.instanceOf[verification_pass]
  }

  def ivWayfinderController = new TestController {
    override val authConnector = mockAuthConnector

  }

  "IV Wayfinder login-pass" must {

    "respond with a 303 when login-pass hasn't got an auth connection" in {
      val result = ivWayfinderController.loginPass()(createFakeRequest(isAuthorised = false, authRetrieveNino = false))

      status(result) mustBe 303
    }

    "respond with a 200 when login-pass page is served" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy (ihtReference = Some(""))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = ivWayfinderController.loginPass()(createFakeRequest(authRetrieveNino = false))

      status(result) mustBe 200
    }

  }

  "IV Wayfinder verification-pass" must {

    "respond with a 303 when verification-pass hasn't got an auth connection" in {
      val result = ivWayfinderController.verificationPass()(createFakeRequest(isAuthorised = false, authRetrieveNino = false))

      status(result) mustBe 303
    }

    "respond with a 200 when verification-pass page is served" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy (ihtReference = Some(""))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = ivWayfinderController.verificationPass()(createFakeRequest(authRetrieveNino = false))

      status(result) mustBe 200
    }

  }
}

