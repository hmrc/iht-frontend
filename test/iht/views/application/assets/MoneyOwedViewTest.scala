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

package iht.views.application.assets

import iht.controllers.application.assets.routes
import iht.controllers.application.assets.routes._
import iht.forms.ApplicationForms._
import iht.models.application.basicElements.BasicEstateElement
import iht.testhelpers.CommonBuilder
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.money_owed
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

class MoneyOwedViewTest extends ShareableElementInputViewBehaviour[BasicEstateElement] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def form:Form[BasicEstateElement] = moneyOwedForm
  override def formToView:Form[BasicEstateElement] => Appendable = form => money_owed(form, regDetails)

  override def pageTitle = Messages("iht.estateReport.assets.moneyOwed", deceasedName)
  override def browserTitle = Messages("iht.estateReport.assets.moneyOwed", Messages("iht.the.deceased"))
  override def questionTitle = Messages("page.iht.application.assets.moneyOwed.isOwned", deceasedName)
  override def valueQuestion = Messages("page.iht.application.assets.moneyOwed.inputLabel1")
  override def hasValueQuestionHelp = false
  override def valueQuestionHelp = ""
  override def returnLinkText = Messages("page.iht.application.return.to.assetsOf", deceasedName)
  override def returnLinkUrl = AssetsOverviewController.onPageLoad().url
  override def formTarget =Some(routes.MoneyOwedController.onSubmit)

  "Money Owed view" must {
    behave like yesNoValueView

    "show the correct guidance" in {
      messagesShouldBePresent(view,
        Messages("page.iht.application.assets.moneyOwed.description.p1", deceasedName),
        Messages("page.iht.application.assets.moneyOwed.description.p2", deceasedName),
        Messages("page.iht.application.assets.moneyOwed.description.p3", deceasedName))
    }
  }

}
