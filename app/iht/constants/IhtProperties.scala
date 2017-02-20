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
object IhtProperties {
  lazy val validCountryCodes: Array[String] = getPropertyAsStringArray("validCountryCodes")
  lazy val ukIsoCountryCode: String = getProperty("ukIsoCountryCode")
  lazy val dateFormatForDisplay: String = getProperty("dateFormatForDisplay")
  lazy val maxCoExecutors: Int = getPropertyAsInt("maxCoExecutors")
  lazy val maxNameLength: Int = getPropertyAsInt("maxNameLength")
  lazy val hyphenateNamesLength: Int = getPropertyAsInt("hyphenateNamesLength")
  lazy val validationMaxLengthAddresslines: Int = getPropertyAsInt("validationMaxLengthAddresslines")
  lazy val validationMaxLengthPostcode: Int = getPropertyAsInt("validationMaxLengthPostcode")
  lazy val validationMaxLengthFirstName: Int = getPropertyAsInt("validationMaxLengthFirstName")
  lazy val validationMaxLengthLastName: Int = getPropertyAsInt("validationMaxLengthLastName")
  lazy val validationMaxLengthNINO: Int = getPropertyAsInt("validationMaxLengthNINO")
  lazy val validationMinLengthNINO: Int = getPropertyAsInt("validationMinLengthNINO")
  lazy val validationMaxLengthPhoneNo: Int = getPropertyAsInt("validationMaxLengthPhoneNo")
  lazy val validationMaxCharityNumberLength: Int = getPropertyAsInt("validationMaxCharityNumberLength")
  lazy val validationMinCharityNumberLength: Int = getPropertyAsInt("validationMinCharityNumberLength")
  lazy val validationTrustMaxValue: BigDecimal = getPropertyAsBigDecimal("validationTrustMaxValue")
  lazy val validationForeignAssetMaxValue: BigDecimal = getPropertyAsBigDecimal("validationForeignAssetMaxValue")
  lazy val validationTotalAssetMaxValue: BigDecimal = getPropertyAsBigDecimal("validationTotalAssetMaxValue")
  lazy val tnrbThresholdLimit: BigDecimal = getPropertyAsBigDecimal("tnrbThresholdLimit")
  lazy val transferredNilRateBand: BigDecimal = getPropertyAsBigDecimal("transferredNilRateBand")
  lazy val exemptionsThresholdValue: BigDecimal = getPropertyAsBigDecimal("exemptionsThresholdValue")
  lazy val giftsMaxValue: BigDecimal = getPropertyAsBigDecimal("giftsMaxValue")
  lazy val taxThreshold: BigDecimal = getPropertyAsBigDecimal("taxThreshold")
  lazy val grossEstateLimit: BigDecimal = getPropertyAsBigDecimal("grossEstateLimit")

  lazy val applicantCountryEnglandOrWales: String = getProperty("applicantCountryEnglandOrWales")
  lazy val applicantCountryScotland: String = getProperty("applicantCountryScotland")
  lazy val applicantCountryNorthernIreland: String = getProperty("applicantCountryNorthernIreland")
  lazy val applicantCountryOther: String = getProperty("applicantCountryOther")
  lazy val roleLeadExecutor: String = getProperty("roleLeadExecutor")
  lazy val roleExecutor: String = getProperty("roleExecutor")
  lazy val roleAdministrator: String = getProperty("roleAdministrator")
  lazy val domicileEnglandOrWales: String = getProperty("domicileEnglandOrWales")
  lazy val domicileScotland: String = getProperty("domicileScotland")
  lazy val domicileNorthernIreland: String = getProperty("domicileNorthernIreland")
  lazy val domicileOther: String = getProperty("domicileOther")
  lazy val statusMarried: String = getProperty("statusMarried")
  lazy val statusSingle: String = getProperty("statusSingle")
  lazy val statusDivorced: String = getProperty("statusDivorced")
  lazy val statusWidowed: String = getProperty("statusWidowed")
  lazy val linkGovUkIht: String = getProperty("linkGovUkIht")
  lazy val linkRegistrationKickOut: String = getProperty("linkRegistrationKickOut")
  lazy val linkEstateReportKickOut: String = getProperty("linkEstateReportKickOut")
  lazy val linkGovUk: String = getProperty("linkGovUk")
  lazy val linkIHT401: String = getProperty("linkIHT401")
  lazy val linkExitToGovUKIHTForms: String = getProperty("linkExitToGovUKIHTForms")
  lazy val linkScottishCourtAndTribunal: String = getProperty("linkScottishCourtAndTribunal")
  lazy val linkIHT205: String = getProperty("linkIHT205")
  lazy val linkContactHMRC: String = getProperty("linkContactHMRC")
  lazy val charityLink: String = getProperty("charityLink")
  lazy val correctiveAccountsLink: String = getProperty("correctiveAccountLink")
  lazy val giftsStartDay: Int = getPropertyAsInt("giftsStartDay")
  lazy val giftsStartMonth: Int = getPropertyAsInt("giftsStartMonth")
  lazy val giftsYears: Int = getPropertyAsInt("giftsYears")
  lazy val ownershipDeceasedOnly: String = getProperty("ownershipDeceasedOnly")
  lazy val ownershipJoint: String = getProperty("ownershipJoint")
  lazy val ownershipInCommon: String = getProperty("ownershipInCommon")
  lazy val tenureFreehold: String = getProperty("tenureFreehold")
  lazy val tenureLeasehold: String = getProperty("tenureLeasehold")
  lazy val propertyTypeDeceasedHome: String = getProperty("propertyTypeDeceasedHome")
  lazy val propertyTypeOtherResidentialBuilding: String = getProperty("propertyTypeOtherResidentialBuilding")
  lazy val propertyTypeNonResidential: String = getProperty("propertyTypeNonResidential")
  lazy val questionnaireEasyToUseVeryEasy: String = getProperty("questionnaireEasyToUseVeryEasy")
  lazy val questionnaireEasyToUseEasy: String = getProperty("questionnaireEasyToUseEasy")
  lazy val questionnaireEasyToUseNeither: String = getProperty("questionnaireEasyToUseNeither")
  lazy val questionnaireEasyToUseDifficult: String = getProperty("questionnaireEasyToUseDifficult")
  lazy val questionnaireEasyToUseVeryDifficult: String = getProperty("questionnaireEasyToUseVeryDifficult")
  lazy val questionnaireFeelingAboutExperienceVerySatisfied: String = getProperty("questionnaireFeelAboutYourExperienceVerySatisfied")
  lazy val questionnaireFeelingAboutExperienceSatisfied: String = getProperty("questionnaireFeelAboutYourExperienceSatisfied")
  lazy val questionnaireFeelingAboutExperienceNeither: String = getProperty("questionnaireFeelAboutYourExperienceNeither")
  lazy val questionnaireFeelingAboutExperienceDissatisfied: String = getProperty("questionnaireFeelAboutYourExperienceDissatisfied")
  lazy val questionnaireFeelingAboutExperienceVeryDissatisfied: String = getProperty("questionnaireFeelAboutYourExperienceVeryDissatisfied")
  lazy val giftsInYearMaxExemptionsValue: Int = getPropertyAsInt("giftsInYearMaxExemptionsValue")
  lazy val maximumAdditionalCoExecutors: Int = getPropertyAsInt("maximumAdditionalCoExecutors")
  lazy val dateOfDeathMinValidationDate: LocalDate = getPropertyAsDate("dateOfDeathMinValidationDate")
  lazy val dateOfDeathMaxValidationDate: LocalDate = getPropertyAsDate("dateOfDeathMaxValidationDate")
  lazy val dateOfPredeceasedForTnrbEligibility: LocalDate = getPropertyAsDate("dateOfPredeceasedForTnrbEligibility")
  lazy val dateOfCivilPartnershipInclusion: LocalDate = getPropertyAsDate("dateOfCivilPartnershipInclusion")

  lazy val ETMPAssetCodeMoney:String = getProperty("etmpAssetCodeMoney")
  lazy val ETMPAssetCodeHouseHold:String = getProperty("etmpAssetCodeHouseHold")
  lazy val ETMPAssetCodePrivatePension:String = getProperty("etmpAssetCodePrivatePension")
  lazy val ETMPAssetCodeStockShareNotListed:String = getProperty("etmpAssetCodeStockShareNotListed")
  lazy val ETMPAssetCodeStockShareListed:String = getProperty("etmpAssetCodeStockShareListed")
  lazy val ETMPAssetCodeInsurancePolicy:String = getProperty("etmpAssetCodeInsurancePolicy")
  lazy val ETMPAssetCodeBusinessInterest:String = getProperty("etmpAssetCodeBusinessInterest")
  lazy val ETMPAssetCodeNominatedAsset:String = getProperty("etmpAssetCodeNominatedAsset")
  lazy val ETMPAssetCodeForeignAsset:String = getProperty("etmpAssetCodeForeignAsset")
  lazy val ETMPAssetCodeMoneyOwed:String = getProperty("etmpAssetCodeMoneyOwed")
  lazy val ETMPAssetCodeOtherAsset:String = getProperty("etmpAssetCodeOtherAsset")
  lazy val ETMPAssetCodeTrust:String = getProperty("etmpAssetCodeTrust")
  lazy val ETMPAssetCodeGift:String = getProperty("etmpAssetCodeGift")

  lazy val ETMPExemptionTypeGNCP: String = getProperty("etmpExemptionTypeGNCP")

  lazy val DateRangeMonths: Integer = getPropertyAsInt("dateRangeMonths")

  lazy val pdfStaticHeaders: Seq[(String, String)] = {
    val headers = getPropertyAsSeqStringTuples("pdfStaticHeaders")
    Logger.debug("PDF static headers read in from property file:" + headers)
    headers
  }
}