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

package iht.views.application.assets.money

import iht.controllers.application.assets.money.routes._
import iht.forms.ApplicationForms._
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.money.money_deceased_own
import play.api.i18n.Messages

class MoneyDeceasedOwnViewTest  extends ViewTestHelper with ShareableElementInputViewBehaviour {

  override def pageTitle = "iht.estateReport.assets.moneyOwned"
  override def browserTitle = "page.iht.application.assets.money.deceased.browserTitle"
  override def questionTitle = Messages("iht.estateReport.assets.money.ownName.question")
  override def valueQuestion = Messages("iht.estateReport.assets.money.valueOfMoneyOwnedInOwnName")
  override def hasValueQuestionHelp = true
  override def valueQuestionHelp = ""
  override def returnLinkText = Messages("site.link.return.money")
  override def returnLinkUrl = MoneyOverviewController.onPageLoad().url

  "Money Deceased Own view" must {
    behave like yesNoValueView

    "show the correct guidance" in {
      val f = fixture()
      messagesShouldBePresent(f.view, "page.iht.application.assets.money.deceased.guidance")
    }
  }

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val view = money_deceased_own(moneyFormOwn, CommonBuilder.buildRegistrationDetails).toString
    val doc = asDocument(view)
  }
}
