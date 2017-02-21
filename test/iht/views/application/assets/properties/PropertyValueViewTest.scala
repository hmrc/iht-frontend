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

package iht.views.application.assets.properties

import iht.forms.ApplicationForms._
import iht.models.application.assets.Property
import iht.testhelpers.CommonBuilder
import iht.views.application.{CancelComponent, ValueViewBehaviour}
import iht.views.html.application.asset.properties.property_value
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

class PropertyValueViewTest extends ValueViewBehaviour[Property] {
  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidance = guidance(
    Set(
      Messages("page.iht.application.property.value.question.hint1", deceasedName),
      Messages("page.iht.application.property.value.question.hint2", deceasedName)
    )
  )

  override def pageTitle = Messages("iht.estateReport.assets.properties.value.question", deceasedName)

  override def browserTitle = Messages("page.iht.application.property.value.browserTitle")

  override def formTarget = Some(CommonBuilder.DefaultCall1)

  override val cancelId: String = "cancel-button"

  override def cancelComponent = Some(
    CancelComponent(
      CommonBuilder.DefaultCall2,
      Messages("iht.estateReport.assets.properties.returnToAddAProperty")
    )
  )

  override def form: Form[Property] = propertyValueForm

  override def formToView: Form[Property] => Appendable =
    form =>
      property_value(form, CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall2, deceasedName)

  "Property value page" must {
    behave like valueView()
  }
}
