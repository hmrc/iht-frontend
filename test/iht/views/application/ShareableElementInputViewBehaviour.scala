/*
 * Copyright 2016 HM Revenue & Customs
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
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

trait ShareableElementInputViewBehaviour extends ViewTestHelper {

  def pageTitle: String
  def browserTitle: String
  def questionTitle: String
  def valueQuestion: String
  def hasValueQuestionHelp: Boolean
  def valueQuestionHelp: String
  def returnLinkText: String
  def returnLinkUrl: String

  def fixture() = new {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = null
    val view: String = null
    val doc: Document = null
  }

  def yesNoValueView() = viewBehaviour("value")

  def yesNoValueViewJoint() = viewBehaviour("shareValue")

  private def viewBehaviour(valueId: String) = {
    "have the correct title" in {
      val f = fixture()
      titleShouldBeCorrect(f.view, pageTitle)
    }

    "have the correct browser title" in {
      val f = fixture()
      browserTitleShouldBeCorrect(f.view, browserTitle)
    }

    "show the correct Yes/No question" in {
      val f = fixture()
      val legend = f.doc.getElementsByTag("legend").first
      legend.text shouldBe questionTitle
    }

    "have yes and no radio buttons" in {
      val f = fixture()
      assertRenderedById(f.doc, "yes")
      assertRenderedById(f.doc, "no")
    }

    "have a value input box" in {
      val f = fixture()
      assertRenderedById(f.doc, valueId)
    }

    "show the correct value question" in {
      val f = fixture()
      labelShouldBe(f.doc, s"$valueId-container", valueQuestion)
    }

    if (hasValueQuestionHelp) {
      "show the correct help text for the value question" in {
        val f = fixture()
        labelHelpTextShouldBe(f.doc, s"$valueId-container", valueQuestionHelp)
      }
    }

    "show a Save and continue button" in {
      val f = fixture()
      assertRenderedById(f.doc, "save-continue")
    }

    "show a return link" in {
      val f = fixture()
      val link = f.doc.getElementById("return-button")
      link.text shouldBe returnLinkText
      link.attr("href") shouldBe returnLinkUrl
    }
  }
}
