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

package iht.views.application.assets.money

import iht.controllers.application.assets.money.routes._
import iht.forms.ApplicationForms._
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.money.money_deceased_own
import play.api.i18n.Messages.Implicits._
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

class MoneyDeceasedOwnViewTest  extends ViewTestHelper with ShareableElementInputViewBehaviour[ShareableBasicEstateElement] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def form:Form[ShareableBasicEstateElement] = moneyFormOwn
  override def formToView:Form[ShareableBasicEstateElement] => Appendable = form => money_deceased_own(form, regDetails)

  override def pageTitle = messagesApi("iht.estateReport.assets.moneyOwned", deceasedName)
  override def browserTitle = messagesApi("page.iht.application.assets.money.deceased.browserTitle")
  override def questionTitle = messagesApi("iht.estateReport.assets.money.ownName.question", deceasedName)
  override def valueQuestion = messagesApi("iht.estateReport.assets.money.valueOfMoneyOwnedInOwnName")
  override def hasValueQuestionHelp = true
  override def valueQuestionHelp = ""
  override def returnLinkText = messagesApi("site.link.return.money")
  override def returnLinkUrl = MoneyOverviewController.onPageLoad().url

  "Money Deceased Own view" must {
    behave like yesNoValueViewWithErrorSummaryBox

    "show the correct guidance" in {
      messagesShouldBePresent(view, messagesApi("page.iht.application.assets.money.deceased.guidance", deceasedName))
    }
  }

}
