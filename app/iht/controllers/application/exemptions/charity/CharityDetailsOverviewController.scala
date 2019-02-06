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

package iht.controllers.application.exemptions.charity

import iht.config.{AppConfig, FrontendAuthConnector}
import iht.connector.{CachingConnector, IhtConnector, IhtConnectors}
import iht.controllers.application.EstateController
import iht.metrics.Metrics
import iht.utils.{CommonHelper, StringHelper}
import javax.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.PlayAuthConnector

import scala.concurrent.Future
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}

/**
  * Created by jennygj on 15/08/16.
  */

class CharityDetailsOverviewControllerImpl @Inject()() extends CharityDetailsOverviewController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait CharityDetailsOverviewController extends EstateController {


  def ihtConnector: IhtConnector

  def cachingConnector: CachingConnector

  def onPageLoad() = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withApplicationDetails(userNino) { rd =>
        ad =>
          Future.successful(Ok(iht.views.html.application.exemption.charity.charity_details_overview()))
      }
    }
  }

  def onEditPageLoad(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {

      withRegistrationDetails { registrationDetails =>
        for {
          applicationDetails <- ihtConnector.getApplication(StringHelper.getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
            registrationDetails.acknowledgmentReference)
        } yield {
          applicationDetails match {
            case Some(applicationDetails) =>
              applicationDetails.charities.find(charity => charity.id.contains(id)).fold {
                throw new RuntimeException("No charity found for the id")
              } {
                (matchedCharity) =>
                  Ok(iht.views.html.application.exemption.charity.charity_details_overview(Some(matchedCharity)
                  ))
              }
            case _ =>
              Logger.warn("Problem retrieving Application Details. Redirecting to Internal Server Error")
              InternalServerError("No Application Details found")
          }
        }
      }
    }
  }
}
