/*
 * Copyright 2020 HM Revenue & Customs
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

package iht.controllers.application.assets.properties

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationController
import iht.metrics.IhtMetrics
import iht.models.application.ApplicationDetails
import iht.models.application.debts.{Mortgage, MortgageEstateElement}
import iht.utils.CommonHelper
import javax.inject.Inject
import play.api.Logger
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever


class DeletePropertyControllerImpl @Inject()(val metrics: IhtMetrics,
                                             val ihtConnector: IhtConnector,
                                             val cachingConnector: CachingConnector,
                                             val authConnector: AuthConnector,
                                             val formPartialRetriever: FormPartialRetriever,
                                             implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with DeletePropertyController

trait DeletePropertyController extends ApplicationController {


  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withRegistrationDetails { registrationData =>
        for {
          applicationDetails <- ihtConnector.getApplication(getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(registrationData.ihtReference),
            registrationData.acknowledgmentReference)
        } yield {
          applicationDetails match {
            case Some(applicationDetails) => {
              applicationDetails.propertyList.find(p => p.id.getOrElse("") equals id).fold {
                Logger.warn("No Property Found. Redirecting to Internal Server Error")
                InternalServerError("No Property Found")
              } {
                (matchedProperty) => Ok(iht.views.html.application.asset.properties.delete_property_confirm(matchedProperty))
              }
            }
            case _ => {
              Logger.warn("Problem retrieving application details. Redirecting to Internal Server Error")
              InternalServerError("No application details found")
            }
          }
        }
      }
    }
  }

  def onSubmit(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withRegistrationDetails { registrationData =>
        for {
          applicationDetails: Option[ApplicationDetails] <- ihtConnector.getApplication(
            getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(registrationData.ihtReference),
            registrationData.acknowledgmentReference)
          propertyListNew = applicationDetails.map(_.propertyList.filterNot(p => p.id.getOrElse("") == id)).getOrElse(Nil)
          mortgageEstateElement: Option[MortgageEstateElement] = applicationDetails.flatMap(_.allLiabilities.flatMap(_.mortgages))
          mortgageEstateElementNew: Option[MortgageEstateElement] = updateMortgageEstateElementWithDeletedMortgage(mortgageEstateElement, id)
          applicationDetailsNew: Option[ApplicationDetails] = applicationDetails.map(
            x => x.copy(propertyList = propertyListNew, allLiabilities = x.allLiabilities.map(_.copy(mortgages = mortgageEstateElementNew))))
          storedApplication <- ihtConnector.saveApplication(
            getNino(userNino),
            CommonHelper.getOrExceptionNoApplication(applicationDetailsNew),
            registrationData.acknowledgmentReference)
        } yield {
          storedApplication match {
            case Some(_) => Redirect(CommonHelper.addFragmentIdentifier(
              routes.PropertiesOverviewController.onPageLoad(), Some(appConfig.AssetsPropertiesAddPropertyID)))
            case _ => {
              Logger.warn("Problem storing Application details. Redirecting to InternalServerError")
              InternalServerError
            }
          }
        }
      }
    }
  }

  private def updateMortgageEstateElementWithDeletedMortgage(mortgageEstateElement: Option[MortgageEstateElement],
                                                             mortgageId: String): Option[MortgageEstateElement] = {
    val mortgages: List[Mortgage] = mortgageEstateElement.map(_.mortgageList).getOrElse(Nil)
    val mortgagesNew: List[Mortgage] = mortgages.filterNot(m => m.id == mortgageId)
    val isOwnedNew: Option[Boolean] = mortgageEstateElement.flatMap(_.isOwned) match {
      case Some(true) if !mortgagesNew.exists(_.value.getOrElse(0) != 0) => None
      case a => a
    }
    mortgageEstateElement.map(_.copy(isOwned = isOwnedNew, mortgageList = mortgagesNew))
  }
}
