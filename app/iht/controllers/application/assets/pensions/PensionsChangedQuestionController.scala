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

package iht.controllers.application.assets.pensions

import iht.controllers.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.ApplicationKickOutHelper
import iht.views.html.application.asset.pensions.pensions_changed_question

object PensionsChangedQuestionController extends PensionsChangedQuestionController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait PensionsChangedQuestionController extends EstateController {

  val submitUrl = iht.controllers.application.assets.pensions.routes.PensionsOverviewController.onPageLoad()
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsPensions)

  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[PrivatePension](pensionsChangedQuestionForm, pensions_changed_question.apply, _.allAssets.flatMap(_.privatePension))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], PrivatePension) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, privatePension) => {

          val existingValue = appDetails.allAssets.flatMap(_.privatePension.flatMap(_.value))
          val existingIsOwned = appDetails.allAssets.flatMap(_.privatePension.flatMap(_.isOwned))

         val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, privatePension = Some(privatePension)))
          (_.copy(privatePension = Some(privatePension.copy(value = existingValue, isOwned = existingIsOwned) )))
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[PrivatePension](
        pensionsChangedQuestionForm,
        pensions_changed_question.apply,
        updateApplicationDetails,
        submitUrl,
        Some(createValidationFunction("isChanged", _.isDefined, "error.assets.privatePensions.changed.select"))
      )
    }
  }
}
