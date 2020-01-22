/*
 * Copyright 2020 HM Revenue & Customs
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

import iht.testhelpers.CommonBuilder
import iht.views.application.{ApplicationPageBehaviour, CancelComponent, Guidance}
import iht.views.html.application.asset.insurancePolicy.insurance_policies_overview
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig
import play.api.mvc.Call

class InsurancePoliciesOverviewViewTest extends ApplicationPageBehaviour {

  lazy val call = CommonBuilder.DefaultCall1
  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val returnUrlTextMsgKey = "page.iht.application.return.to.assetsOf"
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def pageTitle = messagesApi("iht.estateReport.assets.insurancePolicies")

  override def browserTitle = messagesApi("iht.estateReport.assets.insurancePolicies")

  override def view:String = insurance_policies_overview(regDetails, Nil, Some(call), returnUrlTextMsgKey).toString()

  override def guidance: Guidance = guidance(Set(
    messagesApi("page.iht.application.assets.insurance.policies.overview.guidance1", deceasedName),
    messagesApi("page.iht.application.assets.insurance.policies.overview.guidance.bullet1"),
    messagesApi("page.iht.application.assets.insurance.policies.overview.guidance.bullet2"),
    messagesApi("page.iht.application.assets.insurance.policies.overview.guidance.bullet3"),
    messagesApi("page.iht.application.assets.insurance.policies.overview.guidance.bullet4", deceasedName)
  ))

  override def formTarget: Option[Call] = None

  override def cancelComponent: Option[CancelComponent] = None

  "InsurancePoliciesOverviewView" must {
    behave like applicationPage
  }
}
