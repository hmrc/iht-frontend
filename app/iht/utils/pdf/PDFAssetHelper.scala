/*
 * Copyright 2022 HM Revenue & Customs
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

package iht.utils.pdf

import iht.models.des.ihtReturn.Asset

object PDFAssetHelper {
  val blankBigDecimalValue = None //Some(BigDecimal(0))
  val DefaultPostCode = "AA1 1AA"

  private def buildAssetMoney = {
    Asset(
      // General asset
      assetCode = Some("9001"),
      assetDescription = Some("Rolled up bank and building society accounts"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Standard"),
      devolutions = None,
      liabilities = None
    )
  }

  // Create Jointly owed money asset
  private def buildJointAssetMoney = {
    Asset(
      // General asset
      assetCode = Some("9001"),
      assetDescription = Some("Rolled up bank and building society accounts"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Joint - Beneficial Joint Tenants"),
      devolutions = None,
      liabilities = None
    )
  }

  // Household and personal goods plus motor vehicles, caravans and boats
  private def buildAssetHouseholdAndPersonalItems = {
    Asset(
      // General asset
      assetCode = Some("9004"),
      assetDescription = Some("Rolled up household and personal goods"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Standard"),
      devolutions = None,
      liabilities = None
    )
  }

  // Create joint household and personal items
  private def buildJointAssetHouseholdAndPersonalItems = {
    Asset(
      // General asset
      assetCode = Some("9004"),
      assetDescription = Some("Rolled up household and personal goods"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Joint - Beneficial Joint Tenants"),
      devolutions = None,
      liabilities = None
    )
  }

  private def buildAssetPrivatePensions = {
    Asset(
      // General asset
      assetCode = Some("9005"),
      assetDescription = Some("Rolled up pensions"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Standard"),
      devolutions = None,
      liabilities = None
    )
  }

  private def buildAssetStocksAndSharesNotListed = {
    Asset(
      // General asset
      assetCode = Some("9010"),
      assetDescription = Some("Rolled up unlisted stocks and shares"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Standard"),
      devolutions = None,
      liabilities = None
    )
  }

  private def buildAssetStocksAndSharesListed = {
    Asset(
      // General asset
      assetCode = Some("9008"),
      assetDescription = Some("Rolled up quoted stocks and shares"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Standard"),
      devolutions = None,
      liabilities = None
    )
  }

  private def buildAssetInsurancePoliciesOwned = {
    Asset(
      // General asset
      assetCode = Some("9006"),
      assetDescription = Some("Rolled up life assurance policies"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Standard"),
      devolutions = None,
      liabilities = None
    )
  }

  // Create jointly owed insurance policy
  private def buildJointAssetInsurancePoliciesOwned = {
    Asset(
      // General asset
      assetCode = Some("9006"),
      assetDescription = Some("Rolled up life assurance policies"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Joint - Beneficial Joint Tenants"),
      devolutions = None,
      liabilities = None
    )
  }

  private def buildAssetBusinessInterests = {
    Asset(
      // General asset
      assetCode = Some("9021"),
      assetDescription = Some("Rolled up business assets"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Standard"),
      devolutions = None,
      liabilities = None
    )
  }

  private def buildAssetNominatedAssets = {

    Asset(
      // General asset
      assetCode = Some("9099"),
      assetDescription = Some("Rolled up nominated assets"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Nominated"),
      devolutions = None,
      liabilities = None
    )
  }

  private def buildAssetForeignAssets = {
    Asset(
      // General asset
      assetCode = Some("9098"),
      assetDescription = Some("Rolled up foreign assets"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Foreign"),
      devolutions = None,
      liabilities = None
    )
  }

  private def buildAssetMoneyOwed = {
    Asset(
      // General asset
      assetCode = Some("9013"),
      assetDescription = Some("Rolled up money owed to deceased"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Standard"),
      devolutions = None,
      liabilities = None
    )
  }

  private def buildAssetOther = {
    Asset(
      // General asset
      assetCode = Some("9015"),
      assetDescription = Some("Rolled up other assets"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Standard"),
      devolutions = None,
      liabilities = None
    )
  }

  private def buildAssetsPropertiesDeceasedsHome = {
    val address = models.des.Address(addressLine1 = "addr1", addressLine2 = "addr2",
        addressLine3 = None, addressLine4 = None,
        postalCode = DefaultPostCode, countryCode = "GB")

    Asset(
      // General asset
      assetCode = Some("0016"),
      assetDescription = Some("Deceased's residence"),
      assetID = Some("null"),
      assetTotalValue = blankBigDecimalValue,
      howheld = Some("Standard"),
      liabilities = None,
      //      liabilities= None,

      // Property asset
      propertyAddress = Some(address),
      tenure = Some("Freehold"), tenancyType = Some("Vacant Possession"),
      yearsLeftOnLease = Some(0),
      yearsLeftOntenancyAgreement = Some(0)
    )
  }

  val blankSetAsset = Set(
    buildAssetMoney,
    buildJointAssetMoney,
    buildAssetHouseholdAndPersonalItems,
    buildJointAssetHouseholdAndPersonalItems,
    buildAssetStocksAndSharesListed,
    buildAssetStocksAndSharesNotListed,
    buildAssetPrivatePensions,
    buildAssetInsurancePoliciesOwned,
    buildJointAssetInsurancePoliciesOwned,
    buildAssetBusinessInterests,
    buildAssetNominatedAssets,
    buildAssetForeignAssets,
    buildAssetMoneyOwed,
    buildAssetOther,
    buildAssetsPropertiesDeceasedsHome
  )
}
