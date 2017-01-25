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

package iht.controllers.application.assets.insurancePolicy

import iht.controllers.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_paying_other

object InsurancePolicyDetailsPayingOtherController extends InsurancePolicyDetailsPayingOtherController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait InsurancePolicyDetailsPayingOtherController extends EstateController {
  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[InsurancePolicy](insurancePolicyForm, insurance_policy_details_paying_other.apply,
        _.allAssets.flatMap(_.insurancePolicy))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {

      val blankUnapplicableQuestions: InsurancePolicy => InsurancePolicy = insurancePolicy => {
        if (!insurancePolicy.isInsurancePremiumsPayedForSomeoneElse.fold(true)(identity)) {
            insurancePolicy copy (
              isInTrust  = None,
              sevenYearsBefore = None,
              isAnnuitiesBought = None,
              moreThanMaxValue = None
            )
        } else {
          insurancePolicy
        }
      }

      val updateApplicationDetails: (ApplicationDetails, Option[String], InsurancePolicy) =>
        (ApplicationDetails, Option[String]) =
      (appDetails, _, insurancePolicy) => {
        val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, insurancePolicy = Some(insurancePolicy))) (allAssets=>
          updateAllAssetsWithInsurancePolicy(allAssets, insurancePolicy, blankUnapplicableQuestions))
        ))
        (updatedAD, None)
      }


      estateElementOnSubmitConditionalRedirect[InsurancePolicy](insurancePolicyPayingOther,
        insurance_policy_details_paying_other.apply, updateApplicationDetails,
        (ad, _) =>  ad.allAssets.flatMap(allAssets=>allAssets.insurancePolicy).flatMap(_.isInsurancePremiumsPayedForSomeoneElse)
          .fold(insurancePoliciesRedirectLocation)(isInsurancePremiumsPayedForSomeoneElse=>
            if(isInsurancePremiumsPayedForSomeoneElse) {
              iht.controllers.application.assets.insurancePolicy.routes.InsurancePolicyDetailsMoreThanMaxValueController.onPageLoad()
            } else {
              insurancePoliciesRedirectLocation
            }
          )
      )
    }
  }
}
