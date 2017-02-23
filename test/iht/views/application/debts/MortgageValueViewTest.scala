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

package iht.views.application.debts

import iht.forms.ApplicationForms._
import iht.models.application.debts.BasicEstateElementLiabilities
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.CommonHelper
import play.api.i18n.Messages.Implicits._
import iht.views.html.application.debts.mortgage_value
import iht.views.html.application.debts.{funeral_expenses, mortgage_value}
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable
import iht.constants.Constants._

/**
  * Created by vineet on 15/11/16.
  */
class MortgageValueViewTest extends DebtsElementViewBehaviour[BasicEstateElementLiabilities]{

  val ihtReference = Some("ABC1A1A1A")
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
                                    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
                                                            maritalStatus = Some(TestHelper.MaritalStatusMarried))),
                                    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  override def form:Form[BasicEstateElementLiabilities] = funeralExpensesForm
  override def formToView:Form[BasicEstateElementLiabilities] => Appendable = form => mortgage_value(mortgagesForm,
                              CommonBuilder.buildProperty.copy(id = Some("1"), typeOfOwnership = Some("Deceased only")),
                              iht.controllers.application.debts.routes.MortgageValueController.onSubmit("1"),
                              regDetails)

  override def pageTitle = messagesApi("page.iht.application.debts.mortgageValue.title", CommonHelper.getDeceasedNameOrDefaultString(regDetails))
  override def browserTitle = messagesApi("page.iht.application.debts.mortgageValue.browserTitle")
  override def guidanceParagraphs = Set()
  override def yesNoQuestionText = messagesApi("page.iht.application.debts.mortgageValue.title", CommonHelper.getDeceasedNameOrDefaultString(regDetails))
  override def inputValueFieldLabel = messagesApi("page.iht.application.debts.mortgage.inputText.value")
  override def returnLinkId = "cancel-button"
  override def returnLinkText = messagesApi("site.link.return.mortgage.overview")
  override def returnLinkTargetUrl = iht.controllers.application.debts.routes.MortgagesOverviewController.onPageLoad()


  "MortgageValueView" must {
    behave like debtsElement
  }
}
