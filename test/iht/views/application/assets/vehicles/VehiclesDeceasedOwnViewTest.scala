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

package iht.views.application.assets.vehicles

import iht.controllers.application.assets.vehicles.routes._
import iht.forms.ApplicationForms._
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.vehicles.vehicles_deceased_own
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class VehiclesDeceasedOwnViewTest  extends ViewTestHelper with ShareableElementInputViewBehaviour {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def pageTitle = Messages("iht.estateReport.assets.vehiclesOwned", deceasedName)
  override def browserTitle = Messages("page.iht.application.assets.vehicles.deceased.browserTitle")
  override def questionTitle = Messages("iht.estateReport.assets.vehicles.ownName.question", deceasedName)
  override def valueQuestion = Messages("page.iht.application.assets.vehicles.deceased.value")
  override def hasValueQuestionHelp = true
  override def valueQuestionHelp = Messages("iht.estateReport.assets.getProfessionalValuation")
  override def returnLinkText = Messages("site.link.return.vehicles")
  override def returnLinkUrl = VehiclesOverviewController.onPageLoad().url

  "Vehicles Deceased Own view" must {
    behave like yesNoValueView
  }

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val view = vehicles_deceased_own(vehiclesFormOwn, regDetails).toString
    val doc = asDocument(view)
  }
}
