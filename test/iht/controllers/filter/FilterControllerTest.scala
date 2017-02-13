/*
 * Copyright 2017 HM Revenue & Customs
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

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.FilterForms._
import iht.views.HtmlSpec
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import iht.constants.Constants._

/**
  * Created by adwelly on 21/10/2016.
  */
class FilterControllerTest extends ApplicationControllerTest with HtmlSpec {

  override implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def controller = new FilterController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "FilterController" must {
    "show the 'what do you want to do' page when accessed by an unauthorized person" in {
      val result = controller.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) should be(OK)

      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first
      titleElement.text() should be(Messages("iht.whatDoYouWantToDo"))
    }

    "show an error if no radio  button is selected" in {
      val request = createFakeRequestWithBody(isAuthorised = false, data = filterForm.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) should be(BAD_REQUEST)

      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "errors")
    }

    "redirect to sign in with Estate reports as the ultimate destination when the page is submitted with the continue choice selected" in {
      val form = filterForm.fill(Some(continueEstateReport))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(iht.controllers.home.routes.IhtHomeController.onPageLoad().url))
    }

    "redirect to the already started page as the ultimate destination when the page is submitted with the already started choice selected" in {
      val form = filterForm.fill(Some(alreadyStarted))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(iht.controllers.registration.routes.RegistrationChecklistController.onPageLoad().url))
    }

    "redirect to the agent page when the page is submitted with agent choice selected" in {
      val form = filterForm.fill(Some(agent))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(iht.controllers.filter.routes.AgentController.onPageLoad().url))
    }

    "redirect to the domicile page when the page is submitted with register choice selected" in {
      val form = filterForm.fill(Some(register))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(iht.controllers.filter.routes.DomicileController.onPageLoad().url))
    }
  }
}
