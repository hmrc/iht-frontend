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

package iht.views.application.debts

import iht.views.ViewTestHelper
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat.Appendable

trait DebtsElementViewBehaviour[A] extends ViewTestHelper {

  def pageTitle: String
  def browserTitle: String
  def guidanceParagraphs: Set[String]
  def yesNoQuestionText: String
  def inputValueFieldLabel: String
  def inputValueFieldHintText: String = "default hint"
  def returnLinkId: String = "return-button"
  def returnLinkText: String = Messages("site.link.return.debts")
  def returnLinkTargetUrl: Call = iht.controllers.application.debts.routes.DebtsOverviewController.onPageLoad
  def linkHash: String = ""

  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()
  def view: String = formToView(form).toString
  def doc: Document = asDocument(view)
  def form:Form[A] = ???
  def formToView:Form[A] => Appendable = ???

  def debtsElement() = {
    "have the correct title" in {
      val headers = doc.getElementsByTag("h1")
      headers.size shouldBe 1
      headers.first.text() shouldBe pageTitle
    }

    "have the correct browser title" in {
      browserTitleShouldBeCorrect(view, browserTitle)
    }

    "show the correct guidance paragraphs" in {
      for (paragraph <- guidanceParagraphs) messagesShouldBePresent(view, paragraph)
    }

    "show the correct yes/no question text" in {
      messagesShouldBePresent(view, yesNoQuestionText)
    }

    "show the correct input field value label" in {
      messagesShouldBePresent(view, inputValueFieldLabel)
    }

    "show the correct input field value hint text (if there is any)" in {
      if(inputValueFieldHintText == "default hint") {
        assertNotContainsText(doc, inputValueFieldHintText)
      } else {
        messagesShouldBePresent(view, inputValueFieldHintText)
      }
    }

    "show the Save and continue button" in {
      val saveAndContinueButton = doc.getElementById("save-continue")
      saveAndContinueButton.text() shouldBe Messages("iht.saveAndContinue")
    }

    "show the correct return link with text" in {
      val returnLink = doc.getElementById(returnLinkId)
      if(linkHash > "") {
        returnLink.attr("href") shouldBe (returnLinkTargetUrl.url + "#" + linkHash)
      } else {
        returnLink.attr("href") shouldBe returnLinkTargetUrl.url
      }
      returnLink.text() shouldBe returnLinkText
    }
  }
}
