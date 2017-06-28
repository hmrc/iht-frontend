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

import iht.constants.IhtProperties
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_joint
import play.api.i18n.MessagesApi

@Singleton
class InsurancePolicyDetailsJointController @Inject()(
                                                       val messagesApi: MessagesApi,
                                                       val ihtProperties: IhtProperties,
                                                       val applicationForms: ApplicationForms) extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsInsurancePoliciesJointlyOwned)
  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnPageLoad[InsurancePolicy](applicationForms.insurancePolicyJointQuestionForm, insurance_policy_details_joint.apply,
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
      estateElementOnSubmit[InsurancePolicy](
        applicationForms.insurancePolicyJointQuestionForm,
        insurance_policy_details_joint.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(insurancePoliciesRedirectLocation, Some(ihtProperties.InsuranceJointlyHeldYesNoID))
      )
    }
  }
}
