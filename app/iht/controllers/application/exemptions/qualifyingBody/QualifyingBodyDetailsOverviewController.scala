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

package iht.controllers.application.exemptions.qualifyingBody

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.utils.CommonHelper
import javax.inject.Inject
import play.api.Logging
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.exemption.qualifyingBody.qualifying_body_details_overview

import scala.concurrent.Future

/**
  * Created by jennygj on 15/08/16.
  */

class QualifyingBodyDetailsOverviewControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                            val cachingConnector: CachingConnector,
                                                            val authConnector: AuthConnector,
                                                            val qualifyingBodyDetailsOverviewView: qualifying_body_details_overview,
                                                            implicit val appConfig: AppConfig,
                                                            val cc: MessagesControllerComponents)
  extends FrontendController(cc) with QualifyingBodyDetailsOverviewController {

}

trait QualifyingBodyDetailsOverviewController extends EstateController with Logging {


  def ihtConnector: IhtConnector

  def cachingConnector: CachingConnector
  val qualifyingBodyDetailsOverviewView: qualifying_body_details_overview
  def onPageLoad() = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withApplicationDetails(userNino) { rd =>
        ad =>
          Future.successful(Ok(qualifyingBodyDetailsOverviewView()))
      }
    }
  }

  def onEditPageLoad(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {

      withRegistrationDetails { registrationDetails =>
        for {
          applicationDetails <- ihtConnector.getApplication(getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
            registrationDetails.acknowledgmentReference)
        } yield {
          applicationDetails match {
            case Some(applicationDetails) =>
              applicationDetails.qualifyingBodies.find(qualifyingBody => qualifyingBody.id.contains(id)).fold {
                throw new RuntimeException("No qualifyingBody found for id " + id)
              } {
                (matchedQualifyingBody) =>
                  Ok(qualifyingBodyDetailsOverviewView(Some(matchedQualifyingBody)
                  ))
              }
            case _ =>
              logger.warn("Problem retrieving Application Details. Redirecting to Internal Server Error")
              InternalServerError("No Application Details found")
          }
        }
      }
    }
  }
}
