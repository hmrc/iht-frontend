/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.constants.IhtProperties
import iht.forms.ApplicationForms._
import iht.models.application.assets.Property
import iht.testhelpers.CommonBuilder
import iht.views.application.{SubmittableApplicationPageBehaviour, CancelComponent}
import iht.views.html.application.asset.properties.{property_ownership, property_tenure}
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable
import play.api.i18n.Messages.Implicits._
import iht.testhelpers.TestHelper

class PropertyOwnershipViewTest extends SubmittableApplicationPageBehaviour[Property] {
  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidance = guidance(Set(
    messagesApi("iht.estateReport.assets.property.youCan"),
    messagesApi("iht.estateReport.assets.property.findOutFromLandRegistry"),
    messagesApi("page.iht.application.assets.property.ownership.guidance1b")
  ))

  override def pageTitle = messagesApi("iht.estateReport.assets.howOwnedByDeceased", deceasedName)

  override def browserTitle = messagesApi("page.iht.application.assets.property.ownership.browserTitle")

  override def formTarget = Some(CommonBuilder.DefaultCall1)

  override def cancelComponent = Some(
    CancelComponent(
      CommonBuilder.DefaultCall2,
      messagesApi("iht.estateReport.assets.properties.returnToAddAProperty"),
      TestHelper.AssetsPropertiesPropertyOwnershipID
    )
  )

  override val cancelId: String = "cancel-button"

  override def form: Form[Property] = typeOfOwnershipForm

  override def formToView: Form[Property] => Appendable =
    form =>
      property_ownership(form, CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall2, deceasedName)

  "Property Ownership View" must {
    behave like applicationPageWithErrorSummaryBox()
  }

  behave like radioButton(
    testTitle = "only by the deceased",
    titleId = "typeOfOwnership-deceased_only-main",
    titleExpectedValue = "page.iht.application.assets.typeOfOwnership.deceasedOnly.label",
    hintId = "typeOfOwnership-deceased_only-hint",
    hintExpectedValue = "page.iht.application.assets.typeOfOwnership.deceasedOnly.hint",
    hintExpectedValueParam = Some(deceasedName),
    titleExpectedValueParam = Some(deceasedName)
  )

  behave like radioButton(
    testTitle = "joint tenants",
    titleId = "typeOfOwnership-joint-main",
    titleExpectedValue = "page.iht.application.assets.typeOfOwnership.joint.label",
    hintId = "typeOfOwnership-joint-hint",
    hintExpectedValue = "page.iht.application.assets.typeOfOwnership.joint.hint",
    hintExpectedValueParam = Some(deceasedName)
  )

  behave like radioButton(
    testTitle = "tenants in common",
    titleId = "typeOfOwnership-in_common-main",
    titleExpectedValue = "page.iht.application.assets.typeOfOwnership.inCommon.label",
    hintId = "typeOfOwnership-in_common-hint",
    hintExpectedValue = "page.iht.application.assets.typeOfOwnership.inCommon.hint",
    hintExpectedValueParam = Some(deceasedName)
  )


  behave like link("land-registry-link", IhtProperties.linkLandRegistry,
    messagesApi("iht.estateReport.assets.property.findOutFromLandRegistry"))
}
