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

package iht.views.application.exemption.partner

import iht.models.application.exemptions.PartnerExemption
import iht.testhelpers.CommonBuilder
import iht.views.application.{CancelComponent, ValueViewBehaviour}
import iht.views.html.application.exemption.partner.{partner_name, partner_nino}
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig
import play.twirl.api.HtmlFormat.Appendable
import iht.forms.ApplicationForms._
import iht.testhelpers.TestHelper._

class PartnerNinoViewTest extends ValueViewBehaviour[PartnerExemption] {

  def registrationDetails = CommonBuilder.buildRegistrationDetails1
  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidance = noGuidance

  override def pageTitle = messagesApi("page.iht.application.exemptions.partner.nino.sectionTitle")

  override def browserTitle = messagesApi("page.iht.application.exemptions.partner.nino.browserTitle")

  override def formTarget = Some(iht.controllers.application.exemptions.partner.routes.PartnerNinoController.onSubmit())

  override val cancelId: String = "cancel-button"

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.exemptions.partner.routes.PartnerOverviewController.onPageLoad(),
      messagesApi("iht.estateReport.exemptions.partner.returnToAssetsLeftToSpouse"),
      ExemptionsPartnerNinoID
    )
  )

  override def form: Form[PartnerExemption] = partnerNinoForm

  override def formToView: Form[PartnerExemption] => Appendable =
    form => partner_nino(form, registrationDetails)

  override val value_id = "nino"

  "Partner Nino View" must {
    behave like valueView()
  }
}
