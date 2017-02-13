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

package iht.views.application.tnrb

import iht.forms.TnrbForms._
import iht.models.application.tnrb.TnrbEligibiltyModel
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.tnrb.TnrbHelper
import iht.views.application.YesNoQuestionViewBehaviour
import iht.views.html.application.tnrb.permanent_home
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

class PermanentHomeViewTest extends YesNoQuestionViewBehaviour[TnrbEligibiltyModel] {

  override def guidanceParagraphs = Set.empty

  def tnrbModel = CommonBuilder.buildTnrbEligibility

  def widowCheck = CommonBuilder.buildWidowedCheck

  override def pageTitle = Messages("iht.estateReport.tnrb.permanentHome.question",
    TnrbHelper.spouseOrCivilPartnerLabel(tnrbModel, widowCheck,
      Messages("page.iht.application.tnrbEligibilty.partner.additional.label.the")))

  override def browserTitle = Messages("page.iht.application.tnrb.permanentHome.browerTitle")

  override def formTarget = iht.controllers.application.tnrb.routes.PermanentHomeController.onSubmit()

  override def form: Form[TnrbEligibiltyModel] = partnerLivingInUkForm

  override def formToView: Form[TnrbEligibiltyModel] => Appendable =
    form =>
      permanent_home(form, tnrbModel, widowCheck)

  "Permanent home page Question View" must {
    behave like yesNoQuestion
  }
}


//  val ihtReference = Some("ABC1A1A1A")
//  val deceasedDetails = CommonBuilder.buildDeceasedDetails
//  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
//                        deceasedDetails = Some(deceasedDetails.copy(maritalStatus = Some(TestHelper.MaritalStatusMarried))),
//                        deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))
//
//  val tnrbModel = CommonBuilder.buildTnrbEligibility
//  val widowCheck = CommonBuilder.buildWidowedCheck
//
//  override def pageTitle = Messages("iht.estateReport.tnrb.permanentHome.question",
//                                 TnrbHelper.spouseOrCivilPartnerLabel(tnrbModel, widowCheck,
//                                      Messages("page.iht.application.tnrbEligibilty.partner.additional.label.the")))
//  override def browserTitle = Messages("page.iht.application.tnrb.permanentHome.browerTitle")
//  override def guidanceParagraphs = Set()
//  override def yesNoQuestionText = Messages("iht.estateReport.tnrb.permanentHome.question",
//                                         TnrbHelper.spouseOrCivilPartnerLabel(tnrbModel, widowCheck,
//                                         Messages("page.iht.application.tnrbEligibilty.partner.additional.label.the")))
//  override def returnLinkId = "cancel-button"
//  override def returnLinkText = Messages("page.iht.application.tnrb.returnToIncreasingThreshold")
//  override def returnLinkTargetUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()
//
//  override def fixture() = new {
//    implicit val request = createFakeRequest()
//    val view = permanent_home(partnerLivingInUkForm, tnrbModel, widowCheck).toString
//    val doc = asDocument(view)
//  }
//
//  "PermanentHomeView" must {
//    behave like yesNoQuestion
//  }
//
//}
