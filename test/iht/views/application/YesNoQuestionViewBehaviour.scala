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

import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest

trait YesNoQuestionViewBehaviour[A] extends SubmittableApplicationPageBehaviour[A] {
  /**
    * Assumes that the Call for the continue button has been set up as CommonBuilder.DefaultCall1.
    */
  def yesNoQuestion() = {
    behave like applicationPageWithErrorSummaryBox()

    "show the correct yes/no question text" in {
      doc.getElementById("yes-label").text shouldBe messagesApi("iht.yes")
      doc.getElementById("no-label").text shouldBe messagesApi("iht.no")
    }
  }

  def yesNoQuestionWithLegend(questionLegend: => String) = {
    behave like yesNoQuestion()

    "show the correct question text" in {
      doc.getElementById("yes-no-question-legend").text shouldBe questionLegend
    }
  }
}
