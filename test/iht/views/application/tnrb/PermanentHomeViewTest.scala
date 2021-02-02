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
import iht.models.application.tnrb.TnrbEligibiltyModel
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.tnrb.TnrbHelper
import iht.views.application.YesNoQuestionViewBehaviour
import iht.views.html.application.tnrb.permanent_home
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.config.AppConfig
import play.twirl.api.HtmlFormat.Appendable

class PermanentHomeViewTest extends YesNoQuestionViewBehaviour[TnrbEligibiltyModel] with TnrbHelper {

  override def guidance = noGuidance

  def tnrbModel = CommonBuilder.buildTnrbEligibility

  def widowCheck = CommonBuilder.buildWidowedCheck

  override def pageTitle = messagesApi("iht.estateReport.tnrb.permanentHome.question",
    spouseOrCivilPartnerLabelGenitive(tnrbModel, widowCheck,
      messagesApi("page.iht.application.tnrbEligibilty.partner.additional.label.the")))

  override def browserTitle = messagesApi("iht.registration.deceased.locationOfPermanentHome")

  override def formTarget = Some(iht.controllers.application.tnrb.routes.PermanentHomeController.onSubmit())

  override def form: Form[TnrbEligibiltyModel] = partnerLivingInUkForm

  override def formToView: Form[TnrbEligibiltyModel] => Appendable =
    form =>
      permanent_home(form, tnrbModel, widowCheck, CommonBuilder.DefaultCall2, CommonBuilder.buildRegistrationDetails3)

  override def cancelComponent = None

  "Permanent home page Question View" must {
    behave like yesNoQuestion
  }
}
