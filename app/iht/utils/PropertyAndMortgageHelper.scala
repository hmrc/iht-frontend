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

package iht.utils

import iht.constants.IhtProperties.{AppSectionPropertiesID, AssetsPropertiesOwnedID}
import iht.models.application.ApplicationDetails
import iht.models.application.assets.{Properties, Property}
import iht.models.application.debts.{Mortgage, MortgageEstateElement}
import play.api.Logger
import play.api.mvc._

object PropertyAndMortgageHelper {

  def previousValueOfIsPropertyOwned(appDetails: ApplicationDetails): Option[Boolean] = {
    appDetails.allAssets.flatMap(_.properties.flatMap(_.isOwned))
  }

  def doesPropertyListContainProperties(appDetails: ApplicationDetails): Boolean = appDetails.propertyList.nonEmpty

  def updatePropertyList(properties: Properties, appDetails: ApplicationDetails): List[Property] = {
    properties.isOwned match {
      case Some(false) => Nil
      case _ => appDetails.propertyList
    }
  }

  def isMortgagesLargerThanProperties(appDetails: ApplicationDetails): Boolean = {
    val properties = appDetails.propertyList.toSet
    val mortgages: Option[MortgageEstateElement] = appDetails.allLiabilities.flatMap(_.mortgages)
    if(mortgages.isDefined)  mortgages.get.mortgageList.size > properties.size else false
  }

  def reduceMortgagesToMatchProperties(appDetails: ApplicationDetails): List[Mortgage] = {
    val mortgages: Option[MortgageEstateElement] = appDetails.allLiabilities.flatMap(_.mortgages)
    val newMortgages: List[Mortgage] = mortgages.get.mortgageList take appDetails.propertyList.size
    newMortgages
  }

  def updateMortgages(properties: Properties, appDetails: ApplicationDetails): Option[MortgageEstateElement] = {
    (properties.isOwned, isMortgagesLargerThanProperties(appDetails)) match {
      case (Some(false), _) => None
      case (Some(true), false) => appDetails.allLiabilities.flatMap(_.mortgages)
      case (Some(true), true) => Some(MortgageEstateElement(Some(true), reduceMortgagesToMatchProperties(appDetails)))
      case _ => None
    }
  }

  def determineRedirectLocationForPropertiesOwnedQuestion(properties: Properties,
                                                          appDetails: ApplicationDetails): Result = {
    (properties.isOwned,
      PropertyAndMortgageHelper.previousValueOfIsPropertyOwned(appDetails),
      PropertyAndMortgageHelper.doesPropertyListContainProperties(appDetails)) match {
      case (Some(false), _, _) => Results.Redirect(CommonHelper.addFragmentIdentifier(
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        Some(AppSectionPropertiesID)))
      case (Some(true), Some(true), _) =>
        Results.Redirect(CommonHelper.addFragmentIdentifier(
          iht.controllers.application.assets.properties.routes.PropertiesOverviewController.onPageLoad(),
          Some(AssetsPropertiesOwnedID)))
      case (Some(true), _, false) =>
        Results.Redirect(
          iht.controllers.application.assets.properties.routes.PropertyDetailsOverviewController.onPageLoad())
      case (_, _, true) =>
        Results.Redirect(CommonHelper.addFragmentIdentifier(
          iht.controllers.application.assets.properties.routes.PropertiesOverviewController.onPageLoad(),
          Some(AssetsPropertiesOwnedID)))
      case _ =>
        Logger.warn("Problem storing Application details. Redirecting to InternalServerError")
        Results.InternalServerError
    }
  }

}
