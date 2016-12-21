/*
 * Copyright 2016 HM Revenue & Customs
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

package iht.controllers.application.exemptions.partner

import iht.controllers.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions._
import iht.views.html.application.exemption.partner.partner_nino

object PartnerNinoController extends PartnerNinoController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait PartnerNinoController extends EstateController {
  val submitUrl = iht.controllers.application.exemptions.partner.routes.PartnerOverviewController.onPageLoad()

  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[PartnerExemption](partnerNinoForm, partner_nino.apply, _.allExemptions.flatMap(_.partner))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], PartnerExemption) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, partnerExemption) => {

          val existingIsAssetForDeceasedPartner = appDetails.allExemptions.flatMap(_.partner.flatMap(_.isAssetForDeceasedPartner))
          val existingIsPartnerHomeInUK = appDetails.allExemptions.flatMap(_.partner.flatMap(_.isPartnerHomeInUK))
          val existingFirstName = appDetails.allExemptions.flatMap(_.partner.flatMap(_.firstName))
          val existingLastName = appDetails.allExemptions.flatMap(_.partner.flatMap(_.lastName))
          val existingDateOfBirth = appDetails.allExemptions.flatMap(_.partner.flatMap(_.dateOfBirth))
          val existingTotalAssets = appDetails.allExemptions.flatMap(_.partner.flatMap(_.totalAssets))

          val updatedAD = appDetails.copy(allExemptions = Some(appDetails.allExemptions.fold
            (new AllExemptions(partner = Some(partnerExemption)))
          (partnerExemption.nino match {
            case Some(_) => _.copy( partner= Some(partnerExemption.copy(isAssetForDeceasedPartner = existingIsAssetForDeceasedPartner,
              isPartnerHomeInUK = existingIsPartnerHomeInUK,
              firstName = existingFirstName,
              lastName = existingLastName,
              dateOfBirth = existingDateOfBirth,
              totalAssets = existingTotalAssets,
              nino = partnerExemption.nino) ))

            case None => throw new RuntimeException("Partner Exemption does not exist")
          }
          )))
          (updatedAD, None)
        }

      estateElementOnSubmit[PartnerExemption](
        partnerNinoForm,
        partner_nino.apply,
        updateApplicationDetails,
        submitUrl,
        Some(createValidationFunction("nino", _.isDefined))
      )
    }
  }
}
