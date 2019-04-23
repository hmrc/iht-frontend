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

package iht.utils

import iht.config.AppConfig
import iht.connector.CachingConnector
import iht.constants.Constants
import iht.models.application.ApplicationDetails
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

trait ExemptionsGuidanceHelper {
  implicit val appConfig: AppConfig

  def guidanceRedirect(finalDestination: Call, applicationDetails: ApplicationDetails, connector: CachingConnector)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Call]] = {

    def isEstateOverThreshold(ad: ApplicationDetails): Boolean = ad.netValueAfterExemptionAndDebtsForPositiveExemption > ad.currentThreshold

    connector.getSingleValue(Constants.ExemptionsGuidanceContinueUrlKey).flatMap { continueUrl: Option[String] =>
      val shouldShowGuidance = isEstateOverThreshold(applicationDetails) && !applicationDetails.hasSeenExemptionGuidance.getOrElse(false) &&
        continueUrl.isEmpty
      if (shouldShowGuidance) {
        connector.storeSingleValue(Constants.ExemptionsGuidanceContinueUrlKey, finalDestination.url).map { _ =>
          Some(iht.controllers.application.exemptions.routes.ExemptionsGuidanceIncreasingThresholdController
            .onPageLoad(applicationDetails.ihtRef.getOrElse("")))
        }
      } else {
        connector.cacheDelete(Constants.ExemptionsGuidanceContinueUrlKey).map { _ => None }
      }
    }
  }

  def finalDestination(ihtReference: String, connector: CachingConnector)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Call] = {
    connector.getSingleValue(Constants.ExemptionsGuidanceContinueUrlKey).flatMap {
      case None =>
        val newCall = iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtReference)
        connector.storeSingleValue(Constants.ExemptionsGuidanceContinueUrlKey, newCall.url).map(_ => newCall)
      case Some(urlString) => Future.successful(Call(Constants.GET, urlString))
    }
  }
}
