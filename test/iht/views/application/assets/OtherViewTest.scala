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

package iht.views.application.assets

import iht.controllers.application.assets.routes
import iht.controllers.application.assets.routes._
import iht.forms.ApplicationForms._
import iht.models.application.basicElements.BasicEstateElement
import iht.testhelpers.CommonBuilder
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.other
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable


class OtherViewTest extends ShareableElementInputViewBehaviour[BasicEstateElement] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)
  lazy val otherView: other = app.injector.instanceOf[other]

  override def form:Form[BasicEstateElement] = otherForm
  override def formToView:Form[BasicEstateElement] => Appendable = form => otherView(form, regDetails)

  override def pageTitle = messagesApi("iht.estateReport.assets.other.title")
  override def browserTitle = messagesApi("page.iht.application.assets.other.browserTitle")
  override def questionTitle = messagesApi("page.iht.application.assets.other.isOwned", deceasedName)
  override def valueQuestion = messagesApi("page.iht.application.assets.other.inputLabel1", deceasedName)
  override def hasValueQuestionHelp = false
  override def valueQuestionHelp = ""
  override def returnLinkText = messagesApi("page.iht.application.return.to.assetsOf", deceasedName)
  override def returnLinkUrl = AssetsOverviewController.onPageLoad.url
  override def formTarget =Some(routes.OtherController.onSubmit)
  override def linkHash = appConfig.AppSectionOtherID

  "Money Owed view" must {
    behave like yesNoValueView

    "show the correct guidance" in {
      messagesShouldBePresent(view,
        messagesApi("page.iht.application.assets.other.description.p1"),
        messagesApi("page.iht.application.assets.other.description.p2", deceasedName))
    }
  }

}
