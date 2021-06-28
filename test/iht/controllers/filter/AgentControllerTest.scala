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

package iht.controllers.filter

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.views.HtmlSpec
import iht.views.html.filter.agent_view
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class AgentControllerTest extends ApplicationControllerTest with HtmlSpec {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with AgentController {
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def controller: AgentController = new TestController {

    override def messagesApi: MessagesApi = mockControllerComponents.messagesApi

    override val agentViewView: agent_view = app.injector.instanceOf[agent_view]
  }

  "AgentController" must {
    "show the 'no change' page when accessed by an unauthorized person" in {
      val result = controller.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) must be(OK)

      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first
      titleElement.text() must be(mockControllerComponents.messagesApi("iht.noChangeToHowReportToHMRC"))
    }
  }
}