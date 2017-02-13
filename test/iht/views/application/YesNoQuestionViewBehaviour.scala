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

import play.api.i18n.Messages
import play.api.mvc.Call

trait YesNoQuestionViewBehaviour[A] extends ApplicationPageBehaviour[A] {
  def pageTitle: String

  def browserTitle: String

  def guidanceParagraphs: Set[String]

  def formTarget: Call

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
      val continueButton = doc.getElementById("save-continue")
      doc.getElementsByTag("form").attr("action") shouldBe formTarget.url
    }
  }

  def yesNoQuestionWithLegend(questionLegend: => String) = {
    yesNoQuestion()

    "show the correct question text" in {
      doc.getElementById("yes-no-question-legend").text shouldBe questionLegend
    }
  }
}
