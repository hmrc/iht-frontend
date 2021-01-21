/*
 * Copyright 2021 HM Revenue & Customs
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

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset.vehicles.vehicles_deceased_own
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class VehiclesDeceasedOwnControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                  val cachingConnector: CachingConnector,
                                                  val authConnector: AuthConnector,
                                                  val formPartialRetriever: FormPartialRetriever,
                                                  implicit val appConfig: AppConfig,
                                                  val cc: MessagesControllerComponents) extends FrontendController(cc) with VehiclesDeceasedOwnController

trait VehiclesDeceasedOwnController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsVehiclesDeceasedOwned)


  lazy val submitUrl = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.assets.vehicles.routes.VehiclesOverviewController.onPageLoad(),
    Some(appConfig.AssetsVehiclesOwnID))

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      estateElementOnPageLoad[ShareableBasicEstateElement](vehiclesFormOwn, vehicles_deceased_own.apply, _.allAssets.flatMap(_.vehicles), userNino)
    }
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], ShareableBasicEstateElement) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, vehicles) => {
          val existingShareValue = appDetails.allAssets.flatMap(_.vehicles.flatMap(_.shareValue))
          val existingIsOwnedShare = appDetails.allAssets.flatMap(_.vehicles.flatMap(_.isOwnedShare))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, vehicles = Some(vehicles)))

          (vehicles.isOwned match {
            case Some(true) => _.copy(vehicles = Some(vehicles.copy(shareValue = existingShareValue,
              isOwnedShare = existingIsOwnedShare)))
            case Some(false) => _.copy(vehicles = Some(vehicles.copy(value = None, shareValue = existingShareValue,
              isOwnedShare = existingIsOwnedShare)))
            case None => throw new RuntimeException("Not able to retrieve the value of VehiclesDeceasedOwned question")
          })
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[ShareableBasicEstateElement](
        vehiclesFormOwn,
        vehicles_deceased_own.apply,
        updateApplicationDetails,
        submitUrl,
        userNino
      )
    }
  }
}
