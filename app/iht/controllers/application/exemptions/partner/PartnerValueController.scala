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
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions._
import iht.views.html.application.exemption.partner.partner_value

/**
  * Created by jennygj on 03/08/16.
  */
object PartnerValueController extends PartnerValueController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait PartnerValueController extends EstateController {
  val submitUrl = iht.controllers.application.exemptions.partner.routes.PartnerOverviewController.onPageLoad()

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnPageLoad[PartnerExemption](
        partnerValueForm, partner_value.apply, _.allExemptions.flatMap(_.partner))
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], PartnerExemption) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, partnerExemption) => {
          val existingPartnerExemptions = appDetails.allExemptions.flatMap(_.partner).getOrElse(
            new PartnerExemption(None, None, None, None, None, None, None))
          val updatedAD = appDetails.copy(allExemptions = Some(appDetails.allExemptions.fold(new AllExemptions(partner = Some(partnerExemption)))(
            _.copy(partner = Some(existingPartnerExemptions.copy(totalAssets = partnerExemption.totalAssets)))
          )))
          (updatedAD, None)
        }
      estateElementOnSubmit[PartnerExemption](
        partnerValueForm,
        partner_value.apply,
        updateApplicationDetails,
        submitUrl,
        Some(createValidationFunction("totalAssets", _.isDefined))
      )
    }
  }
}
