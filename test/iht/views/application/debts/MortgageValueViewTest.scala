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
import iht.models.application.debts.Mortgage
import iht.testhelpers.TestHelper._
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.DeceasedInfoHelper
import iht.views.application.CancelComponent
import iht.views.html.application.debts.mortgage_value
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable


class MortgageValueViewTest extends DebtsElementViewBehaviour[Mortgage]{

  val ihtReference = Some("ABC1A1A1A")
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
                                    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
                                                            maritalStatus = Some(TestHelper.MaritalStatusMarried))),
                                    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))
  lazy val mortgageValueView: mortgage_value = app.injector.instanceOf[mortgage_value]

  override def form:Form[Mortgage] = mortgagesForm
  override def formToView:Form[Mortgage] => Appendable = form => mortgageValueView(form,
                              CommonBuilder.buildProperty.copy(id = Some("1"), typeOfOwnership = Some("Deceased only"),
                                address = Some(CommonBuilder.DefaultUkAddress)
                              ),
                              iht.controllers.application.debts.routes.MortgageValueController.onSubmit("1"),
                              regDetails)

  override def pageTitle = messagesApi("page.iht.application.debts.mortgageValue.title", DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails))
  override def browserTitle = messagesApi("page.iht.application.debts.mortgageValue.browserTitle")
  override def guidance = noGuidance
  override def yesNoQuestionText = messagesApi("page.iht.application.debts.mortgageValue.title", DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails))
  override def inputValueFieldLabel = messagesApi("page.iht.application.debts.mortgage.inputText.value")

  override val cancelId = "cancel-button"
  override def cancelComponent = Some(
    CancelComponent(
      routes.MortgagesOverviewController.onPageLoad,
      messagesApi("site.link.return.mortgage.overview"),
      DebtsMortgagesPropertyID + "1"
    )
  )

  override def formTarget = Some(routes.MortgageValueController.onSubmit("1"))


  "MortgageValueView" must {
    behave like debtsElement

    "show the address" in {
      val addressDiv = doc.getElementById("address")
      addressDiv.text mustBe formatAddressForDisplay(CommonBuilder.DefaultUkAddress)
    }
  }
}
