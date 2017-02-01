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

package iht.views.registration

import play.api.i18n.Messages

trait YesNoQuestionViewBehaviour[A] extends RegistrationPageBehaviour[A] {

  def pageTitle: String

  def browserTitle: String

  def guidanceParagraphs: Set[String]

  def yesNoQuestion() = {
    registrationPageWithErrorSummaryBox()

    "show the correct guidance paragraphs" in {
      val f = fixture()
      for (paragraph <- guidanceParagraphs) messagesShouldBePresent(f.view, paragraph)
    }

    "show the correct yes/no question text" in {
      val f = fixture()
      f.doc.getElementById("yes-label").text shouldBe Messages("iht.yes")
      f.doc.getElementById("no-label").text shouldBe Messages("iht.no")
    }

    "show the Continue button" in {
      val f = fixture()
      val continueButton = f.doc.getElementById("continue-button")
      continueButton.`val` shouldBe Messages("iht.continue")
    }
  }
}
