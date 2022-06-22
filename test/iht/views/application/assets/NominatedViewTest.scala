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
import iht.forms.ApplicationForms._
import iht.models.application.basicElements.BasicEstateElement
import iht.testhelpers.CommonBuilder
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.nominated
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class NominatedViewTest extends ShareableElementInputViewBehaviour[BasicEstateElement] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)
  lazy val nominatedView: nominated = app.injector.instanceOf[nominated]

  override def form:Form[BasicEstateElement] = nominatedForm
  override def formToView:Form[BasicEstateElement] => Appendable = form => nominatedView(form, regDetails)

  override def pageTitle = messagesApi("iht.estateReport.assets.nominated")
  override def browserTitle = messagesApi("page.iht.application.assets.nominated.browserTitle")
  override def questionTitle = messagesApi("page.iht.application.assets.nominated.question", deceasedName)
  override def valueQuestion = messagesApi("page.iht.application.assets.nominated.inputLabel1", deceasedName)
  override def hasValueQuestionHelp = false
  override def valueQuestionHelp = ""
  override def returnLinkText = messagesApi("page.iht.application.return.to.assetsOf", deceasedName)
  override def returnLinkUrl = iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad.url
  override def formTarget =Some(routes.NominatedController.onSubmit)
  override def linkHash = appConfig.AppSectionNominatedID

  "Nominated assets view" must {
    behave like yesNoValueViewWithErrorSummaryBox

    "show the correct guidance" in {
      messagesShouldBePresent(view,
        messagesApi("page.iht.application.assets.nominated.description.p1", deceasedName),
        messagesApi("page.iht.application.assets.nominated.description.p2"),
        messagesApi("page.iht.application.assets.nominated.description.p3"),
        messagesApi("page.iht.application.assets.nominated.description.p4"))
    }
  }

}
