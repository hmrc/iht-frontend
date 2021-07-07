/*
 * Copyright 2021 HM Revenue & Customs
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

package iht.views.application.tnrb

import iht.forms.TnrbForms._
import iht.models.application.tnrb.WidowCheck
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.tnrb.TnrbHelper
import iht.views.application.YesNoQuestionViewBehaviour
import iht.views.html.application.tnrb.deceased_widow_check_question
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class DeceasedWidowCheckQuestionViewTest extends YesNoQuestionViewBehaviour[WidowCheck] with TnrbHelper {

  override def guidance = noGuidance
  lazy val deceasedWidowCheckQuestionView: deceased_widow_check_question = app.injector.instanceOf[deceased_widow_check_question]


  def tnrbModel = CommonBuilder.buildTnrbEligibility

  def widowCheck = CommonBuilder.buildWidowedCheck

  override def pageTitle = messagesApi(
    "iht.estateReport.tnrb.partner.married",
    CommonBuilder.buildDeceasedDetails.firstName.get + " "
      + CommonBuilder.buildDeceasedDetails.lastName.get,
    preDeceasedMaritalStatusSubLabel(widowCheck.dateOfPreDeceased),
    spouseOrCivilPartnerMessage(widowCheck.dateOfPreDeceased))

  override def browserTitle = messagesApi("iht.estateReport.tnrb.increasingIHTThreshold")

  override def formTarget = Some(iht.controllers.application.tnrb.routes.DeceasedWidowCheckQuestionController.onSubmit())

  override def form: Form[WidowCheck] = deceasedWidowCheckQuestionForm

  override def formToView: Form[WidowCheck] => Appendable = {
    def regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = Some("ABC1A1A1A"),
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(maritalStatus = Some(TestHelper.MaritalStatusMarried))),
      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

    form =>
      deceasedWidowCheckQuestionView(form, widowCheck, tnrbModel, regDetails,
        iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad(),
        messagesApi("page.iht.application.tnrb.returnToIncreasingThreshold"))
  }

  override def cancelComponent = None

  "Deceased Widow Check Question View" must {
    behave like yesNoQuestion
  }
}
