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

package iht.views.application.assets.pensions

import iht.controllers.application.assets.pensions.routes
import iht.forms.ApplicationForms.pensionsChangedQuestionForm
import iht.models.application.assets.PrivatePension
import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper._
import iht.views.application.{CancelComponent, YesNoQuestionViewBehaviour}
import iht.views.html.application.asset.pensions.pensions_changed_question
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class PensionsChangedQuestionViewTest extends YesNoQuestionViewBehaviour[PrivatePension] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)
  lazy val pensionsChangedQuestionView: pensions_changed_question = app.injector.instanceOf[pensions_changed_question]

  override def guidance = noGuidance

  override def pageTitle = messagesApi("page.iht.application.assets.pensions.changed.title", deceasedName)

  override def browserTitle = messagesApi("page.iht.application.assets.pensions.changed.browserTitle")

  override def formTarget = Some(routes.PensionsChangedQuestionController.onSubmit())

  override def form: Form[PrivatePension] = pensionsChangedQuestionForm

  override def formToView: Form[PrivatePension] => Appendable =
    form => pensionsChangedQuestionView(form, regDetails)

  override def cancelComponent = Some(CancelComponent(routes.PensionsOverviewController.onPageLoad(),
    messagesApi("iht.estateReport.assets.pensions.returnToPrivatePensions"),
    AssetsPensionChangesID
  ))

  "Pensions Changed Question View" must {
    behave like yesNoQuestion
  }
}
