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

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.views.HtmlSpec
import iht.views.html.filter.deceased_before_2022
import iht.forms.FilterForms.deceasedBefore2022Form
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class DeceasedBefore2022ControllerTest extends ApplicationControllerTest with HtmlSpec {

  implicit val fakedMessagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  protected abstract class TestController extends FrontendController(mockControllerComponents) with DeceasedBefore2022Controller {
    override implicit val appConfig: AppConfig = mockAppConfig
    override val deceasedBefore2022View: deceased_before_2022 = app.injector.instanceOf[deceased_before_2022]
  }

  def controller: TestController = new TestController {
    override def messagesApi: MessagesApi = fakedMessagesApi
  }

  "Deceased Before 2022 Controller" must {

    "show the deceased before 2022 page when accessed by an unauthorised person" in {
      val result = controller.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(OK)

      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first
      titleElement.text() must be(messages("page.iht.filter.deceased.before.2022.heading"))
    }

    "show an error if no radio button is selected" in {
      val request = createFakeRequestWithBody(isAuthorised = false, data = deceasedBefore2022Form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) must be(BAD_REQUEST)

      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "errors")
    }

    "redirect to the Use Checker page if 'no' is selected" in {
      val form = deceasedBefore2022Form.fill(Some(false))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.filter.routes.UseCheckerController.onPageLoad.url))
    }

    "redirect to the Jointly Owned page if 'yes' is selected" in {
      val form = deceasedBefore2022Form.fill(Some(true))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.filter.routes.FilterJointlyOwnedController.onPageLoad.url))

    }
  }
}
