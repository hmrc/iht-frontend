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

package iht.views.application.assets.vehicles

import iht.controllers.application.assets.vehicles.routes
import iht.controllers.application.assets.vehicles.routes._
import iht.forms.ApplicationForms._
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.testhelpers.CommonBuilder
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.vehicles.vehicles_deceased_own
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class VehiclesDeceasedOwnViewTest  extends ShareableElementInputViewBehaviour[ShareableBasicEstateElement] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def form:Form[ShareableBasicEstateElement] = vehiclesFormOwn
  lazy val vehiclesDeceasedOwnView: vehicles_deceased_own = app.injector.instanceOf[vehicles_deceased_own]

  override def formToView:Form[ShareableBasicEstateElement] => Appendable = form => vehiclesDeceasedOwnView(form, regDetails)

  override def pageTitle = messagesApi("iht.estateReport.assets.vehiclesOwned", deceasedName)
  override def browserTitle = messagesApi("page.iht.application.assets.vehicles.deceased.browserTitle")
  override def questionTitle = messagesApi("iht.estateReport.assets.vehicles.ownName.question", deceasedName)
  override def valueQuestion = messagesApi("page.iht.application.assets.vehicles.deceased.value", deceasedName)
  override def hasValueQuestionHelp = true
  override def valueQuestionHelp = messagesApi("iht.estateReport.assets.getProfessionalValuation")
  override def returnLinkText = messagesApi("site.link.return.vehicles")
  override def returnLinkUrl = VehiclesOverviewController.onPageLoad.url
  override def linkHash = appConfig.AssetsVehiclesOwnID
  override def formTarget =Some(routes.VehiclesDeceasedOwnController.onSubmit)

  "Vehicles Deceased Own view" must {
    behave like yesNoValueView
  }

}
