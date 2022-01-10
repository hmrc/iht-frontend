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

package iht.controllers.filter

import iht.controllers.application.ApplicationControllerTest
import iht.views.HtmlSpec
import iht.views.html.filter.use_checker
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class UseCheckerControllerTest extends ApplicationControllerTest with HtmlSpec {

  implicit val fakedMessagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  protected abstract class TestController extends FrontendController(mockControllerComponents) with UseCheckerController {
    override val useCheckerView: use_checker = app.injector.instanceOf[use_checker]
  }

  def controller: TestController = new TestController {
    override def messagesApi: MessagesApi = fakedMessagesApi
  }

  "Use Checker Controller" must {

    "show the use checker page when accessed by an unauthorised person" in {
      val result = controller.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) must be(OK)

      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first
      titleElement.text() must be(messages("page.iht.filter.useChecker.heading"))
    }
  }
}
