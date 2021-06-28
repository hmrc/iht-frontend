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

package iht.controllers.application.assets.properties

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.metrics.IhtMetrics
import iht.utils.{CommonHelper, DeceasedInfoHelper}
import javax.inject.Inject
import play.api.Logging
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.asset.properties.property_details_overview

import scala.concurrent.Future

/**
  * Created by james on 16/06/16.
  */
class PropertyDetailsOverviewControllerImpl @Inject()(val metrics: IhtMetrics,
                                                      val ihtConnector: IhtConnector,
                                                      val cachingConnector: CachingConnector,
                                                      val authConnector: AuthConnector,
                                                      val propertyDetailsOverviewView: property_details_overview,
                                                      implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with PropertyDetailsOverviewController

trait PropertyDetailsOverviewController extends EstateController with Logging {


  def ihtConnector: IhtConnector

  def cachingConnector: CachingConnector
  val propertyDetailsOverviewView: property_details_overview
  def onPageLoad = authorisedForIht {
    implicit request => {
      withRegistrationDetails { registrationDetails =>
        val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(registrationDetails)
        Future.successful(Ok(propertyDetailsOverviewView(deceasedName)))
      }
    }
  }

  def onEditPageLoad(propertyId: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {

      withRegistrationDetails { registrationDetails =>
        val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(registrationDetails)

        for {
          applicationDetails <- ihtConnector.getApplication(getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
            registrationDetails.acknowledgmentReference)
        } yield {
          applicationDetails match {
            case Some(applicationDetails) => {
              applicationDetails.propertyList.find(property => property.id.getOrElse("") equals propertyId).fold {
                logger.info(s"User attempted to navigate to property details of non-existent property (id $propertyId)")
                Redirect(iht.controllers.application.assets.properties.routes.PropertiesOverviewController.onPageLoad())
              } {
                (matchedProperty) =>
                  Ok(propertyDetailsOverviewView(
                    deceasedName,
                    Some(matchedProperty)
                  ))
              }
            }
            case _ => {
              logger.warn("Problem retrieving Application Details. Redirecting to Internal Server Error")
              InternalServerError("No Application Details found")
            }
          }
        }
      }
    }
  }

}
