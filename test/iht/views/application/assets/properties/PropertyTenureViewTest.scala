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

package iht.views.application.assets.properties

import iht.forms.ApplicationForms._
import iht.models.application.assets.Property
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.views.application.{CancelComponent, SubmittableApplicationPageBehaviour}
import iht.views.html.application.asset.properties.property_tenure
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class PropertyTenureViewTest extends SubmittableApplicationPageBehaviour[Property] {
  override def guidance = guidance(
    Set(
      messagesApi("iht.estateReport.assets.property.youCan"),
      messagesApi("iht.estateReport.assets.property.findOutFromLandRegistry"),
      messagesApi("page.iht.application.assets.property.tenure.guidance1b")
    )
  )
  lazy val propertyTenureView: property_tenure = app.injector.instanceOf[property_tenure]

  override def pageTitle = messagesApi("iht.estateReport.assets.properties.freeholdOrLeasehold")

  override def browserTitle = messagesApi("page.iht.application.assets.property.tenure.browserTitle")

  override def formTarget = Some(CommonBuilder.DefaultCall1)

  override def cancelComponent = Some(
    CancelComponent(
      CommonBuilder.DefaultCall2,
      messagesApi("iht.estateReport.assets.properties.returnToAddAProperty"),
      TestHelper.AssetsPropertiesTenureID
    )
  )

  override val cancelId: String = "cancel-button"

  override def form: Form[Property] = propertyTenureForm

  val deceasedName = "John"

  override def formToView: Form[Property] => Appendable =
    form =>
      propertyTenureView(form, CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall2, deceasedName)

  "Property Tenure View" must {
    behave like applicationPageWithErrorSummaryBox()
  }

  behave like radioButton(
    testTitle = "freehold tenure",
    titleId = "tenure-freehold-main",
    titleExpectedValue = "page.iht.application.assets.tenure.freehold.label",
    hintId = "tenure-freehold-hint",
    hintExpectedValue = "page.iht.application.assets.tenure.freehold.hint",
    hintExpectedValueParam = Some(deceasedName)
  )

  behave like radioButton(
    testTitle = "leasehold tenure",
    titleId = "tenure-leasehold-main",
    titleExpectedValue = "page.iht.application.assets.tenure.leasehold.label",
    hintId = "tenure-leasehold-hint",
    hintExpectedValue = "page.iht.application.assets.tenure.leasehold.hint",
    hintExpectedValueParam = Some(deceasedName)
  )

  behave like link("land-registry-link", appConfig.linkLandRegistry,
    messagesApi("iht.estateReport.assets.property.findOutFromLandRegistry"))
}
