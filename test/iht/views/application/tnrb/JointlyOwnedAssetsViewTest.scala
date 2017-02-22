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

package iht.views.application.tnrb

import iht.forms.TnrbForms._
import iht.models.application.tnrb.TnrbEligibiltyModel
import iht.testhelpers.CommonBuilder
import iht.views.application.YesNoQuestionViewBehaviourSubmittable
import iht.views.html.application.tnrb.{jointly_owned_assets, permanent_home}
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.twirl.api.HtmlFormat.Appendable

class JointlyOwnedAssetsViewTest extends YesNoQuestionViewBehaviourSubmittable[TnrbEligibiltyModel] {

  override def guidance = noGuidance

  def tnrbModel = CommonBuilder.buildTnrbEligibility

  def widowCheck = CommonBuilder.buildWidowedCheck

  val deceasedDetailsName = CommonBuilder.buildDeceasedDetails.name

  override def pageTitle = messagesApi("page.iht.application.tnrb.jointlyOwnedAssets.question", deceasedDetailsName)

  override def browserTitle = messagesApi("page.iht.application.tnrb.jointlyOwnedAssets.browserTitle")

  override def formTarget = Some(iht.controllers.application.tnrb.routes.JointlyOwnedAssetsController.onSubmit())

  override def form: Form[TnrbEligibiltyModel] = jointAssetPassedForm

  override def formToView: Form[TnrbEligibiltyModel] => Appendable =
    form =>
      jointly_owned_assets(form, deceasedDetailsName)

  override def cancelComponent = None

  "Jointly Owned Assets page Question View" must {
    behave like yesNoQuestion
  }
}
