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

import iht.models.application.assets.InsurancePolicy
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.html.application.asset.insurancePolicy.insurance_policies_overview
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class InsurancePoliciesOverviewViewTest extends ViewTestHelper {

  lazy val assetsOverviewPageUrl = iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad()
  lazy val returnUrlTextMsgKey = "page.iht.application.return.to.assetsOf"
  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  def insurancePoliciesOverviewView(insurancePolicy:InsurancePolicy) = {
    implicit val request = createFakeRequest()

    val view = insurance_policies_overview(regDetails, Nil, Some(assetsOverviewPageUrl), returnUrlTextMsgKey).toString()
    asDocument(view)
  }

  "InsurancePoliciesOverview view" must {

    "have correct title and browser title " in {
      val view = insurancePoliciesOverviewView(CommonBuilder.buildInsurancePolicy).toString

      titleShouldBeCorrect(view, Messages("iht.estateReport.assets.insurancePolicies"))
      browserTitleShouldBeCorrect(view, Messages("iht.estateReport.assets.insurancePolicies"))
    }

    "have correct guidance paragraphs" in {
      val view = insurancePoliciesOverviewView(CommonBuilder.buildInsurancePolicy).toString
      messagesShouldBePresent(view, Messages("page.iht.application.assets.insurance.policies.overview.guidance1",
        deceasedName))
    }

    "have correct return link with text" in {
      val view = insurancePoliciesOverviewView(CommonBuilder.buildInsurancePolicy)

      val returnLink = view.getElementById("return-button")
      returnLink.attr("href") shouldBe assetsOverviewPageUrl.url
      returnLink.text() shouldBe Messages(returnUrlTextMsgKey, deceasedName)

    }
  }

}
