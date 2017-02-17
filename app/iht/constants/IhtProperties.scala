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

import iht.config.IhtPropertiesReader._
import org.joda.time.LocalDate
import play.api.Logger

/**
  * Created by yasar on 25/11/15.
  */
trait IhtProperties {
  val validCountryCodes: Array[String]
  val ukIsoCountryCode: String
  val dateFormatForDisplay: String
  val maxCoExecutors: Int
  val maxNameLength: Int
  val hyphenateNamesLength: Int
  val validationMaxLengthAddresslines: Int
  val validationMaxLengthPostcode: Int
  val validationMaxLengthFirstName: Int
  val validationMaxLengthLastName: Int
  val validationMaxLengthNINO: Int
  val validationMinLengthNINO: Int
  val validationMaxLengthPhoneNo: Int
  val validationMaxCharityNumberLength: Int
  val validationMinCharityNumberLength: Int
  val validationTrustMaxValue: BigDecimal
  val validationForeignAssetMaxValue: BigDecimal
  val validationTotalAssetMaxValue: BigDecimal
  val tnrbThresholdLimit: BigDecimal
  val transferredNilRateBand: BigDecimal
  val exemptionsThresholdValue: BigDecimal
  val giftsMaxValue: BigDecimal
  val taxThreshold: BigDecimal
  val grossEstateLimit: BigDecimal
  val applicantCountryEnglandOrWales: String
  val applicantCountryScotland: String
  val applicantCountryNorthernIreland: String
  val applicantCountryOther: String
  val roleLeadExecutor: String
  val roleExecutor: String
  val roleAdministrator: String
  val domicileEnglandOrWales: String
  val domicileScotland: String
  val domicileNorthernIreland: String
  val domicileOther: String
  val statusMarried: String
  val statusSingle: String
  val statusDivorced: String
  val statusWidowed: String
  val linkGovUkIht: String
  val linkRegistrationKickOut: String
  val linkEstateReportKickOut: String
  val linkGovUk: String
  val linkIHT401: String
  val linkExitToGovUKIHTForms: String
  val linkScottishCourtAndTribunal: String
  val linkIHT205: String
  val linkContactHMRC: String
  val charityLink: String
  val correctiveAccountsLink: String
  val giftsStartDay: Int
  val giftsStartMonth: Int
  val giftsYears: Int
  val ownershipDeceasedOnly: String
  val ownershipJoint: String
  val ownershipInCommon: String
  val tenureFreehold: String
  val tenureLeasehold: String
  val propertyTypeDeceasedHome: String
  val propertyTypeOtherResidentialBuilding: String
  val propertyTypeNonResidential: String
  val questionnaireEasyToUseVeryEasy: String
  val questionnaireEasyToUseEasy: String
  val questionnaireEasyToUseNeither: String
  val questionnaireEasyToUseDifficult: String
  val questionnaireEasyToUseVeryDifficult: String
  val questionnaireFeelingAboutExperienceVerySatisfied: String
  val questionnaireFeelingAboutExperienceSatisfied: String
  val questionnaireFeelingAboutExperienceNeither: String
  val questionnaireFeelingAboutExperienceDissatisfied: String
  val questionnaireFeelingAboutExperienceVeryDissatisfied: String
  val giftsInYearMaxExemptionsValue: Int
  val maximumAdditionalCoExecutors: Int
  val dateOfDeathMinValidationDate: LocalDate
  val dateOfDeathMaxValidationDate: LocalDate
  val dateOfPredeceasedForTnrbEligibility: LocalDate
  val dateOfCivilPartnershipInclusion: LocalDate
  val ETMPAssetCodeMoney: String
  val ETMPAssetCodeHouseHold: String
  val ETMPAssetCodePrivatePension: String
  val ETMPAssetCodeStockShareNotListed: String
  val ETMPAssetCodeStockShareListed: String
  val ETMPAssetCodeInsurancePolicy: String
  val ETMPAssetCodeBusinessInterest: String
  val ETMPAssetCodeNominatedAsset: String
  val ETMPAssetCodeForeignAsset: String
  val ETMPAssetCodeMoneyOwed: String
  val ETMPAssetCodeOtherAsset: String
  val ETMPAssetCodeTrust: String
  val ETMPAssetCodeGift: String
  val ETMPExemptionTypeGNCP: String
  val DateRangeMonths: Integer
  val pdfStaticHeaders: Seq[(String, String)]
}

object IhtProperties extends IhtProperties {
  override lazy val validCountryCodes: Array[String] = getPropertyAsStringArray("validCountryCodes")
  override lazy val ukIsoCountryCode: String = getProperty("ukIsoCountryCode")
  override lazy val dateFormatForDisplay: String = getProperty("dateFormatForDisplay")
  override lazy val maxCoExecutors: Int = getPropertyAsInt("maxCoExecutors")
  override lazy val maxNameLength: Int = getPropertyAsInt("maxNameLength")
  override lazy val hyphenateNamesLength: Int = getPropertyAsInt("hyphenateNamesLength")
  override lazy val validationMaxLengthAddresslines: Int = getPropertyAsInt("validationMaxLengthAddresslines")
  override lazy val validationMaxLengthPostcode: Int = getPropertyAsInt("validationMaxLengthPostcode")
  override lazy val validationMaxLengthFirstName: Int = getPropertyAsInt("validationMaxLengthFirstName")
  override lazy val validationMaxLengthLastName: Int = getPropertyAsInt("validationMaxLengthLastName")
  override lazy val validationMaxLengthNINO: Int = getPropertyAsInt("validationMaxLengthNINO")
  override lazy val validationMinLengthNINO: Int = getPropertyAsInt("validationMinLengthNINO")
  override lazy val validationMaxLengthPhoneNo: Int = getPropertyAsInt("validationMaxLengthPhoneNo")
  override lazy val validationMaxCharityNumberLength: Int = getPropertyAsInt("validationMaxCharityNumberLength")
  override lazy val validationMinCharityNumberLength: Int = getPropertyAsInt("validationMinCharityNumberLength")
  override lazy val validationTrustMaxValue: BigDecimal = getPropertyAsBigDecimal("validationTrustMaxValue")
  override lazy val validationForeignAssetMaxValue: BigDecimal = getPropertyAsBigDecimal("validationForeignAssetMaxValue")
  override lazy val validationTotalAssetMaxValue: BigDecimal = getPropertyAsBigDecimal("validationTotalAssetMaxValue")
  override lazy val tnrbThresholdLimit: BigDecimal = getPropertyAsBigDecimal("tnrbThresholdLimit")
  override lazy val transferredNilRateBand: BigDecimal = getPropertyAsBigDecimal("transferredNilRateBand")
  override lazy val exemptionsThresholdValue: BigDecimal = getPropertyAsBigDecimal("exemptionsThresholdValue")
  override lazy val giftsMaxValue: BigDecimal = getPropertyAsBigDecimal("giftsMaxValue")
  override lazy val taxThreshold: BigDecimal = getPropertyAsBigDecimal("taxThreshold")
  override lazy val grossEstateLimit: BigDecimal = getPropertyAsBigDecimal("grossEstateLimit")
  override lazy val applicantCountryEnglandOrWales: String = getProperty("applicantCountryEnglandOrWales")
  override lazy val applicantCountryScotland: String = getProperty("applicantCountryScotland")
  override lazy val applicantCountryNorthernIreland: String = getProperty("applicantCountryNorthernIreland")
  override lazy val applicantCountryOther: String = getProperty("applicantCountryOther")
  override lazy val roleLeadExecutor: String = getProperty("roleLeadExecutor")
  override lazy val roleExecutor: String = getProperty("roleExecutor")
  override lazy val roleAdministrator: String = getProperty("roleAdministrator")
  override lazy val domicileEnglandOrWales: String = getProperty("domicileEnglandOrWales")
  override lazy val domicileScotland: String = getProperty("domicileScotland")
  override lazy val domicileNorthernIreland: String = getProperty("domicileNorthernIreland")
  override lazy val domicileOther: String = getProperty("domicileOther")
  override lazy val statusMarried: String = getProperty("statusMarried")
  override lazy val statusSingle: String = getProperty("statusSingle")
  override lazy val statusDivorced: String = getProperty("statusDivorced")
  override lazy val statusWidowed: String = getProperty("statusWidowed")
  override lazy val linkGovUkIht: String = getProperty("linkGovUkIht")
  override lazy val linkRegistrationKickOut: String = getProperty("linkRegistrationKickOut")
  override lazy val linkEstateReportKickOut: String = getProperty("linkEstateReportKickOut")
  override lazy val linkGovUk: String = getProperty("linkGovUk")
  override lazy val linkIHT401: String = getProperty("linkIHT401")
  override lazy val linkExitToGovUKIHTForms: String = getProperty("linkExitToGovUKIHTForms")
  override lazy val linkScottishCourtAndTribunal: String = getProperty("linkScottishCourtAndTribunal")
  override lazy val linkIHT205: String = getProperty("linkIHT205")
  override lazy val linkContactHMRC: String = getProperty("linkContactHMRC")
  override lazy val charityLink: String = getProperty("charityLink")
  override lazy val correctiveAccountsLink: String = getProperty("correctiveAccountLink")
  override lazy val giftsStartDay: Int = getPropertyAsInt("giftsStartDay")
  override lazy val giftsStartMonth: Int = getPropertyAsInt("giftsStartMonth")
  override lazy val giftsYears: Int = getPropertyAsInt("giftsYears")
  override lazy val ownershipDeceasedOnly: String = getProperty("ownershipDeceasedOnly")
  override lazy val ownershipJoint: String = getProperty("ownershipJoint")
  override lazy val ownershipInCommon: String = getProperty("ownershipInCommon")
  override lazy val tenureFreehold: String = getProperty("tenureFreehold")
  override lazy val tenureLeasehold: String = getProperty("tenureLeasehold")
  override lazy val propertyTypeDeceasedHome: String = getProperty("propertyTypeDeceasedHome")
  override lazy val propertyTypeOtherResidentialBuilding: String = getProperty("propertyTypeOtherResidentialBuilding")
  override lazy val propertyTypeNonResidential: String = getProperty("propertyTypeNonResidential")
  override lazy val questionnaireEasyToUseVeryEasy: String = getProperty("questionnaireEasyToUseVeryEasy")
  override lazy val questionnaireEasyToUseEasy: String = getProperty("questionnaireEasyToUseEasy")
  override lazy val questionnaireEasyToUseNeither: String = getProperty("questionnaireEasyToUseNeither")
  override lazy val questionnaireEasyToUseDifficult: String = getProperty("questionnaireEasyToUseDifficult")
  override lazy val questionnaireEasyToUseVeryDifficult: String = getProperty("questionnaireEasyToUseVeryDifficult")
  override lazy val questionnaireFeelingAboutExperienceVerySatisfied: String = getProperty("questionnaireFeelAboutYourExperienceVerySatisfied")
  override lazy val questionnaireFeelingAboutExperienceSatisfied: String = getProperty("questionnaireFeelAboutYourExperienceSatisfied")
  override lazy val questionnaireFeelingAboutExperienceNeither: String = getProperty("questionnaireFeelAboutYourExperienceNeither")
  override lazy val questionnaireFeelingAboutExperienceDissatisfied: String = getProperty("questionnaireFeelAboutYourExperienceDissatisfied")
  override lazy val questionnaireFeelingAboutExperienceVeryDissatisfied: String = getProperty("questionnaireFeelAboutYourExperienceVeryDissatisfied")
  override lazy val giftsInYearMaxExemptionsValue: Int = getPropertyAsInt("giftsInYearMaxExemptionsValue")
  override lazy val maximumAdditionalCoExecutors: Int = getPropertyAsInt("maximumAdditionalCoExecutors")
  override lazy val dateOfDeathMinValidationDate: LocalDate = getPropertyAsDate("dateOfDeathMinValidationDate")
  override lazy val dateOfDeathMaxValidationDate: LocalDate = getPropertyAsDate("dateOfDeathMaxValidationDate")
  override lazy val dateOfPredeceasedForTnrbEligibility: LocalDate = getPropertyAsDate("dateOfPredeceasedForTnrbEligibility")
  override lazy val dateOfCivilPartnershipInclusion: LocalDate = getPropertyAsDate("dateOfCivilPartnershipInclusion")
  override lazy val ETMPAssetCodeMoney: String = getProperty("etmpAssetCodeMoney")
  override lazy val ETMPAssetCodeHouseHold: String = getProperty("etmpAssetCodeHouseHold")
  override lazy val ETMPAssetCodePrivatePension: String = getProperty("etmpAssetCodePrivatePension")
  override lazy val ETMPAssetCodeStockShareNotListed: String = getProperty("etmpAssetCodeStockShareNotListed")
  override lazy val ETMPAssetCodeStockShareListed: String = getProperty("etmpAssetCodeStockShareListed")
  override lazy val ETMPAssetCodeInsurancePolicy: String = getProperty("etmpAssetCodeInsurancePolicy")
  override lazy val ETMPAssetCodeBusinessInterest: String = getProperty("etmpAssetCodeBusinessInterest")
  override lazy val ETMPAssetCodeNominatedAsset: String = getProperty("etmpAssetCodeNominatedAsset")
  override lazy val ETMPAssetCodeForeignAsset: String = getProperty("etmpAssetCodeForeignAsset")
  override lazy val ETMPAssetCodeMoneyOwed: String = getProperty("etmpAssetCodeMoneyOwed")
  override lazy val ETMPAssetCodeOtherAsset: String = getProperty("etmpAssetCodeOtherAsset")
  override lazy val ETMPAssetCodeTrust: String = getProperty("etmpAssetCodeTrust")
  override lazy val ETMPAssetCodeGift: String = getProperty("etmpAssetCodeGift")
  override lazy val ETMPExemptionTypeGNCP: String = getProperty("etmpExemptionTypeGNCP")
  override lazy val DateRangeMonths: Integer = getPropertyAsInt("dateRangeMonths")
  override lazy val pdfStaticHeaders: Seq[(String, String)] = {
    val headers = getPropertyAsSeqStringTuples("pdfStaticHeaders")
    Logger.debug("PDF static headers read in from property file:" + headers)
    headers
  }
}
