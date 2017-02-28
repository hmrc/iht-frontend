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

package iht.views.application.assets.insurancePolicy

import iht.controllers.application.assets.insurancePolicy.routes
import iht.forms.ApplicationForms._
import iht.models.application.assets.InsurancePolicy
import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_joint
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.twirl.api.HtmlFormat.Appendable

class InsurancePolicyDetailsJointViewTest extends ShareableElementInputViewBehaviour[InsurancePolicy]{

    lazy val regDetails = CommonBuilder.buildRegistrationDetails1
    lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

    override def form:Form[InsurancePolicy] = insurancePolicyJointQuestionForm
    override def formToView:Form[InsurancePolicy] => Appendable = form => insurance_policy_details_joint(form, regDetails)

    override def pageTitle = messagesApi("page.iht.application.insurance.policies.section2.title", deceasedName)
    override def browserTitle = messagesApi("page.iht.application.insurance.policies.section2.browserTitle")
    override def questionTitle = messagesApi("iht.estateReport.insurancePolicies.jointlyHeld.question", deceasedName)
    override def valueQuestion = messagesApi("iht.estateReport.assets.insurancePolicies.totalValueOfDeceasedsShare")
    override def hasValueQuestionHelp = true
    override def valueQuestionHelp = messagesApi("page.iht.application.insurance.policies.section2.guidance2")
    override def returnLinkText = messagesApi("site.link.return.insurance.policies")
    override def returnLinkUrl = routes.InsurancePolicyOverviewController.onPageLoad().url
    override def formTarget =Some(routes.InsurancePolicyDetailsJointController.onSubmit)
    override def linkHash = TestHelper.InsuranceJointlyHeldYesNoID

    "InsurancePolicyDetailsJoint view" must {
      behave like yesNoValueViewJointWithErrorSummaryBox()

      "show the correct guidance" in {
        messagesShouldBePresent(view,
          messagesApi("page.iht.application.insurance.policies.section2.guidance", deceasedName, deceasedName))
      }

      "show the value question in bold " in {
        val label = doc.getElementById("shareValue-container")
        label.getElementsByTag("span").hasClass("form-label bold")
      }
    }
}
