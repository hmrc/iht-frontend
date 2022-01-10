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
import iht.views.html.application.debts.owed_from_trust
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable



class OwedFromTrustViewTest extends DebtsElementViewBehaviour[BasicEstateElementLiabilities]{

  val ihtReference = Some("ABC1A1A1A")
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
                                                      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
                                                                maritalStatus = Some(TestHelper.MaritalStatusMarried))),
                                                      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))
  lazy val owedFromTrustView: owed_from_trust = app.injector.instanceOf[owed_from_trust]

  override def form:Form[BasicEstateElementLiabilities] = debtsTrustForm
  override def formToView:Form[BasicEstateElementLiabilities] => Appendable = form => owedFromTrustView(form, regDetails)

  override def pageTitle = messagesApi("iht.estateReport.debts.debtsTrust.title")
  override def browserTitle = messagesApi("page.iht.application.debts.debtsTrust.browserTitle")

  override def guidance  = guidance(
    Set(messagesApi("page.iht.application.debts.debtsTrust.description.p1",
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)))
  )

  override def yesNoQuestionText = messagesApi("page.iht.application.debts.debtsTrust.isOwned",
                                                     DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails))
  override def inputValueFieldLabel = messagesApi("iht.estateReport.debts.debtsTrust.value")
  override def linkHash = TestHelper.DebtsOwedFromTrustID

  override def formTarget = Some(routes.DebtsOwedFromATrustController.onSubmit)

  "OwedFromTrustView" must {
    behave like debtsElement
  }
}
