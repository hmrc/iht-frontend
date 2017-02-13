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
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.money.money_jointly_owned
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class MoneyJointlyOwnedViewTest extends ViewTestHelper with ShareableElementInputViewBehaviour {
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def pageTitle = Messages("iht.estateReport.assets.money.jointlyOwned")
  override def browserTitle = Messages("page.iht.application.assets.money.jointly.owned.browserTitle")
  override def questionTitle = Messages("page.iht.application.assets.money.jointly.owned.question", deceasedName)
  override def valueQuestion = Messages("page.iht.application.assets.money.jointly.owned.input.value.label")
  override def hasValueQuestionHelp = false
  override def valueQuestionHelp = ""
  override def returnLinkText = Messages("site.link.return.money")
  override def returnLinkUrl = MoneyOverviewController.onPageLoad().url

  "Money Jointly Owned view" must {
    behave like yesNoValueViewJoint

    "show the correct guidance" in {
      val f = fixture()
      messagesShouldBePresent(f.view,
        Messages("page.iht.application.assets.money.jointly.owned.guidance.p1", deceasedName),
        Messages("page.iht.application.assets.money.jointly.owned.guidance.p2", deceasedName),
        Messages("page.iht.application.assets.money.jointly.owned.guidance.p3", deceasedName, deceasedName))
    }
  }

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val view = money_jointly_owned(moneyJointlyOwnedForm, regDetails).toString
    val doc = asDocument(view)
  }
}
