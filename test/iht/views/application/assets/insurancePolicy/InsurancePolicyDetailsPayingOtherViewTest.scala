/*
 * Copyright 2019 HM Revenue & Customs
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

package iht.views.application.assets.insurancePolicy

import iht.controllers.application.assets.insurancePolicy.routes
import iht.forms.ApplicationForms._
import iht.models.application.assets.InsurancePolicy
import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper
import iht.views.application.{CancelComponent, YesNoQuestionViewBehaviour}
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_paying_other
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.twirl.api.HtmlFormat.Appendable

class InsurancePolicyDetailsPayingOtherViewTest extends YesNoQuestionViewBehaviour[InsurancePolicy] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def guidance = guidance(Set(messagesApi("iht.estateReport.insurancePolicies.premiumsNotPayingOut.question.hintText1", deceasedName),
    messagesApi("iht.estateReport.insurancePolicies.premiumsNotPayingOut.question.hintText2", deceasedName)))

  override def pageTitle = messagesApi("iht.estateReport.insurancePolicies.premiumsNotPayingOut.question", deceasedName)

  override def browserTitle = messagesApi("page.iht.application.insurance.policies.section3.browserTitle")

  override def formTarget = Some(routes.InsurancePolicyDetailsPayingOtherController.onSubmit())

  override def form: Form[InsurancePolicy] = insurancePolicyJointQuestionForm

  override def formToView: Form[InsurancePolicy] => Appendable =
  form => insurance_policy_details_paying_other(form, regDetails)

  override def cancelComponent = Some(CancelComponent(routes.InsurancePolicyOverviewController.onPageLoad(),
    messagesApi("site.link.return.insurance.policies"),
    TestHelper.InsurancePaidForSomeoneElseYesNoID
  ))

  "InsurancePolicyDetailsPayingOtherViewTest" must {
  behave like yesNoQuestionWithLegend(messagesApi("iht.estateReport.insurancePolicies.premiumsNotPayingOut.question", deceasedName))
}
}
