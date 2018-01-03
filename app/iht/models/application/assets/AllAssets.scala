/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.models.application.basicElements.{BasicEstateElement, ShareableBasicEstateElement}
import play.api.libs.json.Json

/**
  * Created by vineet on 02/11/16.
  */
case class AllAssets(action: Option[String]=None,
                     money: Option[ShareableBasicEstateElement] = None,
                     household: Option[ShareableBasicEstateElement] = None,
                     vehicles: Option[ShareableBasicEstateElement] = None,
                     privatePension: Option[PrivatePension] = None,
                     stockAndShare: Option[StockAndShare] = None,
                     insurancePolicy: Option[InsurancePolicy] = None,
                     businessInterest: Option[BasicEstateElement] = None,
                     nominated: Option[BasicEstateElement] = None,
                     heldInTrust: Option[HeldInTrust] = None,
                     foreign: Option[BasicEstateElement] = None,
                     moneyOwed: Option[BasicEstateElement] = None,
                     other: Option[BasicEstateElement] = None,
                     properties: Option[Properties] = None) {

  def totalValueWithoutProperties: BigDecimal = {
    val amountList = money.flatMap(_.value) ::
      household.flatMap(_.value) ::
      vehicles.flatMap(_.value) ::
      privatePension.flatMap(_.value) ::
      stockAndShare.flatMap(_.valueListed) ::
      stockAndShare.flatMap(_.valueNotListed)::
      insurancePolicy.flatMap(_.value) ::
      businessInterest.flatMap(_.value) ::
      moneyOwed.flatMap(_.value) ::
      nominated.flatMap(_.value) ::
      heldInTrust.flatMap(_.value) ::
      foreign.flatMap(_.value) ::
      other.flatMap(_.value) ::
      money.flatMap(_.shareValue) :: household.flatMap(_.shareValue) ::
      vehicles.flatMap(_.shareValue) ::
      insurancePolicy.flatMap(_.shareValue) ::
      Nil

    amountList.flatten.foldLeft(BigDecimal(0))(_ + _)
  }

  def totalValueWithoutPropertiesOption: Option[BigDecimal] = {
    val amountList = money.flatMap(_.value) ::
      household.flatMap(_.value) ::
      vehicles.flatMap(_.value) ::
      privatePension.flatMap(_.value) ::
      stockAndShare.flatMap(_.valueListed) ::
      stockAndShare.flatMap(_.valueNotListed)::
      insurancePolicy.flatMap(_.value) ::
      businessInterest.flatMap(_.value) ::
      moneyOwed.flatMap(_.value) ::
      nominated.flatMap(_.value) ::
      heldInTrust.flatMap(_.value) ::
      foreign.flatMap(_.value) ::
      other.flatMap(_.value) ::
      money.flatMap(_.shareValue) :: household.flatMap(_.shareValue) ::
      vehicles.flatMap(_.shareValue) ::
      insurancePolicy.flatMap(_.shareValue) ::
      Nil
    if(amountList.flatten.isEmpty) {None}
    else {Some(amountList.flatten.foldLeft(BigDecimal(0))(_ + _))}
  }

  def areAllAssetsSectionsAnsweredNo: Boolean = {
    val assetsSectionsAnswers = Seq(money.flatMap(_.isOwned),
      money.flatMap(_.isOwnedShare),
      household.flatMap(_.isOwned),
      household.flatMap(_.isOwnedShare),
      vehicles.flatMap(_.isOwned),
      vehicles.flatMap(_.isOwnedShare),
      privatePension.flatMap(_.isOwned),
      stockAndShare.flatMap(_.isListed),
      stockAndShare.flatMap(_.isNotListed),
      insurancePolicy.flatMap(_.policyInDeceasedName),
      insurancePolicy.flatMap(_.isJointlyOwned),
      insurancePolicy.flatMap(_.isInsurancePremiumsPayedForSomeoneElse),
      businessInterest.flatMap(_.isOwned),
      moneyOwed.flatMap(_.isOwned),
      nominated.flatMap(_.isOwned),
      heldInTrust.flatMap(_.isOwned),
      foreign.flatMap(_.isOwned),
      other.flatMap(_.isOwned),
      properties.flatMap(_.isOwned)
    )
    assetsSectionsAnswers.forall(_.contains(false))
  }
}

object AllAssets {
  implicit val formats = Json.format[AllAssets]

}
