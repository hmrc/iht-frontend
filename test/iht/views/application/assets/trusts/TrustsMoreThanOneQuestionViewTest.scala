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

package iht.views.application.assets.trusts

import iht.forms.ApplicationForms._
import iht.models.application.assets.HeldInTrust
import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper._
import iht.views.application.{CancelComponent, YesNoQuestionViewBehaviour}
import iht.views.html.application.asset.trusts.trusts_more_than_one_question
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class TrustsMoreThanOneQuestionViewTest extends YesNoQuestionViewBehaviour[HeldInTrust] {
  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidance = noGuidance

  override def pageTitle = messagesApi("iht.estateReport.assets.trusts.moreThanOne.question", deceasedName)

  override def browserTitle = messagesApi("page.iht.application.assets.trusts.moreThanOne.browserTitle")

  override def formTarget = Some(iht.controllers.application.assets.trusts.routes.TrustsMoreThanOneQuestionController.onSubmit())

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.assets.trusts.routes.TrustsOverviewController.onPageLoad(),
      messagesApi("site.link.return.trusts", deceasedName),
      AssetsTrustsMultipleID
    )
  )

  override def form: Form[HeldInTrust] = trustsMoreThanOneQuestionForm
  lazy val trustsMoreThanOneQuestionView: trusts_more_than_one_question = app.injector.instanceOf[trusts_more_than_one_question]

  override def formToView: Form[HeldInTrust] => Appendable =
    form =>
      trustsMoreThanOneQuestionView(form, registrationDetails)

  "Permanent home page Question View" must {
    behave like yesNoQuestion()
  }
}
