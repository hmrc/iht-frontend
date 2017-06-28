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

package iht.controllers.application.status

import iht.constants.Constants
import iht.controllers.application.EstateController
import iht.models.application.{ApplicationDetails, ProbateDetails}
import iht.models.RegistrationDetails
import iht.utils.CommonHelper
import play.api.Logger
import play.api.mvc.Request
import play.twirl.api.HtmlFormat.Appendable
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.Future


trait ApplicationStatusController extends EstateController {

  def getView: (String, String, ProbateDetails) => Request[_] => Appendable

  def onPageLoad(ihtReference: String) = authorisedForIht {
    implicit user =>
      implicit request => {
        val nino = CommonHelper.getNino(user)
        cachingConnector.storeSingleValue(constants.PDFIHTReference, ihtReference).flatMap{ _ =>
          ihtConnector.getCaseDetails(nino, ihtReference).flatMap { caseDetails =>
            val futureAD = getApplicationDetails(ihtReference, caseDetails.acknowledgmentReference)
            val futureProbateDetails = futureAD.flatMap(ad =>
              getProbateDetails(nino, caseDetails, ad)
            )
            val deceasedDetails = CommonHelper.getOrException(caseDetails.deceasedDetails)
            futureProbateDetails.map { probateDetails =>
              Ok(getView(ihtReference, deceasedDetails.name, probateDetails)(request))
            }
          }
        }
      }
  }

  private def getProbateDetails(nino: String, registrationDetails: RegistrationDetails, applicationDetails: ApplicationDetails)
                               (implicit request: Request[_], user: AuthContext, hc: HeaderCarrier): Future[ProbateDetails] = {
    cachingConnector.getProbateDetails.flatMap {
      case Some(probateDetails) => Future.successful(probateDetails)
      case _ => {
        registrationDetails.ihtReference match {
          case Some(ihtReference) => ihtConnector.getProbateDetails(nino, ihtReference, registrationDetails.updatedReturnId).map {
            case Some(probateDetails) => probateDetails
            case _ => {
              Logger.warn("Registration not found")
              throw new RuntimeException("Probate Details not found")
            }
          }
          case _ => {
            Logger.warn("Registration not found")
            throw new RuntimeException("Required details not available")
          }
        }
      }
    }
  }
}
