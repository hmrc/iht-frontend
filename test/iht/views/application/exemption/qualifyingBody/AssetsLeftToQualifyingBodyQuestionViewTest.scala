/*
 * Copyright 2020 HM Revenue & Customs
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

package iht.views.application.exemption.qualifyingBody

import iht.forms.ApplicationForms._
import iht.models.application.exemptions.BasicExemptionElement
import iht.testhelpers.CommonBuilder
import iht.views.application.{CancelComponent, YesNoQuestionViewBehaviour}
import iht.views.html.application.exemption.qualifyingBody.assets_left_to_qualifying_body_question
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig
import iht.testhelpers.TestHelper._

class AssetsLeftToQualifyingBodyQuestionViewTest extends YesNoQuestionViewBehaviour[BasicExemptionElement] {
  val regDetails = CommonBuilder.buildRegistrationDetails1

  val deceasedName = regDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def form = assetsLeftToQualifyingBodyQuestionForm

  override def formToView = form => assets_left_to_qualifying_body_question(form, regDetails)

  override def pageTitle = messagesApi("page.iht.application.exemptions.assetsLeftToQualifyingBody.sectionTitle", deceasedName)

  override def browserTitle = messagesApi("page.iht.application.exemptions.assetsLeftToQualifyingBody.browserTitle")

  override def guidance = guidance(
    Set(
      messagesApi("page.iht.application.exemptions.assetsLeftToQualifyingBody.p1"),
      messagesApi("page.iht.application.exemptions.assetsLeftToQualifyingBody.p2"),
      messagesApi("iht.estateReport.exemptions.qualifyingBodies.assetsLeftToQualifyingBody.p3"),
      messagesApi("iht.estateReport.exemptions.qualifyingBodies.howFindOutQualifies"),
      messagesApi("page.iht.application.exemptions.assetsLeftToQualifyingBody.help.contents")
    )
  )

  override def formTarget = Some(iht.controllers.application.exemptions.qualifyingBody.routes.AssetsLeftToQualifyingBodyQuestionController.onSubmit())

  override val cancelId: String = "cancel-button"

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad(),
      messagesApi("page.iht.application.return.to.exemptionsOf", deceasedName),
      ExemptionsOtherID
    )
  )

  "AssetsLeftToQualifyingBodyQuestionView" must {
    behave like yesNoQuestion()
  }
}
