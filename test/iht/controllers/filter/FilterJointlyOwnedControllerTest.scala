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
import iht.forms.FilterForms._
import iht.testhelpers.MockFormPartialRetriever
import iht.views.HtmlSpec
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

class FilterJointlyOwnedControllerTest extends ApplicationControllerTest with HtmlSpec {

  implicit val fakedMessagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val request = FakeRequest()
  val messages = fakedMessagesApi.preferred(request)

  def controller = new FilterJointlyOwnedController {
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever

    override def messagesApi: MessagesApi = fakedMessagesApi
  }

  "Filter Jointly Owned Controller" must {

    "show the Filter Jointly Owned page when access by an unauthorised person" in {
      val result = controller.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) must be(OK)

      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first
      titleElement.text() must be(messages("page.iht.filter.jointlyowned.question"))
    }

    "show an error if no radio button is selected" in {
      val request = createFakeRequestWithBody(isAuthorised = false, data = filterJointlyOwnedForm(messages).data.toSeq)
      val result = controller.onSubmit()(request)

      status(result) must be(BAD_REQUEST)

      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "errors")
    }
    
  }

}
