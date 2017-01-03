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

import iht.controllers.application.assets.routes._
import iht.forms.ApplicationForms._
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.other

import play.api.i18n.Messages

class OtherViewTest extends ViewTestHelper with ShareableElementInputViewBehaviour {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def pageTitle = "iht.estateReport.assets.other.title"
  override def browserTitle = "page.iht.application.assets.other.browserTitle"
  override def questionTitle = Messages("page.iht.application.assets.other.isOwned")
  override def valueQuestion = Messages("page.iht.application.assets.other.inputLabel1")
  override def hasValueQuestionHelp = false
  override def valueQuestionHelp = ""
  override def returnLinkText = Messages("page.iht.application.return.to.assetsOf", deceasedName)
  override def returnLinkUrl = AssetsOverviewController.onPageLoad().url

  "Money Owed view" must {
    behave like yesNoValueView

    "show the correct guidance" in {
      val f = fixture()
      messagesShouldBePresent(f.view,
        "page.iht.application.assets.other.description.p1",
        "page.iht.application.assets.other.description.p2")
    }
  }

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val view = other(moneyOwedForm, regDetails).toString
    val doc = asDocument(view)
  }
}
