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

package iht.models.application.assets

import iht.models.application.basicElements.ShareableEstateElement
import play.api.libs.json.Json


case class InsurancePolicy(isAnnuitiesBought: Option[Boolean],
                           isInsurancePremiumsPayedForSomeoneElse: Option[Boolean],
                           value: Option[BigDecimal],
                           shareValue: Option[BigDecimal],
                           policyInDeceasedName:Option[Boolean],
                           isJointlyOwned:Option[Boolean],
                           isInTrust:Option[Boolean],
                           coveredByExemption: Option[Boolean],
                           sevenYearsBefore: Option[Boolean],
                           moreThanMaxValue: Option[Boolean]
                          ) extends ShareableEstateElement {

  def isPremiumsBoughtForSomeoneElseComplete: Boolean = {
    if(isInsurancePremiumsPayedForSomeoneElse.isEmpty) {
      false
    } else if(isInsurancePremiumsPayedForSomeoneElse.get) {
      moreThanMaxValue.isDefined && isAnnuitiesBought.isDefined && isInTrust.isDefined
    } else {
      true
    }
  }

  def isComplete: Option[Boolean] =
    (policyInDeceasedName, value, isJointlyOwned, shareValue) match {
      case (None, None, None, None) => None
      case (None, _, _, _) => Some(false)
      case (_, _, None, _) => Some(false)
      case (Some(true), None, _, _) => Some(false)
      case (_, _, Some(true), None) => Some(false)
      case _ => Some(isPremiumsBoughtForSomeoneElseComplete)
    }
}

object InsurancePolicy {
  implicit val formats = Json.format[InsurancePolicy]
}
