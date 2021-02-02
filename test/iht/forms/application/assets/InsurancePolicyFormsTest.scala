/*
 * Copyright 2021 HM Revenue & Customs
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

package iht.forms.application.assets

import iht.forms.ApplicationForms._
import iht.models.application.assets.InsurancePolicy

class InsurancePolicyFormsTest extends BasicEstateElementFormBehaviour {
  "insurancePolicyDeceasedOwnQuestionForm" must {
    behave like yesNoQuestion[InsurancePolicy]("policyInDeceasedName", insurancePolicyDeceasedOwnQuestionForm,
      _.policyInDeceasedName, "error.assets.insurancePolicy.deceasedOwned.select")
    behave like currencyValue[InsurancePolicy]("value", insurancePolicyDeceasedOwnQuestionForm)
  }

  "insurancePolicyJointQuestionForm" must {
    behave like yesNoQuestion[InsurancePolicy]("isJointlyOwned", insurancePolicyJointQuestionForm,
      _.isJointlyOwned, "error.assets.insurancePolicy.jointlyOwned.select")
    behave like currencyValue[InsurancePolicy]("shareValue", insurancePolicyJointQuestionForm)
  }

  "insurancePolicyPayingOtherForm" must {
    behave like yesNoQuestion[InsurancePolicy]("isInsurancePremiumsPayedForSomeoneElse", insurancePolicyPayingOtherForm,
      _.isInsurancePremiumsPayedForSomeoneElse, "error.assets.insurancePolicy.payedToSomeoneElse.select")
  }

  "insurancePolicyMoreThanMaxForm" must {
    behave like yesNoQuestion[InsurancePolicy]("moreThanMaxValue", insurancePolicyMoreThanMaxForm,
      _.moreThanMaxValue, "error.assets.insurancePolicy.moreThanMaxValue.select")
  }

  "insurancePolicyAnnuityForm" must {
    behave like yesNoQuestion[InsurancePolicy]("isAnnuitiesBought", insurancePolicyAnnuityForm,
      _.isAnnuitiesBought, "error.assets.insurancePolicy.isAnnuitiesBought.select")
  }

  "insurancePolicyInTrustForm" must {
    behave like yesNoQuestion[InsurancePolicy]("isInTrust", insurancePolicyInTrustForm,
      _.isInTrust, "error.assets.insurancePolicy.isInTrust.select")
  }
}
