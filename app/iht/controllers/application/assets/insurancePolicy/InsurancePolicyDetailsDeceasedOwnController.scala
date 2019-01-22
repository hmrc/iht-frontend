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

package iht.controllers.application.assets.insurancePolicy

import iht.config.FrontendAuthConnector
import iht.connector.IhtConnectors
import iht.constants.IhtProperties._
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_deceased_own
import javax.inject.Inject
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}

class InsurancePolicyDetailsDeceasedOwnControllerImpl @Inject()() extends InsurancePolicyDetailsDeceasedOwnController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait InsurancePolicyDetailsDeceasedOwnController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsInsurancePoliciesOwnedByDeceased)


  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      estateElementOnPageLoad[InsurancePolicy](insurancePolicyDeceasedOwnQuestionForm, insurance_policy_details_deceased_own.apply,
        _.allAssets.flatMap(_.insurancePolicy), userNino)
    }
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], InsurancePolicy) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, insurancePolicy) => {
          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
            (new AllAssets(action = None, insurancePolicy = Some(insurancePolicy))) (allAssets=>
            updateAllAssetsWithInsurancePolicy(allAssets, insurancePolicy, identity))
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[InsurancePolicy](
        insurancePolicyDeceasedOwnQuestionForm,
        insurance_policy_details_deceased_own.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(insurancePoliciesRedirectLocation, Some(InsurancePayingToDeceasedYesNoID)),
        userNino
      )
    }
  }
}
