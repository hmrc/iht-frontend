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

package iht.views.application.assets.household

import iht.controllers.application.assets.household.routes
import iht.controllers.application.assets.household.routes._
import iht.forms.ApplicationForms._
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.testhelpers.CommonBuilder
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.household.household_jointly_owned
import play.api.i18n.Messages.Implicits._
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable
import iht.testhelpers.TestHelper._

class HouseholdJointlyOwnedViewTest extends ShareableElementInputViewBehaviour[ShareableBasicEstateElement] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def form:Form[ShareableBasicEstateElement] = householdJointlyOwnedForm
  override def formToView:Form[ShareableBasicEstateElement] => Appendable = form => household_jointly_owned(form, regDetails)

  override def pageTitle = messagesApi("iht.estateReport.assets.householdAndPersonalItemsJointlyOwned.title", deceasedName)
  override def browserTitle = messagesApi("page.iht.application.assets.household.joint.browserTitle")
  override def questionTitle = messagesApi("iht.estateReport.assets.household.joint.question", deceasedName)
  override def valueQuestion = messagesApi("iht.estateReport.assets.household.valueOfJointlyOwned", deceasedName)
  override def hasValueQuestionHelp = true
  override def valueQuestionHelp = messagesApi("iht.estateReport.assets.getProfessionalValuation")
  override def returnLinkText = messagesApi("site.link.return.household")
  override def returnLinkUrl = HouseholdOverviewController.onPageLoad().url
  override def linkHash = AssetsHouseholdSharedID
  override def formTarget =Some(routes.HouseholdJointlyOwnedController.onSubmit)

  "Household Jointly Owned view" must {
    behave like yesNoValueViewJointWithErrorSummaryBox
  }

}
