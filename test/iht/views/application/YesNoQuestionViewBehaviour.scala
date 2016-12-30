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
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest

trait YesNoQuestionViewBehaviour extends ViewTestHelper {

  def pageTitle: String
  def browserTitle: String
  def guidanceParagraphs: Set[String]
  def yesNoQuestionText: String
  def returnLinkId: String = "return-button"
  def returnLinkText: String
  def returnLinkTargetUrl: Call

  def fixture() = new {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()
    val view: String = ""
    val doc: Document = new Document("")
  }

  def debtsElement() = {
    "have the correct title" in {
      val f = fixture()
      val doc = asDocument(f.view)
      val headers = doc.getElementsByTag("h1")
      headers.size shouldBe 1
      headers.first.text() shouldBe pageTitle
    }

    "have the correct browser title" in {
      val f = fixture()
      browserTitleShouldBeCorrect(f.view, browserTitle)
    }

    "show the correct guidance paragraphs" in {
      val f = fixture()
      for (paragraph <- guidanceParagraphs) messagesShouldBePresent(f.view, paragraph)
    }

    "show the correct yes/no question text" in {
      val f = fixture()
      messagesShouldBePresent(f.view, yesNoQuestionText)
    }

    "show the Save and continue button" in {
      val f = fixture()
      val saveAndContinueButton = f.doc.getElementById("save-continue")
      saveAndContinueButton.text() shouldBe Messages("iht.saveAndContinue")
    }

    "show the correct return link with text" in {
      val f = fixture()
      val returnLink = f.doc.getElementById(returnLinkId)
      returnLink.attr("href") shouldBe returnLinkTargetUrl.url
      returnLink.text() shouldBe returnLinkText
    }
  }
}
