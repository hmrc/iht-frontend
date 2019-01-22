/*
 * Copyright 2019 HM Revenue & Customs
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
import iht.testhelpers.MockFormPartialRetriever
import iht.views.HtmlSpec
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.FakeRequest
import play.api.test.Helpers._
import iht.forms.FilterForms._
import iht.constants.Constants._
import iht.constants.IhtProperties._
import uk.gov.hmrc.play.partials.FormPartialRetriever

class DomicileControllerTest extends ApplicationControllerTest with HtmlSpec {

  override implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val request = FakeRequest()
  val messages = messagesApi.preferred(request)

  def controller = new DomicileController {
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "Domicile Controller" must {

    "show the Domicile page when access by an unauthorised person" in {
      val result = controller.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) must be(OK)

      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first
      titleElement.text() must be(messagesApi("page.iht.registration.deceasedPermanentHome.title"))
    }

    "show an error if no radio button is selected" in {
      val request = createFakeRequestWithBody(isAuthorised = false, data = domicileForm(messages).data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) must be(BAD_REQUEST)

      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "errors")
    }

    "redirect to the Jointly Owned page if 'England or Wales' is selected" in {
      val form = domicileForm(messages).fill(Some(englandOrWales))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.filter.routes.FilterJointlyOwnedController.onPageLoad().url))
    }

    "redirect to the 'Scotland transition' page if 'Scotland' is selected" in {
      val form = domicileForm(messages).fill(Some(scotland))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.filter.routes.TransitionController.onPageLoadScotland().url))
    }

    "redirect to the 'Northern Ireland transition' page if 'Northern Ireland' is selected" in {
      val form = domicileForm((messages)).fill(Some(northernIreland))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.filter.routes.TransitionController.onPageLoadNorthernIreland().url))
    }

    "redirect to the 'Other country transition' page if 'Other country' is selected" in {
      val form = domicileForm(messages).fill(Some(otherCountry))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.filter.routes.TransitionController.onPageLoadOtherCountry().url))
    }
  }
}
