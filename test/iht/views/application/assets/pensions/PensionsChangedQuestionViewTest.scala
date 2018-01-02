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

package iht.views.application.assets.pensions

import iht.forms.ApplicationForms.pensionsChangedQuestionForm
import iht.models.application.assets.PrivatePension
import iht.models.application.tnrb.TnrbEligibiltyModel
import iht.testhelpers.CommonBuilder
import iht.views.application.YesNoQuestionViewBehaviour
import iht.views.html.application.asset.pensions.pensions_changed_question
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable
import iht.controllers.application.assets.pensions.routes
import iht.views.application.CancelComponent
import play.api.i18n.Messages.Implicits._
import iht.testhelpers.TestHelper._

class PensionsChangedQuestionViewTest extends YesNoQuestionViewBehaviour[PrivatePension] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def guidance = noGuidance

  override def pageTitle = messagesApi("page.iht.application.assets.pensions.changed.title", deceasedName)

  override def browserTitle = messagesApi("page.iht.application.assets.pensions.changed.browserTitle")

  override def formTarget = Some(routes.PensionsChangedQuestionController.onSubmit())

  override def form: Form[PrivatePension] = pensionsChangedQuestionForm

  override def formToView: Form[PrivatePension] => Appendable =
    form => pensions_changed_question(form, regDetails)

  override def cancelComponent = Some(CancelComponent(routes.PensionsOverviewController.onPageLoad(),
    messagesApi("iht.estateReport.assets.pensions.returnToPrivatePensions"),
    AssetsPensionChangesID
  ))

  "Pensions Changed Question View" must {
    behave like yesNoQuestion
  }
}
