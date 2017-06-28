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

package iht.constants

import java.net.URL
import java.nio.file.{Path, Paths}
import javax.inject.{Inject, Singleton}

import iht.utils.CommonHelper
import play.api.Environment

import scala.collection.immutable.ListMap

/**
  * Created by dbeer on 04/08/15.
  */
@Singleton
class Constants @Inject() (
                            val env: Environment,
                            val ihtProperties:IhtProperties
                          ) {

  val insurancePolicyFormFieldsWithExtraContentLineInErrorSummary = Set(
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

  val PDFHMRCGuidance: URL = CommonHelper.getOrException(env.resource("pdf/151001 Notes to help you fill in IHT online - final - for DDCN.pdf"))
  val pDFHMRCGuidance: Path = Paths.get(PDFHMRCGuidance.toURI)

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

  lazy val estimate = "estimate"
  lazy val under325000 = "under-325000"
  lazy val between325000and1million = "between-325000-and-1million"
  lazy val moreThan1million = "more-than-1million"

  lazy val ETMPAssetCodesToIHTMessageKeys = ListMap(
    ihtProperties.ETMPAssetCodeMoney -> "iht.estateReport.assets.money.upperCaseInitial",
    ihtProperties.ETMPAssetCodeHouseHold -> "iht.estateReport.assets.householdAndPersonalItems.title",
    ihtProperties.ETMPAssetCodePrivatePension -> "iht.estateReport.assets.privatePensions",
    ihtProperties.ETMPAssetCodeStockShareNotListed -> "iht.estateReport.assets.stocksAndSharesNotListed",
    ihtProperties.ETMPAssetCodeStockShareListed -> "iht.estateReport.assets.stocksAndSharesListed",
    ihtProperties.ETMPAssetCodeInsurancePolicy -> "iht.estateReport.assets.insurancePolicies",
    ihtProperties.ETMPAssetCodeBusinessInterest -> "iht.estateReport.assets.businessInterests.title",
    ihtProperties.ETMPAssetCodeNominatedAsset -> "iht.estateReport.assets.nominated",
    ihtProperties.ETMPAssetCodeForeignAsset -> "iht.estateReport.assets.foreign.title",
    ihtProperties.ETMPAssetCodeMoneyOwed -> "iht.estateReport.assets.moneyOwed",
    ihtProperties.ETMPAssetCodeOtherAsset -> "page.iht.application.assets.main-section.other.title",
    ihtProperties.ETMPAssetCodeTrust -> "iht.estateReport.assets.heldInATrust.title",
    ihtProperties.ETMPAssetCodeDeceasedsHome -> "page.iht.application.assets.propertyType.deceasedHome.label"
  )

  lazy val ETMPExemptionTypesToIHTMessageKeys = ListMap(
    ihtProperties.ETMPExemptionTypeCharity -> "pdf.exemption.charity",
    ihtProperties.ETMPExemptionTypeSpouse -> "pdf.exemption.spouse",
    ihtProperties.ETMPExemptionTypeGNCP -> "pdf.exemption.otherQualifyingBodies"
  )

  lazy val  NINO= "customerNino"

  lazy val PropertyAssetCodes = Set("0016", "0017", "0018")
  lazy val  MortgageLiabilityType= "Mortgage"
}
