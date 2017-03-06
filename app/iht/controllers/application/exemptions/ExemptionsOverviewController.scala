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

package iht.controllers.application.exemptions

import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.IhtProperties
import iht.connector.IhtConnectors
import iht.controllers.application.ApplicationController
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions._
import iht.models.RegistrationDetails
import iht.utils.CommonHelper
import play.api.Logger
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

/**
 *
 * Created by Vineet Tyagi on 07/12/15.
 *
 */

object ExemptionsOverviewController extends ExemptionsOverviewController with IhtConnectors

trait ExemptionsOverviewController extends ApplicationController{

  def cachingConnector: CachingConnector
  def ihtConnector: IhtConnector


  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      withApplicationDetails { rd => ad =>
        val allExemptions = ad.allExemptions.fold(new AllExemptions())(exemptions => exemptions)
        val response = Ok(iht.views.html.application.exemption.exemptions_overview(
          ad,
          allExemptions,
          ad.charities,
          ad.qualifyingBodies,
          isEligible(rd),
          rd))
        if (!ad.hasSeenExemptionGuidance.getOrElse(false)) {
          val changedAppDetails = ad copy (hasSeenExemptionGuidance = Some(true))
          ihtConnector.saveApplication(CommonHelper.getNino(user), changedAppDetails, rd.acknowledgmentReference).map(_=>response)
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
    registrationDetails.deceasedDetails.map(_.maritalStatus.contains(IhtProperties.statusMarried)).fold(false)(identity)
}
