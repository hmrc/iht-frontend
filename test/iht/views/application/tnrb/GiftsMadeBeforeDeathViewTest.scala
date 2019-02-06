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

package iht.views.application.tnrb

import iht.forms.TnrbForms._
import play.api.i18n.Messages.Implicits._
import iht.models.application.tnrb.TnrbEligibiltyModel
import iht.testhelpers.CommonBuilder
import iht.utils.tnrb.TnrbHelper
import iht.views.application.YesNoQuestionViewBehaviour
import iht.views.html.application.tnrb.gifts_made_before_death
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

class GiftsMadeBeforeDeathViewTest extends YesNoQuestionViewBehaviour[TnrbEligibiltyModel] {

  def tnrbModel = CommonBuilder.buildTnrbEligibility

  def widowCheck = CommonBuilder.buildWidowedCheck

  val deceasedDetailsName = CommonBuilder.buildDeceasedDetails.name

  override def pageTitle = messagesApi("iht.estateReport.tnrb.giftsMadeBeforeDeath.question",
    TnrbHelper.spouseOrCivilPartnerLabelGenitive(
      tnrbModel, widowCheck,
      messagesApi("page.iht.application.tnrbEligibilty.partner.additional.label.the")))

  override def browserTitle = messagesApi("page.iht.application.tnrb.giftsMadeBeforeDeath.browserTitle")

  override def guidance = guidance(
    Set(messagesApi("page.iht.application.tnrb.giftsMadeBeforeDeath.question.hint1",
      TnrbHelper.spouseOrCivilPartnerName(tnrbModel,
        messagesApi("page.iht.application.tnrb.spouseOrCivilPartner.hint"))),
      messagesApi("page.iht.application.tnrb.giftsMadeBeforeDeath.question.hint2"))
  )

  override def formTarget = Some(iht.controllers.application.tnrb.routes.GiftsMadeBeforeDeathController.onSubmit())

  override def form: Form[TnrbEligibiltyModel] = giftMadeBeforeDeathForm

  override def formToView: Form[TnrbEligibiltyModel] => Appendable =
    form =>
      gifts_made_before_death(form, tnrbModel, widowCheck, CommonBuilder.DefaultCall2, CommonBuilder.buildRegistrationDetails)

  override def cancelComponent = None

  "Gifts With Reservation Of Benefit page Question View" must {
    behave like yesNoQuestion
  }
}
