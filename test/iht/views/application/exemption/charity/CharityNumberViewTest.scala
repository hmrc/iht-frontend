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
import iht.views.html.application.exemption.charity.charity_number
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class CharityNumberViewTest extends ValueViewBehaviour[Charity] {

  def registrationDetails = CommonBuilder.buildRegistrationDetails1
  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)
  lazy val charityNumberView: charity_number = app.injector.instanceOf[charity_number]

  override def guidance = noGuidance

  override def pageTitle = messagesApi("iht.estateReport.exemptions.charities.charityNo.question")

  override def browserTitle = messagesApi("page.iht.application.exemptions.charityNumber.browserTitle")

  override def formTarget = Some(CommonBuilder.DefaultCall1)

  override val cancelId: String = "cancel-button"

  override def cancelComponent = Some(
    CancelComponent(
      CommonBuilder.DefaultCall2,
      messagesApi("iht.estateReport.exemptions.charities.returnToAddACharity"),
      ExemptionsCharitiesNumberID
    )
  )

  override def form: Form[Charity] = charityNumberForm

  override def formToView: Form[Charity] => Appendable =
    form => charityNumberView(form, registrationDetails, CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall2)

  override val value_id = "charityNumber"

  "Charity Number View" must {
    behave like valueView()

    behave like link("charity-register",
      charityLink,
      (messagesApi("page.iht.application.exemptions.charityNumber.linkText")))
    
  }
}
