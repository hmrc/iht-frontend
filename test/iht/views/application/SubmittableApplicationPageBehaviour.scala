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

package iht.views.application

import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable

trait SubmittableApplicationPageBehaviour[A] extends ApplicationPageBehaviour {

  def view: String = formToView(form).toString

  def form: Form[A]

  def formToView: Form[A] => Appendable

  override def linkHash: String = ""

  def applicationPageWithErrorSummaryBox() = {
    applicationPage()
    "display the 'There's a problem' box if there's an error" in {
      val newForm = form.withError(FormError("field", "error message"))
      val document = asDocument(formToView(newForm).toString)
      document.getElementById("errors").children.first.text shouldBe messagesApi("error.problem")
    }
  }

  def applicationPageInEditModeWithErrorSummaryBox(view: => Document, cancelUrl: => Call) = {
    applicationPageWithErrorSummaryBox()

    "have a continue and cancel link in edit mode" in {
      val continueLink = view.getElementById("continue-button")
      continueLink.attr("value") shouldBe messagesApi("iht.continue")

      val cancelLink = view.getElementById("cancel-button")
      if(linkHash > ""){
        cancelLink.attr("href") shouldBe cancelUrl.url + "#" + linkHash
      } else {
        cancelLink.attr("href") shouldBe cancelUrl.url
      }
      cancelLink.text() shouldBe messagesApi("site.link.cancel")
    }
  }
}
