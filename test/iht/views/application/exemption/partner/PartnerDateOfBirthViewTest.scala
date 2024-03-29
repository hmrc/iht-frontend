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
import iht.views.application.{CancelComponent, ValueViewBehaviour}
import iht.views.html.application.exemption.partner.partner_date_of_birth
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class PartnerDateOfBirthViewTest extends ValueViewBehaviour[PartnerExemption] {

  def registrationDetails = CommonBuilder.buildRegistrationDetails1
  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)
  lazy val partnerDateOfBirthView: partner_date_of_birth = app.injector.instanceOf[partner_date_of_birth]

  override def guidance = noGuidance

  override def pageTitle = messagesApi("page.iht.application.exemptions.partner.dateOfBirth.question.title")

  override def browserTitle = messagesApi("page.iht.application.exemptions.partner.dateOfBirth.browserTitle")

  override def formTarget = Some(iht.controllers.application.exemptions.partner.routes.PartnerDateOfBirthController.onSubmit)

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.exemptions.partner.routes.PartnerOverviewController.onPageLoad,
      messagesApi("iht.estateReport.exemptions.partner.returnToAssetsLeftToSpouse"),
      ExemptionsPartnerDobID
    )
  )

  override def form: Form[PartnerExemption] = spouseDateOfBirthForm

  override def formToView: Form[PartnerExemption] => Appendable =
    form => partnerDateOfBirthView(form, registrationDetails)

  override val value_id = "dateOfBirth-container"

  "Partner date of birth View" must {
    behave like valueView()
  }
}
