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

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.ApplicationKickOutHelper
import iht.views.html.application.asset.pensions.pensions_value
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object PensionsValueController extends PensionsValueController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait PensionsValueController extends EstateController {

  val submitUrl = iht.controllers.application.assets.pensions.routes.PensionsOverviewController.onPageLoad()
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsPensionsValue)

  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[PrivatePension](pensionsValueForm, pensions_value.apply, _.allAssets.flatMap(_.privatePension))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], PrivatePension) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, privatePension) => {

          val existingIsChanged = appDetails.allAssets.flatMap(_.privatePension.flatMap(_.isChanged))
          val existingIsOwned = appDetails.allAssets.flatMap(_.privatePension.flatMap(_.isOwned))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, privatePension = Some(privatePension)))
          (_.copy(privatePension = Some(privatePension.copy(isChanged = existingIsChanged, isOwned = existingIsOwned) )))
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[PrivatePension](
        pensionsValueForm,
        pensions_value.apply,
        updateApplicationDetails,
        submitUrl
      )
    }
  }
}
