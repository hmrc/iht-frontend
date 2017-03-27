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

package iht.controllers.application.assets.household

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.utils.ApplicationKickOutHelper
import iht.views.html.application.asset.household.household_jointly_owned
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.utils.CommonHelper
import iht.constants.IhtProperties._

object HouseholdJointlyOwnedController extends HouseholdJointlyOwnedController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait HouseholdJointlyOwnedController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsHouseholdJointlyOwned)
  val submitUrl = CommonHelper.addFragmentIdentifier(iht.controllers.application.assets.household.routes.HouseholdOverviewController.onPageLoad(), Some(AssetsHouseholdSharedID))

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnPageLoad[ShareableBasicEstateElement](householdJointlyOwnedForm, household_jointly_owned.apply,_.allAssets.flatMap(_.household))
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
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
        household_jointly_owned.apply,
        updateApplicationDetails,
        submitUrl
      )
    }
  }
}
