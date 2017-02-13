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

import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest

trait YesNoQuestionViewBehaviour[A] extends ApplicationPageBehaviour[A] {

  // Legacy defs - to be removed
  def yesNoQuestionText: String = "???"
  def returnLinkId: String = "return-button"
  def returnLinkText: String = ""
  def returnLinkTargetUrl: Call = Call("","")
  def fixture() = new {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()
    val view: String = ""
    val doc: Document = new Document("")
  }
  //

  def pageTitle: String

  def browserTitle: String

  def guidanceParagraphs: Set[String]

  def formTarget: Call = Call("","")

  /**
    * Assumes that the Call for the continue button has been set up as CommonBuilder.DefaultCall1.
    */
  def yesNoQuestion() = {
    applicationPageWithErrorSummaryBox()

    "show the correct guidance paragraphs" in {
      for (paragraph <- guidanceParagraphs) messagesShouldBePresent(view, paragraph)
    }

    "show the correct yes/no question text" in {
      doc.getElementById("yes-label").text shouldBe Messages("iht.yes")
      doc.getElementById("no-label").text shouldBe Messages("iht.no")
    }

    "show the Save/Continue button with the correct target" in {
      //val continueButton = doc.getElementsByClass("button")
      val continueButton = doc.getElementById("save-continue")
      doc.getElementsByTag("form").attr("action") shouldBe formTarget.url
    }
  }

//  def yesNoQuestionWithLegend(questionLegend: => String) = {
//    yesNoQuestion()
//
//    "show the correct question text" in {
//      doc.getElementById("yes-no-question-legend").text shouldBe questionLegend
//    }
//  }

//  /**
//    * Assumes that the Call for the continue button has been set up as CommonBuilder.DefaultCall1.
//    * Assumes that the Call for the cancel link has been set up as CommonBuilder.DefaultCall2.
//    */
//  def yesNoQuestionWithCancelLink() = {
//    yesNoQuestion()
//
//    "show the Cancel link with the correct target" in {
//      val continueButton = doc.getElementById("continue-button")
//      continueButton.`val` shouldBe Messages("iht.continue")
//      doc.getElementsByTag("form").attr("action") shouldBe CommonBuilder.DefaultCall1.url
//
//      val cancelLink = doc.getElementById("cancel-button")
//      cancelLink.attr("href") shouldBe CommonBuilder.DefaultCall2.url
//      cancelLink.text() shouldBe Messages("site.link.cancel")
//    }
//  }
}


// Original content of this file:-
//
//  def pageTitle: String
//  def browserTitle: String
//  def guidanceParagraphs: Set[String]
//  def yesNoQuestionText: String
//  def returnLinkId: String = "return-button"
//  def returnLinkText: String
//  def returnLinkTargetUrl: Call
//
//  def fixture() = new {
//    implicit val request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()
//    val view: String = ""
//    val doc: Document = new Document("")
//  }
//
//  def yesNoQuestion() = {
//    "have the correct title" in {
//      val f = fixture()
//      val doc = asDocument(f.view)
//      val headers = doc.getElementsByTag("h1")
//      headers.size shouldBe 1
//      headers.first.text() shouldBe pageTitle
//    }
//
//    "have the correct browser title" in {
//      val f = fixture()
//      browserTitleShouldBeCorrect(f.view, browserTitle)
//    }
//
//    "show the correct guidance paragraphs" in {
//      val f = fixture()
//      for (paragraph <- guidanceParagraphs) messagesShouldBePresent(f.view, paragraph)
//    }
//
//    "show the correct yes/no question text" in {
//      val f = fixture()
//      messagesShouldBePresent(f.view, yesNoQuestionText)
//    }
//
//    "show the Save and continue button" in {
//      val f = fixture()
//      val saveAndContinueButton = f.doc.getElementById("save-continue")
//      saveAndContinueButton.text() shouldBe Messages("iht.saveAndContinue")
//    }
//
//    "show the correct return link with text" in {
//      val f = fixture()
//      val returnLink = f.doc.getElementById(returnLinkId)
//      returnLink.attr("href") shouldBe returnLinkTargetUrl.url
//      returnLink.text() shouldBe returnLinkText
//    }
//  }
//}
