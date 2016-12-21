/*
 * Copyright 2016 HM Revenue & Customs
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

package iht.views.application.assets.household

import iht.controllers.application.assets.household.routes._
import iht.forms.ApplicationForms._
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.household.household_jointly_owned
import play.api.i18n.Messages

class HouseholdJointlyOwnedViewTest extends ViewTestHelper with ShareableElementInputViewBehaviour {

  override def pageTitle = "iht.estateReport.assets.householdAndPersonalItemsJointlyOwned.title"
  override def browserTitle = "page.iht.application.assets.household.joint.browserTitle"
  override def questionTitle = Messages("iht.estateReport.assets.household.joint.question")
  override def valueQuestion = Messages("iht.estateReport.assets.household.valueOfJointlyOwned")
  override def hasValueQuestionHelp = false
  override def valueQuestionHelp = ""
  override def returnLinkText = Messages("site.link.return.household")
  override def returnLinkUrl = HouseholdOverviewController.onPageLoad().url

  "Household Jointly Owned view" must {
    behave like yesNoValueViewJoint
  }

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val view = household_jointly_owned(householdJointlyOwnedForm, CommonBuilder.buildRegistrationDetails).toString
    val doc = asDocument(view)
  }
}
