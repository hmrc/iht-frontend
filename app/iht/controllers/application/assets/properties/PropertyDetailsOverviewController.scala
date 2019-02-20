/*
 * Copyright 2019 HM Revenue & Customs
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
import iht.controllers.application.EstateController
import iht.metrics.IhtMetrics
import iht.utils.{CommonHelper, DeceasedInfoHelper, StringHelper}
import javax.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

/**
  * Created by james on 16/06/16.
  */
class PropertyDetailsOverviewControllerImpl @Inject()(val metrics: IhtMetrics,
                                                      val ihtConnector: IhtConnector,
                                                      val cachingConnector: CachingConnector,
                                                      val authConnector: AuthConnector,
                                                      val formPartialRetriever: FormPartialRetriever) extends PropertyDetailsOverviewController

trait PropertyDetailsOverviewController extends EstateController {


  def ihtConnector: IhtConnector

  def cachingConnector: CachingConnector

  def onPageLoad = authorisedForIht {
    implicit request => {
      withRegistrationDetails { registrationDetails =>
        val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(registrationDetails)
        Future.successful(Ok(iht.views.html.application.asset.properties.property_details_overview(deceasedName)))
      }
    }
  }

  def onEditPageLoad(propertyId: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {

      withRegistrationDetails { registrationDetails =>
        val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(registrationDetails)

        for {
          applicationDetails <- ihtConnector.getApplication(StringHelper.getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
            registrationDetails.acknowledgmentReference)
        } yield {
          applicationDetails match {
            case Some(applicationDetails) => {
              applicationDetails.propertyList.find(property => property.id.getOrElse("") equals propertyId).fold {
                Logger.info(s"User attempted to navigate to property details of non-existent property (id $propertyId)")
                Redirect(iht.controllers.application.assets.properties.routes.PropertiesOverviewController.onPageLoad())
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
