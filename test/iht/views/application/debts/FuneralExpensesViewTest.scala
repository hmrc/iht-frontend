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

import iht.controllers.application.debts.routes
import iht.forms.ApplicationForms._
import iht.models.application.debts.BasicEstateElementLiabilities
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.DeceasedInfoHelper
import iht.views.html.application.debts.funeral_expenses
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable



class FuneralExpensesViewTest extends DebtsElementViewBehaviour[BasicEstateElementLiabilities]{

  val ihtReference = Some("ABC1A1A1A")
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
                                                  deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
                                                             maritalStatus = Some(TestHelper.MaritalStatusMarried))),
                                                  deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  override def form:Form[BasicEstateElementLiabilities] = funeralExpensesForm
  lazy val funeralExpensesView: funeral_expenses = app.injector.instanceOf[funeral_expenses]

  override def formToView:Form[BasicEstateElementLiabilities] => Appendable = form => funeralExpensesView(form, regDetails)

  override def pageTitle = messagesApi("iht.estateReport.debts.funeralExpenses.title")
  override def browserTitle = messagesApi("iht.estateReport.debts.funeralExpenses.title")

  override def yesNoQuestionText = messagesApi("page.iht.application.debts.funeralExpenses.isOwned")
  override def inputValueFieldLabel = messagesApi("iht.estateReport.debts.valueOfFuneralCosts")
  override def linkHash = TestHelper.DebtsFuneralExpensesID

  override def guidance  = guidance(
    Set(messagesApi("page.iht.application.debts.funeralExpenses.description.p1"),
      messagesApi("page.iht.application.debts.funeralExpenses.description.p2",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)))
  )

  override def formTarget = Some(routes.FuneralExpensesController.onSubmit)

  "FuneralExpensesView" must {
    behave like debtsElement
  }
}
