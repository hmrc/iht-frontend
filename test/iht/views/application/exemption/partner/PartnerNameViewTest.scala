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

package iht.views.application.exemption.partner

import iht.models.application.exemptions.PartnerExemption
import iht.testhelpers.CommonBuilder
import iht.views.application.{CancelComponent, ValueViewBehaviour}
import iht.views.html.application.exemption.partner.partner_name
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.twirl.api.HtmlFormat.Appendable
import iht.forms.ApplicationForms._
import iht.testhelpers.TestHelper._

class PartnerNameViewTest extends ValueViewBehaviour[PartnerExemption] {

  def registrationDetails = CommonBuilder.buildRegistrationDetails1
  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidance = noGuidance

  override def pageTitle = messagesApi("page.iht.application.exemptions.partner.name.title")

  override def browserTitle = messagesApi("page.iht.application.exemptions.partner.name.browserTitle")

  override def formTarget = Some(iht.controllers.application.exemptions.partner.routes.ExemptionPartnerNameController.onSubmit())

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.exemptions.partner.routes.PartnerOverviewController.onPageLoad(),
      messagesApi("iht.estateReport.exemptions.partner.returnToAssetsLeftToSpouse"),
      ExemptionsPartnerNameID
    )
  )

  override def form: Form[PartnerExemption] = partnerExemptionNameForm

  override def formToView: Form[PartnerExemption] => Appendable =
    form => partner_name(form, registrationDetails)

  override val value_id = "firstName"

  "Partner Name View" must {
    behave like valueView()

    "have an input field with an ID of lastName" in {
      Option(doc.getElementById("lastName")).isDefined shouldBe true
    }
  }
}
