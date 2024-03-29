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

package iht.controllers

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.views.html.application.non_lead_executor
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class NonLeadExecutorControllerTest extends ApplicationControllerTest {
  implicit val hc = HeaderCarrier()
  implicit val fakedMessagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  protected abstract class TestController extends FrontendController(mockControllerComponents) with NonLeadExecutorController {
    override implicit val appConfig: AppConfig = mockAppConfig
    override val nonLeadExecutorView: non_lead_executor = app.injector.instanceOf[non_lead_executor]

  }

  def controller = new TestController {
    override def messagesApi: MessagesApi = fakedMessagesApi

  }

  val request = FakeRequest()

  "onPageLoad" must {
    "execute successfully" in {
      val result = controller.onPageLoad(request)
      status(result) mustBe OK
    }
  }
}
