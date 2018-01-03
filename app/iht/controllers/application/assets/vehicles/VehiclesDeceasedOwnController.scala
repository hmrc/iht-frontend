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

package iht.controllers.application.assets.vehicles

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.utils.ApplicationKickOutHelper
import iht.views.html.application.asset.vehicles.vehicles_deceased_own
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.utils.CommonHelper
import iht.constants.IhtProperties._

object VehiclesDeceasedOwnController extends VehiclesDeceasedOwnController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait VehiclesDeceasedOwnController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsVehiclesDeceasedOwned)
  val submitUrl = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.assets.vehicles.routes.VehiclesOverviewController.onPageLoad(),
    Some(AssetsVehiclesOwnID))

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnPageLoad[ShareableBasicEstateElement](vehiclesFormOwn, vehicles_deceased_own.apply,_.allAssets.flatMap(_.vehicles))
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], ShareableBasicEstateElement) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, vehicles) => {
          val existingShareValue = appDetails.allAssets.flatMap(_.vehicles.flatMap(_.shareValue))
          val existingIsOwnedShare = appDetails.allAssets.flatMap(_.vehicles.flatMap(_.isOwnedShare))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
            (new AllAssets(action = None, vehicles = Some(vehicles)))

          (vehicles.isOwned match {
            case Some(true) => _.copy(vehicles = Some(vehicles.copy(shareValue = existingShareValue,
                                                                    isOwnedShare = existingIsOwnedShare) ))
            case Some(false) => _.copy(vehicles = Some(vehicles.copy(value = None, shareValue = existingShareValue,
                                                                    isOwnedShare = existingIsOwnedShare) ))
            case None => throw new RuntimeException("Not able to retrieve the value of VehiclesDeceasedOwned question")
          })
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[ShareableBasicEstateElement](
        vehiclesFormOwn,
        vehicles_deceased_own.apply,
        updateApplicationDetails,
        submitUrl
        )
    }
  }
}
