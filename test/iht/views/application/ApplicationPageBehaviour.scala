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

import iht.views.ViewTestHelper
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat.Appendable

case class CancelComponent(target: Call, content: String)

trait ApplicationPageBehaviour[A] extends ViewTestHelper {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  def pageTitle: String

  def browserTitle: String

  def view: String = formToView(form).toString

  def doc: Document = asDocument(view)

  def form: Form[A]

  def formToView: Form[A] => Appendable

  def guidanceParagraphs: Set[String]

  def formTarget: Option[Call]

  def cancelComponent: Option[CancelComponent]

  def applicationPage() = {
    "have the correct title" in {
      titleShouldBeCorrect(view, pageTitle)
    }

    "have the correct browser title" in {
      browserTitleShouldBeCorrect(view, browserTitle)
    }

    "have a Continue button" in {
      doc.getElementById("save-continue").text shouldBe Messages("iht.saveAndContinue")
    }

    if (guidanceParagraphs.nonEmpty) {
      "show the correct guidance paragraphs" in {
        for (paragraph <- guidanceParagraphs) messagesShouldBePresent(view, paragraph)
      }
    }
    if (formTarget.isDefined) {
      "show the Save/Continue button with the correct target" in {
        formTarget.foreach { target =>
          doc.getElementsByTag("form").attr("action") shouldBe target.url
        }
      }
    }

    if (cancelComponent.isDefined) {
      "show the return link with the correct target and text" in {
        cancelComponent.foreach { attrib =>
          val cancelButton = doc.getElementById("return-button")
          cancelButton.attr("href") shouldBe attrib.target.url
          cancelButton.text() shouldBe attrib.content
        }
      }
    }
  }

  def applicationPageWithErrorSummaryBox() = {
    applicationPage()
    "display the 'There's a problem' box if there's an error" in {
      val newForm = form.withError(FormError("field", "error message"))
      val document = asDocument(formToView(newForm).toString)
      document.getElementById("errors").children.first.text shouldBe Messages("error.problem")
    }
  }

  def applicationPageInEditModeWithErrorSummaryBox(view: => Document, cancelUrl: => Call) = {
    applicationPageWithErrorSummaryBox()

    "have a continue and cancel link in edit mode" in {
      val continueLink = view.getElementById("continue-button")
      continueLink.attr("value") shouldBe Messages("iht.continue")

      val cancelLink = view.getElementById("cancel-button")
      cancelLink.attr("href") shouldBe cancelUrl.url
      cancelLink.text() shouldBe Messages("site.link.cancel")
    }
  }
}