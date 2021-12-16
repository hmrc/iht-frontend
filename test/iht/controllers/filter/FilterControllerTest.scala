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
import iht.constants.Constants._
import iht.controllers.application.ApplicationControllerTest
import iht.forms.FilterForms._
import iht.views.HtmlSpec
import iht.views.html.filter.filter_view
import play.api.i18n.{Lang, MessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class FilterControllerTest extends ApplicationControllerTest with HtmlSpec {

  implicit val fakedMessagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val request = FakeRequest()

  protected abstract class TestController extends FrontendController(mockControllerComponents) with FilterController {
    override implicit val appConfig: AppConfig = mockAppConfig
    override val filterViewView: filter_view = app.injector.instanceOf[filter_view]
  }

  def controller = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override def messagesApi: MessagesApi = fakedMessagesApi

  }

  "FilterController" must {
    "show the 'what do you want to do' page when accessed by an unauthorized person" in {
      val result = controller.onPageLoad()(createFakeRequest(isAuthorised = false, Some("")))
      status(result) must be(OK)

      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first
      titleElement.text() must be(fakedMessagesApi("iht.whatDoYouWantToDo"))
    }

    "show an error if no radio  button is selected" in {
      val request = createFakeRequestWithBody(isAuthorised = false, data = filterForm(messages).data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) must be(BAD_REQUEST)

      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "errors")
    }

    "redirect to sign in with Estate reports as the ultimate destination when the page is submitted with the continue choice selected" in {
      val form = filterForm(messages).fill(Some(continueEstateReport))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url))
    }

    "redirect to the already started page as the ultimate destination when the page is submitted with the already started choice selected" in {
      val form = filterForm(messages).fill(Some(alreadyStarted))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.registration.routes.RegistrationChecklistController.onPageLoad().url))
    }

    "redirect to the agent page when the page is submitted with agent choice selected" in {
      val form = filterForm(messages).fill(Some(agent))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.filter.routes.AgentController.onPageLoad().url))
    }

    "redirect to the domicile page when the page is submitted with register choice selected" in {
      val form = filterForm(messages).fill(Some(register))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.filter.routes.DomicileController.onPageLoad().url))
    }

    "switch languages when the referer ends with .cy" in {
      val request = createFakeRequest(isAuthorised = false, Some("test.cy"))
      val welshMessages = app.injector.instanceOf[MessagesApi].preferred(Seq(Lang("cy")))
      val result = controller.onPageLoad(request)
      contentAsString(result) mustNot include(messages("site.registration.title"))
      contentAsString(result) must include(welshMessages("site.registration.title"))
    }
  }
}
