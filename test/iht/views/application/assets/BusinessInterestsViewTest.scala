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
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.business_interests
import play.api.i18n.Messages.Implicits._

class BusinessInterestsViewTest  extends ShareableElementInputViewBehaviour {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def pageTitle = messagesApi("iht.estateReport.assets.businessInterests.title")
  override def browserTitle = messagesApi("page.iht.application.assets.businessInterest.browserTitle")
  override def questionTitle = messagesApi("page.iht.application.assets.businessInterest.isOwned", deceasedName)
  override def valueQuestion = messagesApi("page.iht.application.assets.businessInterest.inputLabel1")
  override def hasValueQuestionHelp = true
  override def valueQuestionHelp = messagesApi("page.iht.application.assets.businessInterest.hint")
  override def returnLinkText = messagesApi("page.iht.application.return.to.assetsOf", deceasedName)
  override def returnLinkUrl = AssetsOverviewController.onPageLoad().url

  "Business Interests view" must {
    behave like yesNoValueView

    "show the correct guidance" in {
      val f = fixture()
      messagesShouldBePresent(f.view,
        messagesApi("page.iht.application.assets.businessInterest.description.p1", deceasedName),
        messagesApi("page.iht.application.assets.businessInterest.description.p2", deceasedName))
    }
  }

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val view = business_interests(businessInterestForm, regDetails).toString
    val doc = asDocument(view)
  }
}
