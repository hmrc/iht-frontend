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

package iht.views.registration.executor

import iht.forms.registration.CoExecutorForms.othersApplyingForProbateForm
import iht.views.html.registration.executor.others_applying_for_probate
import iht.views.registration.YesNoQuestionViewBehaviour
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable

class OthersApplyingForProbateViewTest extends YesNoQuestionViewBehaviour[Option[Boolean]] {

  override def guidanceParagraphs = Set(Messages("page.iht.registration.others-applying-for-probate.description"))

  override def pageTitle = Messages("page.iht.registration.others-applying-for-probate.sectionTitle")

  override def browserTitle = Messages("page.iht.registration.others-applying-for-probate.browserTitle")

  override def form: Form[Option[Boolean]] = othersApplyingForProbateForm

  override def formToView: Form[Option[Boolean]] => Appendable = form => others_applying_for_probate(form, Call("", ""))

  "Others Applying for Probate View" must {
    behave like yesNoQuestion
  }

  "Others Applying for Probate View in Edit Mode" must {
    "have a continue and cancel link with correct targets" in {
      val view: Document = {
        implicit val request = createFakeRequest()
        val view = others_applying_for_probate(othersApplyingForProbateForm,
          Call("GET", "submit"), Some(Call("GET", "cancel"))).toString
        asDocument(view)
      }

      val continueLink = view.getElementById("continue-button")
      continueLink.attr("value") shouldBe Messages("iht.continue")

      val ook = view.getElementsByTag("form").attr("action") shouldBe "submit"

      val cancelLink = view.getElementById("cancel-button")
      cancelLink.attr("href") shouldBe "cancel"
      cancelLink.text() shouldBe Messages("site.link.cancel")
    }
  }
}
