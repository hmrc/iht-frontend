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
import iht.views.html.application.asset.vehicles.vehicles_jointly_owned
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.utils.CommonHelper
import iht.constants.IhtProperties._

object VehiclesJointlyOwnedController extends VehiclesJointlyOwnedController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait VehiclesJointlyOwnedController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsVehiclesJointlyOwned)
  val submitUrl = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.assets.vehicles.routes.VehiclesOverviewController.onPageLoad(),
    Some(AssetsVehiclesSharedID))

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnPageLoad[ShareableBasicEstateElement](vehiclesJointlyOwnedForm, vehicles_jointly_owned.apply,_.allAssets.flatMap(_.vehicles))
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], ShareableBasicEstateElement) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, vehicles) => {
          val existingValue = appDetails.allAssets.flatMap(_.vehicles.flatMap(_.value))
          val existingIsOwned = appDetails.allAssets.flatMap(_.vehicles.flatMap(_.isOwned))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, vehicles = Some(vehicles)))

          (vehicles.isOwnedShare match {
            case Some(true) => _.copy(vehicles = Some(vehicles.copy(value = existingValue, isOwned = existingIsOwned) ))
            case Some(false) => _.copy(vehicles = Some(vehicles.copy(shareValue = None, value = existingValue,
                                                                    isOwned = existingIsOwned) ))
            case None => throw new RuntimeException("Not able to retrieve the value of VehiclesJointlyOwned question")
          })
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[ShareableBasicEstateElement](
        vehiclesJointlyOwnedForm,
        vehicles_jointly_owned.apply,
        updateApplicationDetails,
        submitUrl
      )
    }
  }
}
