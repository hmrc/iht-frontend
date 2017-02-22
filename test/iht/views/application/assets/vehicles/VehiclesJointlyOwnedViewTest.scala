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

import iht.controllers.application.assets.vehicles.routes
import iht.controllers.application.assets.vehicles.routes._
import iht.forms.ApplicationForms._
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.vehicles.vehicles_jointly_owned
import play.api.i18n.Messages.Implicits._
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class VehiclesJointlyOwnedViewTest extends ShareableElementInputViewBehaviour[ShareableBasicEstateElement] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def form:Form[ShareableBasicEstateElement] = vehiclesJointlyOwnedForm
  override def formToView:Form[ShareableBasicEstateElement] => Appendable = form => vehicles_jointly_owned(form, regDetails)

  override def pageTitle = messagesApi("page.iht.application.assets.vehicles.jointly.owned.title")
  override def browserTitle = messagesApi("page.iht.application.assets.vehicles.jointly.owned.browserTitle")
  override def questionTitle = messagesApi("iht.estateReport.assets.vehicles.jointly.owned.question", deceasedName)
  override def valueQuestion = messagesApi("iht.estateReport.assets.vehicles.valueOfJointlyOwned")
  override def hasValueQuestionHelp = false
  override def valueQuestionHelp = ""
  override def returnLinkText = messagesApi("site.link.return.vehicles")
  override def returnLinkUrl = VehiclesOverviewController.onPageLoad().url
  override def formTarget =Some(routes.VehiclesJointlyOwnedController.onSubmit)

  "Vehicles Jointly Owned view" must {
    behave like yesNoValueViewJoint
  }

}
