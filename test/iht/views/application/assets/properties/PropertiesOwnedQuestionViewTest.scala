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

package iht.views.application.assets.properties

import iht.forms.ApplicationForms._
import iht.models.application.assets.Properties
import iht.testhelpers.CommonBuilder
import iht.views.application.{CancelComponent, YesNoQuestionViewBehaviour}
import iht.views.html.application.asset.properties.properties_owned_question
import iht.views.html.application.asset.trusts.trusts_owned_question
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

class PropertiesOwnedQuestionViewTest extends YesNoQuestionViewBehaviour[Properties] {
  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidance = guidance(
    Set(
      Messages("page.iht.application.assets.properties.question.p1", deceasedName)
    )
  )

  override def pageTitle = Messages("iht.estateReport.assets.propertiesBuildingsAndLand")

  override def browserTitle = Messages("iht.estateReport.assets.propertiesBuildingsAndLand")

  override def formTarget = Some(iht.controllers.application.assets.properties.routes.PropertiesOwnedQuestionController.onSubmit())

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
      Messages("page.iht.application.return.to.assetsOf", deceasedName)
    )
  )

  override def form: Form[Properties] = propertiesForm

  override def formToView: Form[Properties] => Appendable =
    form =>
      properties_owned_question(form, registrationDetails)

  "Properties Owned Question View" must {
    behave like yesNoQuestionWithLegend(Messages("page.iht.application.assets.properties.question.question", deceasedName))
  }
}
