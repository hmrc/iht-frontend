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
import iht.testhelpers.{TestHelper, CommonBuilder}
import iht.utils.tnrb.TnrbHelper
import iht.views.application.YesNoQuestionViewBehaviour
import play.api.i18n.Messages
import iht.views.html.application.tnrb.gifts_made_before_death
import play.api.mvc.Call

class GiftsMadeBeforeDeathViewTest extends YesNoQuestionViewBehaviour {

  val ihtReference = Some("ABC1A1A1A")
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
                                                      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
                                                                  maritalStatus = Some(TestHelper.MaritalStatusMarried))),
                                                      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))
  val tnrbModel = CommonBuilder.buildTnrbEligibility
  val widowCheck = CommonBuilder.buildWidowedCheck

  override def pageTitle = Messages("iht.estateReport.tnrb.giftsMadeBeforeDeath.question",
                                    TnrbHelper.spouseOrCivilPartnerLabel(
                                      tnrbModel, widowCheck,
                                      Messages("page.iht.application.tnrbEligibilty.partner.additional.label.the")))

  override def browserTitle = Messages("page.iht.application.tnrb.giftsMadeBeforeDeath.browserTitle")
  override def guidanceParagraphs = Set(Messages("page.iht.application.tnrb.giftsMadeBeforeDeath.question.hint1",
                                                TnrbHelper.spouseOrCivilPartnerName(tnrbModel,
                                                   Messages("page.iht.application.tnrb.spouseOrCivilPartner.hint"))),
                                        Messages("page.iht.application.tnrb.giftsMadeBeforeDeath.question.hint2"))
  override def yesNoQuestionText = Messages("iht.estateReport.tnrb.giftsMadeBeforeDeath.question",
                                        TnrbHelper.spouseOrCivilPartnerLabel(tnrbModel, widowCheck,
                                        Messages("page.iht.application.tnrbEligibilty.partner.additional.label.the")))

  override def returnLinkId = "cancel-button"
  override def returnLinkText = Messages("page.iht.application.tnrb.returnToIncreasingThreshold")
  override def returnLinkTargetUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val view = gifts_made_before_death(giftMadeBeforeDeathForm, tnrbModel, widowCheck).toString
    val doc = asDocument(view)
  }

  "GiftsMadeBeforeDeathView " must {
    behave like yesNoQuestion
  }

}
