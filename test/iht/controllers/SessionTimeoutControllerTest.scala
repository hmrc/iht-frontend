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
import iht.testhelpers.MockFormPartialRetriever
import play.api.http.Status._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers.{status => playStatus}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class SessionTimeoutControllerTest extends ApplicationControllerTest {
  implicit val hc = HeaderCarrier()
  implicit val fakedMessagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  protected abstract class TestController extends FrontendController(mockControllerComponents) with SessionTimeoutController {
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def controller = new TestController {
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever

    override def messagesApi: MessagesApi = fakedMessagesApi
  }

  implicit val request = FakeRequest()

  "onApplicationPageLoad method" must {
    "execute successfully in" in {
      val result = controller.onRegistrationPageLoad()(request)
      playStatus(result) mustBe OK
    }
  }

  "onRegistrationPageLoad method" must {
    "execute successfully in" in {
      val result = controller.onApplicationPageLoad()(request)
      playStatus(result) mustBe OK
    }

    "keep alive method is working" in {
      val result = controller.onSaveAndExitPageLoad(request)
      playStatus(result) mustBe OK
    }
  }
}
