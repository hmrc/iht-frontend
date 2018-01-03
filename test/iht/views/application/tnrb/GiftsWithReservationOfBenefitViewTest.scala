/*
 * Copyright 2018 HM Revenue & Customs
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
import iht.models.application.tnrb.TnrbEligibiltyModel
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.tnrb.TnrbHelper
import iht.views.application.YesNoQuestionViewBehaviour
import play.api.i18n.Messages.Implicits._
import iht.views.html.application.tnrb.{gifts_with_reservation_of_benefit, jointly_owned_assets}
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class GiftsWithReservationOfBenefitViewTest extends YesNoQuestionViewBehaviour[TnrbEligibiltyModel] {

  def tnrbModel = CommonBuilder.buildTnrbEligibility

  def widowCheck = CommonBuilder.buildWidowedCheck

  val deceasedDetailsName = CommonBuilder.buildDeceasedDetails.name

  override def pageTitle = messagesApi("iht.estateReport.tnrb.giftsWithReservationOfBenefit.question", deceasedDetailsName)

  override def browserTitle = messagesApi("page.iht.application.tnrb.giftsWithReservationOfBenefit.browserTitle")
  
  override def guidance = guidance(
    Set(messagesApi("page.iht.application.tnrb.giftsWithReservationOfBenefit.question.hint",
      TnrbHelper.spouseOrCivilPartnerName(tnrbModel,
        messagesApi("iht.estateReport.tnrb.thSouseAndCivilPartner")), deceasedDetailsName,
      TnrbHelper.spouseOrCivilPartnerName(tnrbModel,
        messagesApi("iht.estateReport.tnrb.thSouseAndCivilPartner"))))
  )

  override def formTarget = Some(iht.controllers.application.tnrb.routes.GiftsWithReservationOfBenefitController.onSubmit())

  override def form: Form[TnrbEligibiltyModel] = partnerGiftWithResToOtherForm

  override def formToView: Form[TnrbEligibiltyModel] => Appendable =
    form =>
      gifts_with_reservation_of_benefit(form, tnrbModel, deceasedDetailsName, CommonBuilder.DefaultCall2)

  override def cancelComponent = None

  "Gifts With Reservation Of Benefit page Question View" must {
    behave like yesNoQuestion
  }
}
