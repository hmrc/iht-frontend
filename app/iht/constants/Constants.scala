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

package iht.constants

object Constants {

  val insurancePolicyFormFieldsWithExtraContentLineInErrorSummary: Set[String] = Set(
    "policyInDeceasedName", "isJointlyOwned", "isInsurancePremiumsPayedForSomeoneElse", "isAnnuitiesBought", "isInTrust"
  )

  val AppSectionProperties = "properties"
  val AppSectionMoney = "money"
  val AppSectionHousehold = "household"
  val AppSectionVehicles = "vehicles"
  val AppSectionPrivatePension = "privatePension"
  val AppSectionStockAndShare = "stockAndShare"
  val AppSectionInsurancePolicy = "insurancePolicy"
  val AppSectionBusinessInterest = "businessInterest"
  val AppSectionNominated = "nominated"
  val AppSectionHeldInTrust = "heldInTrust"
  val AppSectionForeign = "foreign"
  val AppSectionMoneyOwed = "moneyOwed"
  val AppSectionOther = "otherAsset"


  val AppSectionMortgages = "mortgages"
  val AppSectionFuneralExpenses = "funeralExpenses"
  val AppSectionDebtsOwedFromTrust = "debtsFromTrust"
  val AppSectionDebtsOwedToAnyoneOutsideUK = "debtsOutsideUk"
  val AppSectionDebtsOwedOnJointAssets = "jointlyOwned"
  val AppSectionDebtsOther = "otherDebt"

  val AppSectionExemptionsPartnerIsAssetForDeceasedPartner = "exemptionsPartnerIsAssetForDeceasedPartner"
  val AppSectionExemptionsPartnerIsPartnerHomeInUK = "exemptionsPartnerIsPartnerHomeInUK"
  val AppSectionExemptionsPartnerName = "exemptionsPartnerName"
  val AppSectionExemptionsPartnerDateOfBirth = "exemptionsPartnerDateOfBirth"
  val AppSectionExemptionsPartnerNino = "exemptionsPartnerNino"
  val AppSectionExemptionsPartnerTotalAssets = "exemptionsPartnerTotalAssets"

  val AppSectionExemptionsCharityValue = "exemptionsCharityValue"
  val AppSectionExemptionsQualifyingBodyValue = "exemptionsQualifyingBodyValue"
  val AppSectionEstateAssets = "estateAssets"
  val AppSectionEstateDebts = "estateDebts"
  val AppSectionEstateGifts = "estateGifts"

  val MaxIterationValueForGiftYears = 8

  val yesAnswer = "Yes"
  val noAnswer = "No"

  val AssetTypeSingle = "single"
  val AssetTypeJoint = "joint"

  //KEYSTORE KEYS
  val ExemptionsGuidanceContinueUrlKey = "ExemptionsGuidanceContinueUrl"
  val PDFIHTReference = "PDFIHTReference"

  //HTTP METHODS
  val GET = "GET"
  val POST = "POST"

  //Filter UI related constants
  lazy val filterChoices = "filter-choices"
  lazy val continueEstateReport = "continue"
  lazy val register = "register"
  lazy val alreadyStarted = "already-started"
  lazy val agent = "agent"

  lazy val domicile = "domicile"
  lazy val englandOrWales = "england-or-wales"
  lazy val scotland = "scotland"
  lazy val northernIreland = "northern-ireland"
  lazy val otherCountry = "other"

  lazy val filterJointlyOwned = "filter-jointly-owned"
  lazy val filterJointlyOwnedYes = "filter-jointly-owned-yes"
  lazy val filterJointlyOwnedNo = "filter-jointly-owned-no"

  lazy val estimate = "estimate"
  lazy val under325000 = "under-325000"
  lazy val between325000and1million = "between-325000-and-1million"
  lazy val moreThan1million = "more-than-1million"

  lazy val anyAssets = "any-assets"
  lazy val anyAssetsYes = "any-assets-yes"
  lazy val anyAssetsNo = "any-assets-no"

  lazy val NINO = "customerNino"

  lazy val PropertyAssetCodes = Set("0016", "0017", "0018")
  lazy val MortgageLiabilityType = "Mortgage"

  lazy val DisplayModeExemption = "exemption"
  lazy val DisplayModeNoExemption = "noExemption"

  /*
  Due to the welsh grammatical rule of "consonant soft mutation" the word
  "priod" ("marriage") changes to "briod" when preceded by the word "gan".
 */
  lazy val contentMutation: (String, String) = "gan priod" -> "gan briod"

  val welshVowels: Set[Char] = Set('a', 'e', 'i', 'o', 'u', 'w', 'y')
}
