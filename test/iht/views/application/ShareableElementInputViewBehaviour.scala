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
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat.Appendable
import iht.utils.CommonHelper._

trait ShareableElementInputViewBehaviour[A] extends ViewTestHelper {

  def pageTitle: String
  def browserTitle: String
  def questionTitle: String
  def valueQuestion: String
  def hasValueQuestionHelp: Boolean
  def valueQuestionHelp: String
  def returnLinkText: String
  def returnLinkUrl: String
  def formTarget: Option[Call]
  def valueInputBoxId: String = "value"
  def shareValueInputBoxId: String = "shareValue"
  def linkHash: String = ""

  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()
  def view: String = formToView(form).toString
  def doc: Document = asDocument(view)
  def form:Form[A] = ???
  def formToView:Form[A] => Appendable = ???

  def yesNoValueView() = viewBehaviour(valueInputBoxId)

  def yesNoValueViewJoint() = viewBehaviour(shareValueInputBoxId)

  private def viewBehaviour(valueId: String) = {
    "have no message keys in html" in {
      noMessageKeysShouldBePresent(view)
    }

    "have the correct title" in {
      titleShouldBeCorrect(view, pageTitle)
    }

    "have the correct browser title" in {
      browserTitleShouldBeCorrect(view, browserTitle)
    }

    "show the correct Yes/No question" in {
      val legend = doc.getElementsByTag("legend").first
      legend.text shouldBe questionTitle
    }

    "have yes and no radio buttons" in {
      assertRenderedById(doc, "yes")
      assertRenderedById(doc, "no")
    }

    "have a value input box" in {
      assertRenderedById(doc, valueId)
    }

    "show the correct value question" in {
      labelShouldBe(doc, s"$valueId-container", valueQuestion)
    }

    if (hasValueQuestionHelp) {
      "show the correct help text for the value question" in {
        labelHelpTextShouldBe(doc, s"$valueId-container", valueQuestionHelp)
      }
    }

    "show a Save and continue button" in {
      assertRenderedById(doc, "save-continue")
    }

    "show a return link" in {
      val link = doc.getElementById("return-button")
      link.text shouldBe returnLinkText
      link.attr("href") shouldBe addFragmentIdentifierToUrl(returnLinkUrl, linkHash)
    }

    if (formTarget.isDefined) {
      "have the Save/Continue button with the correct target" in {
        formTarget.foreach { target =>
          doc.getElementsByTag("form").attr("action") shouldBe target.url
        }
      }
    }
  }

  def yesNoValueViewWithErrorSummaryBox(): Unit = {

    viewBehaviour(valueInputBoxId)

    "display the 'There's a problem' box if there's an error" in {
      val newForm = form.withError(FormError("field", "error message"))
      val document = asDocument(formToView(newForm).toString)
      document.getElementById("errors").children.first.text shouldBe messagesApi("error.problem")
    }
  }

  def yesNoValueViewJointWithErrorSummaryBox(): Unit = {

    viewBehaviour(shareValueInputBoxId)

    "display the 'There's a problem' box if there's an error" in {
      val newForm = form.withError(FormError("field", "error message"))
      val document = asDocument(formToView(newForm).toString)
      document.getElementById("errors").children.first.text shouldBe messagesApi("error.problem")
    }
  }
}
