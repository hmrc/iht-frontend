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

package iht.controllers.application.assets.household

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.IhtMetrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset.household.household_jointly_owned
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class HouseholdJointlyOwnedControllerImpl @Inject()(val metrics: IhtMetrics,
                                                    val ihtConnector: IhtConnector,
                                                    val cachingConnector: CachingConnector,
                                                    val authConnector: AuthConnector,
                                                    val householdJointlyOwnedView: household_jointly_owned,
                                                    implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with HouseholdJointlyOwnedController {

}

trait HouseholdJointlyOwnedController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsHouseholdJointlyOwned)

  lazy val submitUrl = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.assets.household.routes.HouseholdOverviewController.onPageLoad(), Some(appConfig.AssetsHouseholdSharedID))

  val householdJointlyOwnedView: household_jointly_owned

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      estateElementOnPageLoad[ShareableBasicEstateElement](householdJointlyOwnedForm, householdJointlyOwnedView.apply, _.allAssets.flatMap(_.household), userNino)
    }
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], ShareableBasicEstateElement) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, household) => {
          val existingValue = appDetails.allAssets.flatMap(_.household.flatMap(_.value))
          val existingIsOwned = appDetails.allAssets.flatMap(_.household.flatMap(_.isOwned))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, household = Some(household)))

          (household.isOwnedShare match {
            case Some(true) => _.copy(household = Some(household.copy(value = existingValue, isOwned = existingIsOwned)))
            case Some(false) => _.copy(household = Some(household.copy(shareValue = None, value = existingValue,
              isOwned = existingIsOwned)))
            case None => throw new RuntimeException("Not able to retrieve the value of household jointly owed question")
          })
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[ShareableBasicEstateElement](
        householdJointlyOwnedForm,
        householdJointlyOwnedView.apply,
        updateApplicationDetails,
        submitUrl,
        userNino
      )
    }
  }
}
