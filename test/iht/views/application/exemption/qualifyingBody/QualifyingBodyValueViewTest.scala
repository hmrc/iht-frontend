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

package iht.views.application.exemption.qualifyingBody

import iht.forms.ApplicationForms._
import iht.models.application.exemptions.QualifyingBody
import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper._
import iht.views.application.{CancelComponent, ValueViewBehaviour}
import iht.views.html.application.exemption.qualifyingBody.qualifying_body_value
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class QualifyingBodyValueViewTest extends ValueViewBehaviour[QualifyingBody] {

  def registrationDetails = CommonBuilder.buildRegistrationDetails1
  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidance = noGuidance

  override def pageTitle = messagesApi("page.iht.application.exemptions.qualifyingBody.value.sectionTitle")

  override def browserTitle = messagesApi("page.iht.application.exemptions.qualifyingBody.value.browserTitle")

  override def formTarget = Some(CommonBuilder.DefaultCall1)

  override val cancelId: String = "cancel-button"

  override def cancelComponent = Some(
    CancelComponent(
      CommonBuilder.DefaultCall2,
      messagesApi("iht.estateReport.exemptions.qualifyingBodies.returnToAssetsLeftToQualifyingBody"),
      ExemptionsOtherValueID
    )
  )

  override def form: Form[QualifyingBody] = qualifyingBodyValueForm
  lazy val qualifyingBodyValueView: qualifying_body_value = app.injector.instanceOf[qualifying_body_value]

  override def formToView: Form[QualifyingBody] => Appendable =
    form => qualifyingBodyValueView(form, registrationDetails, CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall2)

  override val value_id = "totalValue"

  "Qualifying Body Value View" must {
    behave like valueView()
  }
}
