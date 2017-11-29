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

package iht.views.ihtHelpers.custom

import iht.models.QuestionnaireModel
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.html.ihtHelpers.custom.questionnaire_form
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.data.Forms.{boolean, mapping, number, optional, text}
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.HtmlFormat

class QuestionnaireFormViewTest extends ViewTestHelper {

  private lazy val viewAsDocument: Document = {
    implicit val request = createFakeRequest()

    val call: Call = CommonBuilder.DefaultCall1
    val form = // scalastyle:off magic.number
      Form[QuestionnaireModel](mapping(
        "feelingAboutExperience" -> optional(number(1, 5)),
        "easytouse" -> optional(number(1, 5)),
        "howcanyouimprove" -> optional(text(minLength = 1, maxLength = 1200)),
        "fullName" -> optional(text),
        "contactDetails" -> optional(text),
        "stageInService" -> optional(text(minLength = 1, maxLength = 1200)),
        "intendToReturn" -> optional(boolean)
      )(QuestionnaireModel.apply)(QuestionnaireModel.unapply))

    val view: HtmlFormat.Appendable = questionnaire_form(form, call,
      includeIntendReturnQuestion=true)(request, messagesApi.preferred(request))
    asDocument(view.toString)
  }

  "QuestionnaireForm View" must {

    "have no message keys in html" in {
      noMessageKeysShouldBePresent(viewAsDocument.toString)
    }

    "have an input for users contact details and activity user has completed" in {
     viewAsDocument.toString should include(messagesApi("page.iht.questionnaire.contactDetails"))
     viewAsDocument.toString should include(messagesApi("page.iht.questionnaire.activity.question"))
    }

  }

}
