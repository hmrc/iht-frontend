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

package iht.models.application.assets

import iht.models.application.basicElements.ShareableEstateElement
import play.api.libs.json.Json

/**
  * Created by vineet on 03/11/16.
  */
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

  def isPremiumsBoughtForSomeoneElseComplete: Boolean = (isInsurancePremiumsPayedForSomeoneElse,
  moreThanMaxValue, isAnnuitiesBought, isInTrust) match {
    case (Some(true), None, _, _) => false
    case (Some(true), _, None, _) => false
    case (Some(true), _, _, None) => false
    case (Some(false), _,_, _) => true
    case _ => true
  }

  def isComplete: Option[Boolean] =
    (policyInDeceasedName, value, isJointlyOwned, shareValue, isInsurancePremiumsPayedForSomeoneElse,
      moreThanMaxValue, isAnnuitiesBought, isInTrust) match {
      case (None, None, None, None, None, None, None, None) => None
      case (None, _, _, _, _, _, _, _) => Some(false)
      case (_, _, None, _, _, _, _, _) => Some(false)
      case (Some(true), None, _, _, _, _, _, _) => Some(false)
      case (_, _, Some(true), None, _, _, _, _) => Some(false)
      case (_, _, _, _, None, _, _, _) => Some(false)
      case _ => if(isPremiumsBoughtForSomeoneElseComplete) Some(true) else Some(false)
    }
}

object InsurancePolicy {
  implicit val formats = Json.format[InsurancePolicy]
}
