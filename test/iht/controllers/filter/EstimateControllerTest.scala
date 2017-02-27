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

import iht.controllers.application.ApplicationControllerTest
import iht.views.HtmlSpec
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import iht.forms.FilterForms._
import iht.constants.Constants._
import iht.constants.IhtProperties._

class EstimateControllerTest extends ApplicationControllerTest with HtmlSpec {

  override implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  def controller = new EstimateController {}

  "Estimate Controller" must {

    "show the Estimate page when access by an unauthorised person" in {
      val result = controller.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) should be(OK)

      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first
      titleElement.text() should be(messagesApi("iht.roughEstimateEstateWorth"))
    }

    "show an error if no radio button is selected" in {
      val request = createFakeRequestWithBody(isAuthorised = false, data = estimateForm.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) should be(BAD_REQUEST)

      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "errors")
    }

    "redirect to the Use Service page if 'Under £325,000' is selected" in {
      val form = estimateForm.fill(Some(under325000))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(iht.controllers.filter.routes.UseServiceController.onPageLoadUnder().url))
    }

    "redirect to the Use Service page if 'Between £325,000 and £1 million' is selected" in {
      val form = estimateForm.fill(Some(between325000and1million))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(iht.controllers.filter.routes.UseServiceController.onPageLoadOver().url))
    }

    "redirect to the 'Over £1 million transition' page if 'More than £1 million' is selected" in {
      val form = estimateForm.fill(Some(moreThan1million))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(iht.controllers.filter.routes.UseIHT400Controller.onPageLoad().url))
    }
  }
}
