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
import iht.views.application.{ApplicationPageBehaviour, CancelComponent}
import iht.views.html._
import iht.views.html.application.asset.properties.property_type
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable
import play.api.i18n.Messages.Implicits._

class PropertyTypeViewTest extends ApplicationPageBehaviour[Property] {
  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  def name(deceasedName: => String) = ihtHelpers.name(deceasedName)

  override def guidance = guidance(
    Set(
      messagesApi("page.iht.application.assets.property.type.label1", name(deceasedName)),
      messagesApi("page.iht.application.assets.property.type.label2", name(deceasedName))
    )
  )

  override def pageTitle = messagesApi("iht.estateReport.assets.properties.whatKind.question")

  override def browserTitle = messagesApi("page.iht.application.assets.property.type.browserTitle")

  override def formTarget = Some(CommonBuilder.DefaultCall1)

  override def cancelComponent = Some(
    CancelComponent(
      CommonBuilder.DefaultCall2,
      messagesApi("iht.estateReport.assets.properties.returnToAddAProperty")
    )
  )

  override val cancelId: String = "cancel-button"

  override def form: Form[Property] = propertyTypeForm

  override def formToView: Form[Property] => Appendable =
    form =>
      property_type(form, Some(CommonBuilder.DefaultCall2), CommonBuilder.DefaultCall1, deceasedName)

  "Property Type View" must {
    behave like applicationPageWithErrorSummaryBox()
  }

  "deceased's home radio button" must {
    behave like radioButton(
      testTitle = "deceased's home",
      titleId = "propertyType-deceased's_home-label",
      titleExpectedValue = "page.iht.application.assets.propertyType.deceasedHome.label"
    )
  }

  "other residential building radio button" must {
    behave like radioButton(
      testTitle = "other residential building",
      titleId = "propertyType-other_residential_building-label",
      titleExpectedValue = "page.iht.application.assets.propertyType.otherResidential.label"
    )
  }

  "land, non residential or business building radio button" must {
    behave like radioButton(
      testTitle = "land, non residential or business building",
      titleId = "propertyType-land,_non-residential_or_business_building-label",
      titleExpectedValue = "page.iht.application.assets.propertyType.nonResidential.label"
    )
  }
}
