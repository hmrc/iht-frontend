/*
 * Copyright 2019 HM Revenue & Customs
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
import iht.utils.DeceasedInfoHelper
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig
import iht.views.html.application.debts.any_other_debts
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable
import iht.constants.Constants._

import iht.controllers.application.debts.routes



class AnyOtherDebtsViewTest extends DebtsElementViewBehaviour[BasicEstateElementLiabilities]{

  val ihtReference = Some("ABC1A1A1A")
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
                                                      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
                                                                 maritalStatus = Some(TestHelper.MaritalStatusMarried))),
                                                      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  override def form:Form[BasicEstateElementLiabilities] = anyOtherDebtsForm
  override def formToView:Form[BasicEstateElementLiabilities] => Appendable = form => any_other_debts(form, regDetails)

  override def pageTitle = messagesApi("iht.estateReport.debts.other.title")
  override def browserTitle = messagesApi("page.iht.application.debts.other.browserTitle")

  override def guidance  = guidance(
    Set(messagesApi("page.iht.application.debts.other.description.p1"),
      messagesApi("page.iht.application.debts.other.description.p2",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)),
      messagesApi("page.iht.application.debts.other.description.p3"))
  )

  override def yesNoQuestionText = messagesApi("page.iht.application.debts.other.isOwned")
  override def inputValueFieldLabel = messagesApi("page.iht.application.debts.other.inputLabel1")
  override def linkHash = TestHelper.DebtsOtherID

  override def formTarget = Some(routes.AnyOtherDebtsController.onSubmit)

 "AnyOtherDebtsView" must {
    behave like debtsElement
  }
}
