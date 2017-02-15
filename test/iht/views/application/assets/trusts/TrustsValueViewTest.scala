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

package iht.views.application.assets.trusts

import iht.forms.ApplicationForms._
import iht.models.application.assets.HeldInTrust
import iht.testhelpers.CommonBuilder
import iht.views.application.{CancelComponent, ValueViewBehaviour}
import iht.views.html.application.asset.trusts.trusts_value
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

class TrustsValueViewTest extends ValueViewBehaviour[HeldInTrust] {
  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidanceParagraphs = Set.empty

  override def pageTitle = Messages("iht.estateReport.assets.heldInTrust.valueOfTrust", deceasedName)

  override def browserTitle = Messages("page.iht.application.assets.trusts.value.browserTitle")

  override def formTarget = Some(iht.controllers.application.assets.trusts.routes.TrustsValueController.onSubmit())

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.assets.trusts.routes.TrustsOverviewController.onPageLoad(),
      Messages("site.link.return.trusts", deceasedName)
    )
  )

  override def form: Form[HeldInTrust] = trustsValueForm

  override def formToView: Form[HeldInTrust] => Appendable =
    form =>
      trusts_value(form, registrationDetails)

  "Permanent home page Question View" must {
    behave like valueView()
  }
}
