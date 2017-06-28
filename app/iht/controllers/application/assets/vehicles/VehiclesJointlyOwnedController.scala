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

package iht.controllers.application.assets.vehicles

import javax.inject.{Inject, Singleton}

import iht.constants.IhtProperties
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset.vehicles.vehicles_jointly_owned
import play.api.i18n.MessagesApi

@Singleton
class VehiclesJointlyOwnedController @Inject()(val messagesApi: MessagesApi, val ihtProperties: IhtProperties, val applicationForms: ApplicationForms) extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsVehiclesJointlyOwned)
  val submitUrl = CommonHelper
    .addFragmentIdentifier(iht.controllers.application.assets.vehicles.routes.VehiclesOverviewController.onPageLoad(),
      Some(ihtProperties.AssetsVehiclesSharedID))

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
