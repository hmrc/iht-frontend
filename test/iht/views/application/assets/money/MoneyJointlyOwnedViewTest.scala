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

package iht.views.application.assets.money

import iht.controllers.application.assets.money.routes
import iht.controllers.application.assets.money.routes._
import iht.forms.ApplicationForms._
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.CommonHelper
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.money.money_jointly_owned
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class MoneyJointlyOwnedViewTest extends ShareableElementInputViewBehaviour[ShareableBasicEstateElement] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)
  lazy val moneyJointlyOwnedView: money_jointly_owned = app.injector.instanceOf[money_jointly_owned]

  override def form:Form[ShareableBasicEstateElement] = moneyJointlyOwnedForm
  override def formToView:Form[ShareableBasicEstateElement] => Appendable = form => moneyJointlyOwnedView(form, regDetails)

  override def pageTitle = messagesApi("iht.estateReport.assets.money.jointlyOwned")
  override def browserTitle = messagesApi("page.iht.application.assets.money.jointly.owned.browserTitle")
  override def questionTitle = messagesApi("page.iht.application.assets.money.jointly.owned.question", deceasedName)
  override def valueQuestion = messagesApi("page.iht.application.assets.money.jointly.owned.input.value.label", deceasedName)
  override def hasValueQuestionHelp = false
  override def valueQuestionHelp = ""
  override def returnLinkText = messagesApi("site.link.return.money")
  override def returnLinkUrl = CommonHelper.addFragmentIdentifierToUrl(MoneyOverviewController.onPageLoad.url, TestHelper.AssetsMoneySharedID)
  override def formTarget =Some(routes.MoneyJointlyOwnedController.onSubmit)

  "Money Jointly Owned view" must {
    behave like yesNoValueViewJointWithErrorSummaryBox

    "show the correct guidance" in {
      messagesShouldBePresent(view,
        messagesApi("page.iht.application.assets.money.jointly.owned.guidance.p1", deceasedName),
        messagesApi("page.iht.application.assets.money.jointly.owned.guidance.p2", deceasedName),
        messagesApi("page.iht.application.assets.money.jointly.owned.guidance.p3", deceasedName, deceasedName))
    }
  }

}
