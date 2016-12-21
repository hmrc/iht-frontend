/*
 * Copyright 2016 HM Revenue & Customs
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

import iht.controllers.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.utils.ApplicationKickOutHelper
import iht.views.html.application.asset.household.household_deceased_own

object HouseholdDeceasedOwnController extends HouseholdDeceasedOwnController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait HouseholdDeceasedOwnController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsHouseholdDeceasedOwned)
  val submitUrl = iht.controllers.application.assets.household.routes.HouseholdOverviewController.onPageLoad()

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnPageLoad[ShareableBasicEstateElement](householdFormOwn, household_deceased_own.apply,_.allAssets.flatMap(_.household))
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], ShareableBasicEstateElement) => (ApplicationDetails,Option[String]) =
        (appDetails, _, household) => {
          val existingShareValue = appDetails.allAssets.flatMap(_.household.flatMap(_.shareValue))
          val existingIsOwnedShare = appDetails.allAssets.flatMap(_.household.flatMap(_.isOwnedShare))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
            (new AllAssets(action = None, household = Some(household)))
            (household.isOwned match {
              case Some(true) => _.copy(household = Some(household.copy(shareValue = existingShareValue,
                                        isOwnedShare = existingIsOwnedShare) ))
              case Some(false) => _.copy(household = Some(household.copy(value = None, shareValue = existingShareValue,
                                        isOwnedShare = existingIsOwnedShare) ))
              case None => throw new RuntimeException("Not able to retrieve the value of household owed question")
            })
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[ShareableBasicEstateElement](
        householdFormOwn,
        household_deceased_own.apply,
        updateApplicationDetails,
        submitUrl,
        Some(createValidationFunction("isOwned", _.isDefined, "error.assets.household.deceasedOwned.select"))
      )
    }
  }
}
