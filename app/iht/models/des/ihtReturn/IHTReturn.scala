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

package iht.models.des.ihtReturn

import iht.constants.{Constants, IhtProperties}
import iht.models.Joda._
import play.api.libs.json.Json

// Can reuse the address object from Event Registration

case class IHTReturn(acknowledgmentReference: Option[String] = None,
                     submitter: Option[Submitter] = None,
                     deceased: Option[Deceased] = None,
                     freeEstate: Option[FreeEstate] = None,
                     gifts: Option[Set[Seq[Gift]]] = None,
                     trusts: Option[Set[Trust]] = None,
                     declaration: Option[Declaration] = None) {

  def totalAssetsValue =
    freeEstate.flatMap(_.estateAssets).fold(BigDecimal(0))(_.foldLeft(BigDecimal(0))(
      (a, b) => a + b.assetTotalValue.fold(BigDecimal(0))(identity)))

  def totalDebtsValue = {
    val debtsValueWithoutMortgage = freeEstate.flatMap(_.estateLiabilities).fold(BigDecimal(0))(_.foldLeft(BigDecimal(0))(
      (a, b) => a + b.liabilityAmount.fold(BigDecimal(0))(identity)))

    val mortgageValue: BigDecimal = freeEstate.flatMap(_.estateAssets).fold(BigDecimal(0)) {
      assets => {

        val setOfMatchedAssets: Set[Asset] = assets.filter(Constants.PropertyAssetCodes contains _.assetCode.getOrElse(""))

        val matchedLiabilities: Set[Liability] = setOfMatchedAssets.flatMap(_.liabilities).flatten

        val liabilitiesWithMortgage: Set[Liability] = matchedLiabilities.filter(
          _.liabilityType.getOrElse("") == Constants.MortgageLiabilityType)
        liabilitiesWithMortgage.map(_.liabilityAmount.getOrElse(BigDecimal(0))).sum

      }
    }

    debtsValueWithoutMortgage + mortgageValue
  }

  def totalForAssetIDs(assetIDs: Set[String]) = {
    val filteredOptionSetAsset = freeEstate.flatMap(_.estateAssets).map(_.filter(_.assetCode.fold(false)(id => assetIDs.contains(id))))
    filteredOptionSetAsset.fold(BigDecimal(0))(_.map(_.assetTotalValue.fold(BigDecimal(0))(identity)).sum)
  }

  def exemptionTotalsByExemptionType: Map[String, BigDecimal] = {
    val optionTotalledExemptions: Option[Map[Option[String], BigDecimal]] = freeEstate.flatMap(_.estateExemptions).map(_.groupBy(_.exemptionType))
      .map(_.map(item => item._1 -> item._2.map(_.overrideValue.fold(BigDecimal(0))(identity)).sum))
    optionTotalledExemptions.fold[Map[Option[String], BigDecimal]](Map.empty)(identity).map(x => x._1.fold("")(identity) -> x._2)
  }

  def totalExemptionsValue =
    freeEstate.flatMap(_.estateExemptions).fold(BigDecimal(0))(_.foldLeft(BigDecimal(0))(
      (a, b) => a + b.overrideValue.fold(BigDecimal(0))(identity)))

  def totalGiftsValue = gifts.fold[Set[Gift]](Set())(_.flatten)
    .foldLeft(BigDecimal(0))((a, b) => a + b.assetTotalValue.fold(BigDecimal(0))(identity))

  def totalTrustsValue = {
    trusts.fold(BigDecimal(0)) { (setOfTrust: Set[Trust]) =>
      val setOfAsset: Set[Asset] = setOfTrust.flatMap(_.trustAssets.fold[Set[Asset]](Set())(identity))
      setOfAsset.foldLeft(BigDecimal(0))((a, b) => a + b.assetTotalValue.fold(BigDecimal(0))(identity))
    }
  }

  def giftsTotalExclExemptions = {
    gifts.fold[Set[Gift]](Set())(_.flatten)
      .foldLeft(BigDecimal(0))((a, b) => a + b.valuePrevOwned.fold(BigDecimal(0))(identity))
  }

  def giftsExemptionsTotal = {
    gifts.fold[Set[Gift]](Set())(_.flatten)
      .foldLeft(BigDecimal(0))((a, b) => a + b.assetDescription.fold(BigDecimal(0))(x => {
        if (x.length > 0) {
          val str: Array[String] = x.split("£")
          str.length match {
            case x if (x > 1) => BigDecimal(str(1))
            case _ => BigDecimal(0)
          }
        } else {
          BigDecimal(0)
        }
      }))
  }

  def totalNetValue:BigDecimal = {
    if(totalExemptionsValue > 0) {
      (totalAssetsValue + totalTrustsValue + totalGiftsValue) - totalExemptionsValue - totalDebtsValue
    } else {
      totalAssetsValue + totalTrustsValue + totalGiftsValue
    }
  }

  def currentThreshold: BigDecimal = {
      if (isTnrbApplicable) IhtProperties.transferredNilRateBand else IhtProperties.exemptionsThresholdValue
  }

  def isTnrbApplicable: Boolean = deceased.fold(false) {
    x => x.transferOfNilRateBand.fold(false)(_ => true)
  }
}

object IHTReturn {
  implicit val formats = Json.format[IHTReturn]

  def sortByGiftDate(ihtReturn: IHTReturn) = {
    val optionSetSetGifts: Option[Set[Seq[Gift]]] = ihtReturn.gifts.map(_.map(seqGifts => seqGifts.sorted))
    ihtReturn copy (gifts = optionSetSetGifts)
  }
}
