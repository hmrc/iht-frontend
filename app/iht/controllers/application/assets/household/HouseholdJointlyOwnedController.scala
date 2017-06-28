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

import javax.inject.{Inject, Singleton}

import iht.constants.IhtProperties
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset.household.household_jointly_owned
import play.api.i18n.MessagesApi

@Singleton
class HouseholdJointlyOwnedController @Inject()(
                                                 val messagesApi: MessagesApi,
                                                 val ihtProperties: IhtProperties,
                                                 val applicationForms: ApplicationForms) extends EstateController {

  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsHouseholdJointlyOwned)
  val submitUrl = CommonHelper
    .addFragmentIdentifier(iht.controllers.application.assets.household.routes.HouseholdOverviewController.onPageLoad(),
      Some(ihtProperties.AssetsHouseholdSharedID))

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnPageLoad[ShareableBasicEstateElement](applicationForms.householdJointlyOwnedForm, household_jointly_owned.apply,_.allAssets.flatMap(_.household))
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
        applicationForms.householdJointlyOwnedForm,
        household_jointly_owned.apply,
        updateApplicationDetails,
        submitUrl
      )
    }
  }
}
