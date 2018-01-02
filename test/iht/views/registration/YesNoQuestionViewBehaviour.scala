/*
 * Copyright 2018 HM Revenue & Customs
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

package iht.views.registration

import iht.testhelpers.CommonBuilder
import play.api.i18n.Messages

trait YesNoQuestionViewBehaviour[A] extends RegistrationPageBehaviour[A] {

  def pageTitle: String

  def browserTitle: String

  def guidanceParagraphs: Set[String]

  /**
    * Assumes that the Call for the continue button has been set up as CommonBuilder.DefaultCall1.
    */
  def yesNoQuestion() = {
    registrationPageWithErrorSummaryBox()

    "show the correct guidance paragraphs" in {
      for (paragraph <- guidanceParagraphs) messagesShouldBePresent(view, paragraph)
    }

    "show the correct yes/no question text" in {
      doc.getElementById("yes-label").text shouldBe messagesApi("iht.yes")
      doc.getElementById("no-label").text shouldBe messagesApi("iht.no")
    }

    "show the Continue button with the correct target" in {
      val continueButton = doc.getElementById("continue-button")
      continueButton.`val` shouldBe messagesApi("iht.continue")
      doc.getElementsByTag("form").attr("action") shouldBe CommonBuilder.DefaultCall1.url
    }
  }

  def yesNoQuestionWithLegend(questionLegend: => String) = {
    yesNoQuestion()

    "show the correct question text" in {
      doc.getElementById("yes-no-question-legend").text shouldBe questionLegend
    }
  }

  /**
    * Assumes that the Call for the continue button has been set up as CommonBuilder.DefaultCall1.
    * Assumes that the Call for the cancel link has been set up as CommonBuilder.DefaultCall2.
    */
  def yesNoQuestionWithCancelLink() = {
    yesNoQuestion()

    "show the Cancel link with the correct target" in {
      val continueButton = doc.getElementById("continue-button")
      continueButton.`val` shouldBe messagesApi("iht.continue")
      doc.getElementsByTag("form").attr("action") shouldBe CommonBuilder.DefaultCall1.url

      val cancelLink = doc.getElementById("cancel-button")
      cancelLink.attr("href") shouldBe CommonBuilder.DefaultCall2.url
      cancelLink.text() shouldBe messagesApi("site.link.cancel")
    }
  }
}
