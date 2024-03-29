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

package iht.controllers.application.exemptions

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationController
import iht.models.RegistrationDetails
import iht.models.application.exemptions._
import iht.utils.StringHelper
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.exemption.exemptions_overview

import scala.concurrent.Future

class ExemptionsOverviewControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                                 val ihtConnector: IhtConnector,
                                                 val authConnector: AuthConnector,
                                                 val exemptionsOverviewView: exemptions_overview,
                                                 implicit val appConfig: AppConfig,
                                                 val cc: MessagesControllerComponents) extends FrontendController(cc) with ExemptionsOverviewController

trait ExemptionsOverviewController extends ApplicationController with StringHelper {


  def cachingConnector: CachingConnector
  def ihtConnector: IhtConnector
  val exemptionsOverviewView: exemptions_overview

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withApplicationDetails(userNino) { rd => ad =>
        val allExemptions = ad.allExemptions.fold(new AllExemptions())(exemptions => exemptions)
        val response = Ok(exemptionsOverviewView(
          ad,
          allExemptions,
          ad.charities,
          ad.qualifyingBodies,
          isEligible(rd),
          rd))
        if (!ad.hasSeenExemptionGuidance.getOrElse(false)) {
          val changedAppDetails = ad copy (hasSeenExemptionGuidance = Some(true))
          ihtConnector.saveApplication(getNino(userNino), changedAppDetails, rd.acknowledgmentReference).map(_=>response)
        } else {
          Future.successful(response)
        }
      }
    }
  }

  /**
   * Checks the marital status
 *
   * @param registrationDetails
   * @return
   */
  private def isEligible(registrationDetails: RegistrationDetails): Boolean =
    registrationDetails.deceasedDetails.map(_.maritalStatus.contains(appConfig.statusMarried)).fold(false)(identity)
}
