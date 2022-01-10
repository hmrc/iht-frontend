/*
 * Copyright 2022 HM Revenue & Customs
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

import iht.views.application.{SubmittableApplicationPageBehaviour, CancelComponent, ApplicationPageBehaviour}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.twirl.api.HtmlFormat.Appendable
import iht.controllers.application.debts.routes

trait DebtsElementViewBehaviour[A] extends SubmittableApplicationPageBehaviour[A] {

  def yesNoQuestionText: String
  def inputValueFieldLabel: String
  def inputValueFieldHintText: String = "default hint"
  override def linkHash: String = ""

  override def cancelComponent = Some(
    CancelComponent(
      routes.DebtsOverviewController.onPageLoad,
      messagesApi("site.link.return.debts"),
      linkHash
    )
  )

  override def view: String = formToView(form).toString
  def form:Form[A]
  def formToView:Form[A] => Appendable

  def debtsElement() = {

    behave like applicationPageWithErrorSummaryBox()

    "show the correct yes/no question text" in {
      messagesShouldBePresent(view, yesNoQuestionText)
    }

    "show the correct input field value label" in {
      messagesShouldBePresent(view, inputValueFieldLabel)
    }

    "show the correct input field value hint text (if there is any)" in {
      if(inputValueFieldHintText == "default hint") {
        assertNotContainsText(asDocument(view), inputValueFieldHintText)
      } else {
        messagesShouldBePresent(view, inputValueFieldHintText)
      }
    }

  }
}
