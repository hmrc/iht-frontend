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
import iht.views.html.application.asset.properties.property_address
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable
import play.api.i18n.Messages.Implicits._

class PropertyAddressViewTest extends ApplicationPageBehaviour[Property] {
  override def pageTitle = messagesApi("iht.estateReport.assets.property.whatIsAddress.question")

  override def browserTitle = messagesApi("page.iht.application.assets.property.address.browserTitle")

  override def guidance = noGuidance

  override def form: Form[Property] = propertyAddressForm

  override def formToView: Form[Property] => Appendable = form =>
    property_address(form, CommonBuilder.DefaultCall2, CommonBuilder.DefaultCall1)

  override def formTarget = Some(CommonBuilder.DefaultCall1)

  override def cancelComponent = Some(
    CancelComponent(
      CommonBuilder.DefaultCall2,
      messagesApi("iht.estateReport.assets.properties.returnToAddAProperty")
    )
  )

  override val cancelId: String = "cancel-button"

  "Property Address View in UK Mode" must {

    behave like applicationPageWithErrorSummaryBox()

    behave like addressPageUK()
  }
}
