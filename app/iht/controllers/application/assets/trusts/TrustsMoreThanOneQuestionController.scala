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

package iht.controllers.application.assets.trusts

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.ApplicationKickOutHelper
import iht.views.html.application.asset.trusts.trusts_more_than_one_question
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.utils.CommonHelper
import iht.constants.IhtProperties._

object TrustsMoreThanOneQuestionController extends TrustsMoreThanOneQuestionController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait TrustsMoreThanOneQuestionController extends EstateController {

  val submitUrl = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.assets.trusts.routes.TrustsOverviewController.onPageLoad(), Some(AssetsTrustsMultipleID))
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsMoreThanOneTrust)

  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[HeldInTrust](trustsMoreThanOneQuestionForm, trusts_more_than_one_question.apply, _.allAssets.flatMap(_.heldInTrust))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], HeldInTrust) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, heldInTrust) => {

          val existingValue = appDetails.allAssets.flatMap(_.heldInTrust.flatMap(_.value))
          val existingIsOwned = appDetails.allAssets.flatMap(_.heldInTrust.flatMap(_.isOwned))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, heldInTrust = Some(heldInTrust)))
          (_.copy(heldInTrust = Some(heldInTrust.copy(value = existingValue, isOwned = existingIsOwned) )))
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[HeldInTrust](
        trustsMoreThanOneQuestionForm,
        trusts_more_than_one_question.apply,
        updateApplicationDetails,
        submitUrl
      )
    }
  }
}
