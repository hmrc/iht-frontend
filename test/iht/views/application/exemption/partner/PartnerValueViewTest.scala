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

import iht.forms.ApplicationForms._
import iht.models.application.exemptions.PartnerExemption
import iht.testhelpers.CommonBuilder
import iht.views.application.{CancelComponent, ValueViewBehaviour}
import iht.views.html.application.exemption.partner.partner_value
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.twirl.api.HtmlFormat.Appendable

class PartnerValueViewTest extends ValueViewBehaviour[PartnerExemption] {

  def registrationDetails = CommonBuilder.buildRegistrationDetails1
  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidance = noGuidance

  override def pageTitle = messagesApi("page.iht.application.exemptions.partner.value.sectionTitle")

  override def browserTitle = messagesApi("page.iht.application.exemptions.partner.value.browserTitle")

  override def formTarget = Some(CommonBuilder.DefaultCall1)

  override val cancelId: String = "cancel-button"

  override def cancelComponent = Some(
    CancelComponent(
      CommonBuilder.DefaultCall2,
      messagesApi("iht.estateReport.exemptions.partner.returnToAssetsLeftToPartner")
    )
  )

  override def form: Form[PartnerExemption] = partnerValueForm

  override def formToView: Form[PartnerExemption] => Appendable =
    form => partner_value(form, registrationDetails)

  override val value_id = "totalValue"

  "Qualifying Body Value View" must {
    behave like valueView()
  }
}
