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

package iht.views.application.assets.properties

import iht.forms.ApplicationForms._
import iht.models.application.assets.Properties
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.views.application.{CancelComponent, YesNoQuestionViewBehaviour}
import iht.views.html.application.asset.properties.properties_owned_question
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class PropertiesOwnedQuestionViewTest extends YesNoQuestionViewBehaviour[Properties] {
  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)
  lazy val propertiesOwnedQuestionView: properties_owned_question = app.injector.instanceOf[properties_owned_question]

  override def guidance = guidance(
    Set(
      messagesApi("page.iht.application.assets.properties.question.p1", deceasedName)
    )
  )

  override def pageTitle = messagesApi("iht.estateReport.assets.propertiesBuildingsAndLand")

  override def browserTitle = messagesApi("iht.estateReport.assets.propertiesBuildingsAndLand")

  override def formTarget = Some(iht.controllers.application.assets.properties.routes.PropertiesOwnedQuestionController.onSubmit)

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad,
      messagesApi("page.iht.application.return.to.assetsOf", deceasedName),
      TestHelper.AppSectionPropertiesID
    )
  )

  override def form: Form[Properties] = propertiesForm

  override def formToView: Form[Properties] => Appendable =
    form =>
      propertiesOwnedQuestionView(form, registrationDetails)

  "Properties Owned Question View" must {
    behave like yesNoQuestionWithLegend(messagesApi("page.iht.application.assets.properties.question.question", deceasedName))
  }
}
