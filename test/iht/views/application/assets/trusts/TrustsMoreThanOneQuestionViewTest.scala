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
import iht.views.application.{CancelComponent, YesNoQuestionViewBehaviour}
import iht.views.html.application.asset.trusts.{trusts_more_than_one_question, trusts_owned_question}
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

class TrustsMoreThanOneQuestionViewTest extends YesNoQuestionViewBehaviour[HeldInTrust] {
  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidanceParagraphs = Set.empty

  override def pageTitle = Messages("iht.estateReport.assets.trusts.moreThanOne.question", deceasedName)

  override def browserTitle = Messages("page.iht.application.assets.trusts.moreThanOne.browserTitle")

  override def formTarget = Some(iht.controllers.application.assets.trusts.routes.TrustsMoreThanOneQuestionController.onSubmit())

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.assets.trusts.routes.TrustsOverviewController.onPageLoad(),
      Messages("site.link.return.trusts", deceasedName)
    )
  )

  override def form: Form[HeldInTrust] = trustsMoreThanOneQuestionForm

  override def formToView: Form[HeldInTrust] => Appendable =
    form =>
      trusts_more_than_one_question(form, registrationDetails)

  "Permanent home page Question View" must {
    behave like yesNoQuestion()
  }
}
