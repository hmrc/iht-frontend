/*
 * Copyright 2018 HM Revenue & Customs
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

package iht.views.application.exemption.charity

import iht.forms.ApplicationForms._
import iht.models.application.exemptions.BasicExemptionElement
import iht.testhelpers.CommonBuilder
import iht.views.application.{CancelComponent, YesNoQuestionViewBehaviour}
import iht.views.html.application.exemption.charity.assets_left_to_charity_question
import play.api.i18n.Messages.Implicits._
import iht.testhelpers.TestHelper._

class AssetsLeftToCharityQuestionViewTest extends YesNoQuestionViewBehaviour[BasicExemptionElement] {
  val regDetails = CommonBuilder.buildRegistrationDetails1

  val deceasedName = regDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def form = assetsLeftToCharityQuestionForm

  override def formToView = form => assets_left_to_charity_question(form, regDetails)

  override def pageTitle = messagesApi("iht.estateReport.exemptions.charities.assetsLeftToACharity.title", deceasedName)

  override def browserTitle = messagesApi("page.iht.application.exemptions.assetLeftToCharity.browserTitle")

  override def guidance = guidance(
    Set(
      messagesApi("page.iht.application.exemptions.assetLeftToCharity.p1"),
      messagesApi("page.iht.application.exemptions.assetLeftToCharity.p2")
    )
  )

  override def formTarget = Some(iht.controllers.application.exemptions.charity.routes.AssetsLeftToCharityQuestionController.onSubmit())

  override val cancelId: String = "cancel-button"

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad(),
      messagesApi("page.iht.application.return.to.exemptionsOf", deceasedName),
      ExemptionsCharityID
    )
  )

  "AssetsLeftToCharityQuestionView" must {
    behave like yesNoQuestion()
  }
}
