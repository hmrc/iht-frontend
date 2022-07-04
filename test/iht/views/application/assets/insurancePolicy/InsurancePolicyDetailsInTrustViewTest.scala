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

package iht.views.application.assets.insurancePolicy

import iht.controllers.application.assets.insurancePolicy.routes
import iht.forms.ApplicationForms._
import iht.models.application.assets.InsurancePolicy
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.views.application.{CancelComponent, YesNoQuestionViewBehaviour}
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_in_trust
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class InsurancePolicyDetailsInTrustViewTest extends YesNoQuestionViewBehaviour[InsurancePolicy] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)
  lazy val insurancePolicyDetailsInTrustView: insurance_policy_details_in_trust = app.injector.instanceOf[insurance_policy_details_in_trust]

  override def pageTitle = messagesApi("page.iht.application.insurance.policies.section4.title", deceasedName)

  override def browserTitle = messagesApi("page.iht.application.insurance.policies.section4.browserTitle")

  override def guidance = noGuidance

  override def formTarget = Some(routes.InsurancePolicyDetailsInTrustController.onSubmit)

  override def form: Form[InsurancePolicy] = insurancePolicyInTrustForm

  override def formToView: Form[InsurancePolicy] => Appendable =
    form => insurancePolicyDetailsInTrustView(form, regDetails)

  override def cancelComponent = Some(CancelComponent(routes.InsurancePolicyOverviewController.onPageLoad,
    messagesApi("site.link.return.insurance.policies"),
    TestHelper.InsurancePlacedInTrustYesNoID
  ))

  "InsurancePolicyDetailsInTrustViewTest" must {
    behave like yesNoQuestion
  }
}
