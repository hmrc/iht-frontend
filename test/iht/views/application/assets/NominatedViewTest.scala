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
import iht.models.application.basicElements.BasicEstateElement
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.nominated
import play.api.i18n.Messages.Implicits._
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable
import iht.constants.Constants._

class NominatedViewTest extends ViewTestHelper with ShareableElementInputViewBehaviour[BasicEstateElement] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def form:Form[BasicEstateElement] = nominatedForm
  override def formToView:Form[BasicEstateElement] => Appendable = form => nominated(form, regDetails)

  override def pageTitle = messagesApi("iht.estateReport.assets.nominated")
  override def browserTitle = messagesApi("page.iht.application.assets.nominated.browserTitle")
  override def questionTitle = messagesApi("page.iht.application.assets.nominated.question", deceasedName)
  override def valueQuestion = messagesApi("page.iht.application.assets.nominated.inputLabel1")
  override def hasValueQuestionHelp = false
  override def valueQuestionHelp = ""
  override def returnLinkText = messagesApi("page.iht.application.return.to.assetsOf", deceasedName)
  override def returnLinkUrl = AssetsOverviewController.onPageLoad().url
  override def linkHash = AppSectionNominatedID

  "Nominated assets view" must {
    behave like yesNoValueView

    "show the correct guidance" in {
      messagesShouldBePresent(view,
        messagesApi("page.iht.application.assets.nominated.description.p1", deceasedName),
        messagesApi("page.iht.application.assets.nominated.description.p2"),
        messagesApi("page.iht.application.assets.nominated.description.p3"),
        messagesApi("page.iht.application.assets.nominated.description.p4"))
    }
  }

}
