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

package iht.views.application.exemption.charity

import iht.forms.ApplicationForms._
import iht.models.application.exemptions.Charity
import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper._
import iht.views.application.{CancelComponent, ValueViewBehaviour}
import iht.views.html.application.exemption.charity.assets_left_to_charity_value
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class CharityValueViewTest extends ValueViewBehaviour[Charity] {

  def registrationDetails = CommonBuilder.buildRegistrationDetails1
  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)
  lazy val assetsLeftToCharityValueView: assets_left_to_charity_value = app.injector.instanceOf[assets_left_to_charity_value]

  override def guidance = noGuidance

  override def pageTitle = messagesApi("page.iht.application.exemptions.charityValue.sectionTitle")

  override def browserTitle = messagesApi("page.iht.application.exemptions.charityValue.browserTitle")

  override def formTarget = Some(CommonBuilder.DefaultCall1)

  override val cancelId: String = "cancel-button"

  override def cancelComponent = Some(
    CancelComponent(
      CommonBuilder.DefaultCall2,
      messagesApi("iht.estateReport.exemptions.charities.returnToAddACharity"),
      ExemptionsCharitiesValueID
    )
  )

  override def form: Form[Charity] = assetsLeftToCharityValueForm

  override def formToView: Form[Charity] => Appendable =
    form => assetsLeftToCharityValueView(form, registrationDetails, CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall2)

  override val value_id = "totalValue"

  "Charity Value View" must {
    behave like valueView()
  }
}
