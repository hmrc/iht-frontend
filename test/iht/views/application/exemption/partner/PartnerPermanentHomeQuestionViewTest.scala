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

package iht.views.application.exemption.partner

import iht.forms.ApplicationForms._
import iht.models.application.exemptions.PartnerExemption
import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper._
import iht.views.application.{CancelComponent, YesNoQuestionViewBehaviour}
import iht.views.html.application.exemption.partner.partner_permanent_home_question

class PartnerPermanentHomeQuestionViewTest extends YesNoQuestionViewBehaviour[PartnerExemption] {
  val regDetails = CommonBuilder.buildRegistrationDetails1

  val deceasedName = regDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def form = partnerPermanentHomeQuestionForm
  lazy val partnerPermanentHomeQuestionView: partner_permanent_home_question = app.injector.instanceOf[partner_permanent_home_question]

  override def formToView = form => partnerPermanentHomeQuestionView(form, regDetails, CommonBuilder.DefaultString, CommonBuilder.DefaultCall1)

  override def pageTitle = messagesApi("iht.estateReport.exemptions.partner.homeInUK.question")

  override def browserTitle = messagesApi("page.iht.application.exemptions.partnerPermanentHome.browserTitle")

  override def guidance = noGuidance

  override def formTarget = Some(iht.controllers.application.exemptions.partner.routes.PartnerPermanentHomeQuestionController.onSubmit)

  override val cancelId: String = "cancel-button"

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.exemptions.partner.routes.PartnerOverviewController.onPageLoad,
      CommonBuilder.DefaultString,
      ExemptionsPartnerHomeID
    )
  )

  "AssetsLeftToPartnerQuestionView" must {
    behave like yesNoQuestion()
  }
}
