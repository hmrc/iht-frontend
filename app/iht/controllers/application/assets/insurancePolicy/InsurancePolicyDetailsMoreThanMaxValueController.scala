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

import javax.inject.{Inject, Singleton}

import iht.controllers.application.EstateController
import iht.forms.ApplicationForms
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.ApplicationKickOutHelper
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_more_than_max_value
import play.api.i18n.MessagesApi

@Singleton
class InsurancePolicyDetailsMoreThanMaxValueController @Inject()(val messagesApi: MessagesApi) extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsInsurancePoliciesMoreThanMax)

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnPageLoad[InsurancePolicy](insurancePolicyMoreThanMaxForm, insurance_policy_details_more_than_max_value.apply,
        _.allAssets.flatMap(_.insurancePolicy))
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], InsurancePolicy) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, insurancePolicy) => {
          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
            (new AllAssets(action = None, insurancePolicy = Some(insurancePolicy))) (allAssets=>
            updateAllAssetsWithInsurancePolicy(allAssets, insurancePolicy, identity))
          ))
          (updatedAD, None)
        }

      estateElementOnSubmitConditionalRedirect[InsurancePolicy](insurancePolicyMoreThanMaxForm,
        insurance_policy_details_more_than_max_value.apply, updateApplicationDetails,
        (ad, _) =>  ad.allAssets.flatMap(allAssets=>allAssets.insurancePolicy).flatMap(_.moreThanMaxValue)
          .fold(insurancePoliciesRedirectLocation)(_=>
          iht.controllers.application.assets.insurancePolicy.routes.InsurancePolicyDetailsAnnuityController.onPageLoad()))
    }
  }
}
