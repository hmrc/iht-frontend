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

package iht.views.application.assets.pensions

import iht.forms.ApplicationForms.pensionsValueForm
import iht.models.application.assets.PrivatePension
import iht.testhelpers.CommonBuilder
import iht.views.application.{CancelComponent, ValueViewBehaviour}
import iht.views.html.application.asset.pensions.pensions_value
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable
import iht.controllers.application.assets.pensions.routes

class PensionsValueViewTest extends ValueViewBehaviour[PrivatePension] {

  def registrationDetails = CommonBuilder.buildRegistrationDetails1
  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidanceParagraphs = Set(Messages("page.iht.application.assets.pensions.hint"))

  override def pageTitle = Messages("iht.estateReport.assets.pensions.valueOfRemainingPaymentsBeingPaid")

  override def browserTitle = Messages("page.iht.application.assets.pensions.value.browserTitle")

  override def formTarget = Some(routes.PensionsValueController.onSubmit)

  override def cancelComponent = Some(CancelComponent(routes.PensionsOverviewController.onPageLoad(),
                                        Messages("iht.estateReport.assets.pensions.returnToPrivatePensions")
                                      )
  )

  override def form: Form[PrivatePension] = pensionsValueForm

  override def formToView: Form[PrivatePension] => Appendable =
    form => pensions_value(form, registrationDetails)

  "Pensions Value View" must {
    behave like valueView()
  }
}
