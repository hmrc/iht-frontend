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

package iht.controllers

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.MockFormPartialRetriever
import play.api.i18n.MessagesApi
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class SessionManagementControllerTest extends ApplicationControllerTest {

  implicit val fakedMessagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  protected abstract class TestController extends FrontendController(mockControllerComponents) with SessionManagementController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def ihtMainController = new TestController {
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
    override def messagesApi: MessagesApi = fakedMessagesApi
    override def authConnector: AuthConnector = mockAuthConnector
  }

  implicit val request = FakeRequest()

  "iht main controller" must {
    "sign out correctly" in {
      val result = ihtMainController.signOut()(request)
      status(result) mustBe OK
    }

    "keep alive method is working" in {
      val result = ihtMainController.keepAlive(createFakeRequest(isAuthorised = false))
      status(result) mustBe SEE_OTHER
    }
  }
}
