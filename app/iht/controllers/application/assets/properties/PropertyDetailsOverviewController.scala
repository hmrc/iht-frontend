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

package iht.controllers.application.assets.properties

import iht.connector.{CachingConnector, IhtConnector}
import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.metrics.Metrics
import iht.utils.CommonHelper
import play.api.Logger
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

/**
  * Created by james on 16/06/16.
  */
object PropertyDetailsOverviewController extends PropertyDetailsOverviewController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait PropertyDetailsOverviewController extends EstateController {

  def ihtConnector: IhtConnector

  def cachingConnector: CachingConnector

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        withExistingRegistrationDetails { registrationDetails =>
          val deceasedName = CommonHelper.getOrException(registrationDetails.deceasedDetails).name
          Future.successful(Ok(iht.views.html.application.asset.properties.property_details_overview(deceasedName)))
        }
      }
  }

  def onEditPageLoad(propertyId: String) = authorisedForIht {
    implicit user =>
      implicit request => {

        withExistingRegistrationDetails { registrationDetails =>
          val deceasedName = CommonHelper.getOrException(registrationDetails.deceasedDetails).name

          for {
            applicationDetails <- ihtConnector.getApplication(CommonHelper.getNino(user),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(applicationDetails) => {
                applicationDetails.propertyList.find(property => property.id.getOrElse("") equals propertyId).fold {
                  throw new RuntimeException("No Property found for the id")
                } {
                  (matchedProperty) =>
                    Ok(iht.views.html.application.asset.properties.property_details_overview(
                      deceasedName,
                      Some(matchedProperty)
                    ))
                }
              }
              case _ => {
                Logger.warn("Problem retrieving Application Details. Redirecting to Internal Server Error")
                InternalServerError("No Application Details found")
              }
            }
          }
        }
      }
  }

}
