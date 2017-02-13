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
import iht.models.application.tnrb.WidowCheck
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.CommonHelper
import iht.utils.tnrb.TnrbHelper
import iht.views.application.YesNoQuestionViewBehaviour
import play.api.i18n.Messages
import iht.views.html.application.tnrb.deceased_widow_check_question

class DeceasedWidowCheckQuestionViewTest extends YesNoQuestionViewBehaviour[WidowCheck] {

  val ihtReference = Some("ABC1A1A1A")
  val deceasedDetails = CommonBuilder.buildDeceasedDetails
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
    deceasedDetails = Some(deceasedDetails.copy(maritalStatus = Some(TestHelper.MaritalStatusMarried))),
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  val tnrbModel = CommonBuilder.buildTnrbEligibility
  val widowCheckModel = CommonBuilder.buildWidowedCheck

  override def pageTitle = Messages("iht.estateReport.tnrb.partner.married",
                             TnrbHelper.preDeceasedMaritalStatusSubLabel(widowCheckModel.dateOfPreDeceased),
                             TnrbHelper.spouseOrCivilPartnerMessage(widowCheckModel.dateOfPreDeceased))

  override def browserTitle = Messages("iht.estateReport.tnrb.increasingIHTThreshold")
  override def guidanceParagraphs = Set()
  override def yesNoQuestionText = Messages("iht.estateReport.tnrb.partner.married")

  override def returnLinkId = "cancel-button"
  override def returnLinkText = Messages("page.iht.application.tnrb.returnToIncreasingThreshold")
  override def returnLinkTargetUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()

  override def fixture() = new {
    implicit val request = createFakeRequest()

    val view = deceased_widow_check_question(deceasedWidowCheckQuestionForm,
                                              widowCheckModel, tnrbModel, regDetails,
                                              returnLinkTargetUrl, returnLinkText).toString
    val doc = asDocument(view)
  }

  "DeceasedWidowCheckQuestionView " must {
    behave like yesNoQuestion
  }

}
