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
import iht.testhelpers.{CommonBuilder, TestHelper}
import play.api.i18n.Messages
import iht.views.html.application.debts.any_other_debts

/**
  * Created by vineet on 15/11/16.
  */
class AnyOtherDebtsViewTest extends DebtsElementViewBehaviour{

  val ihtReference = Some("ABC1A1A1A")
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
                                                      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
                                                                 maritalStatus = Some(TestHelper.MaritalStatusMarried))),
                                                      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  override def pageTitle = Messages("iht.estateReport.debts.other.title")
  override def browserTitle = Messages("page.iht.application.debts.other.browserTitle")
  override def guidanceParagraphs = Set(Messages("page.iht.application.debts.other.description.p1"),
                                        Messages("page.iht.application.debts.other.description.p2"),
                                        Messages("page.iht.application.debts.other.description.p3"))
  override def yesNoQuestionText = Messages("page.iht.application.debts.other.isOwned")
  override def inputValueFieldLabel = Messages("page.iht.application.debts.other.inputLabel1")

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val view = any_other_debts(anyOtherDebtsForm, regDetails).toString
    val doc = asDocument(view)
  }

  "AnyOtherDebtsView" must {
    behave like debtsElement
  }
}
