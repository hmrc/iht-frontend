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
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_deceased_own
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class InsurancePolicyDetailsDeceasedOwnViewTest extends ViewTestHelper with ShareableElementInputViewBehaviour{

    lazy val regDetails = CommonBuilder.buildRegistrationDetails1
    lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

    override def pageTitle = Messages("iht.estateReport.assets.insurancePolicies.payingOutToDeceased", deceasedName)
    override def browserTitle = Messages("page.iht.application.insurance.policies.section1.browserTitle")
    override def questionTitle = Messages(Messages("iht.estateReport.insurancePolicies.ownName.question", deceasedName))
    override def valueQuestion = Messages("iht.estateReport.assets.insurancePolicies.totalValueOwnedAndPayingOut")
    override def hasValueQuestionHelp = false
    override def valueQuestionHelp = ""
    override def returnLinkText = Messages("site.link.return.insurance.policies")
    override def returnLinkUrl = routes.InsurancePolicyOverviewController.onPageLoad().url

    "InsurancePolicyDetailsDeceasedOwn view" must {
      behave like yesNoValueView

      "show the correct guidance" in {
        val f = fixture()
        messagesShouldBePresent(f.view,
          Messages("page.iht.application.insurance.policies.section1.guidance", deceasedName, deceasedName))
      }

      "show the value question in bold " in {
        val f = fixture()

        val label = f.doc.getElementById("value-container")
        label.getElementsByTag("span").hasClass("form-label bold")
      }
    }

    override def fixture() = new {
      implicit val request = createFakeRequest()
      val view = insurance_policy_details_deceased_own(insurancePolicyForm, regDetails).toString
      val doc = asDocument(view)
    }

}
