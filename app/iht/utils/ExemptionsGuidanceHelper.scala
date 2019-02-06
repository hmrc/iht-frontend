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

import iht.connector.CachingConnector
import iht.constants.Constants
import iht.models.application.ApplicationDetails
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

object ExemptionsGuidanceHelper {
  /**
    * The guidanceRedirect method is used with the original destination of the controller that calls it. It checks to
    * see whether or not the TNRB guidance page should be displayed first. If it does not need to be displayed it returns
    * None signalling that the calling controller should behave as usual. If guidance should be displayed, it returns the new
    * destination - the guidance page - for the calling controller to redirect to. The guidance page will use the
    * finalDestination Call to allow the user to go to the original destination of the calling controller once the
    * guidance page has been read.
    *
    * In pseudo code:
    *
    *  Calculate the threshold
    *  Check if guidance is appropriate (over the current threshold, and the guidance flag is not set, and the continue url is not set in the keystore.
    *    if we are going to show guidance:
    *      Update the key store with the continue url
    *      return the guidance redirect
    *      (the exemptions overview onload method that is reached from the guidance page must mark the application model with the seen guidance flag)
    *    else:
    *       Remove the continue url from the keystore if it's there
    *       return None
    */
  def guidanceRedirect(finalDestination: Call, applicationDetails: ApplicationDetails, connector: CachingConnector)(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Call]] = {

    def isEstateOverThreshold(ad:ApplicationDetails): Boolean = ad.netValueAfterExemptionAndDebtsForPositiveExemption > ad.currentThreshold

    connector.getSingleValue(Constants.ExemptionsGuidanceContinueUrlKey).flatMap{ (continueUrl: Option[String]) =>
      val shouldShowGuidance = isEstateOverThreshold(applicationDetails) && !applicationDetails.hasSeenExemptionGuidance.getOrElse(false) &&
        continueUrl.isEmpty
      if(shouldShowGuidance) {
        connector.storeSingleValue(Constants.ExemptionsGuidanceContinueUrlKey, finalDestination.url).map{ _=>
          Some(iht.controllers.application.exemptions.routes.ExemptionsGuidanceIncreasingThresholdController
            .onPageLoad(applicationDetails.ihtRef.getOrElse("")))
        }
      } else {
        connector.delete(Constants.ExemptionsGuidanceContinueUrlKey).map {_ => None}
      }
    }
  }

  /**
    * Retrieves from keystore the final destination that should have been stored by guidanceRedirect. If no final
    * destination is found in keystore then the estate overview is stored in keystore and returned.
    */
  def finalDestination(ihtReference: String, connector: CachingConnector)
            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Call] = {
    connector.getSingleValue(Constants.ExemptionsGuidanceContinueUrlKey).flatMap {
      case None =>
        val newCall = iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtReference)
        connector.storeSingleValue(Constants.ExemptionsGuidanceContinueUrlKey, newCall.url).map(_=>newCall)
      case Some(urlString) => Future.successful(Call(Constants.GET, urlString))
    }
  }
}
