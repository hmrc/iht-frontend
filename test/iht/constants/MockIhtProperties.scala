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

import org.joda.time.LocalDate

/**
  * This is a mock file for the IhtProperties file. It is necessary to use this rather than the real file
  * when accessing one of the values from within a scalatest "must" clause, since in this situation the
  * Play Application object has not yet been initialized. There is no problem in other situations, e.g.
  * inside a scalatest "in" clause.
  */

object MockIhtProperties extends IhtProperties {
  override lazy val validCountryCodes: Array[String] = IhtProperties.validCountryCodes
  override lazy val ukIsoCountryCode: String = IhtProperties.ukIsoCountryCode
  override lazy val dateFormatForDisplay: String = "d MMMM yyyy"
  override lazy val maxCoExecutors: Int = IhtProperties.maxCoExecutors
  override lazy val maxNameLength: Int = IhtProperties.maxNameLength
  override lazy val hyphenateNamesLength: Int = IhtProperties.hyphenateNamesLength
  override lazy val validationMaxLengthAddresslines: Int = IhtProperties.validationMaxLengthAddresslines
  override lazy val validationMaxLengthPostcode: Int = IhtProperties.validationMaxLengthPostcode
  override lazy val validationMaxLengthFirstName: Int = IhtProperties.validationMaxLengthFirstName
  override lazy val validationMaxLengthLastName: Int = IhtProperties.validationMaxLengthLastName
  override lazy val validationMaxLengthNINO: Int = IhtProperties.validationMaxLengthNINO
  override lazy val validationMinLengthNINO: Int = IhtProperties.validationMinLengthNINO
  override lazy val validationMaxLengthPhoneNo: Int = IhtProperties.validationMaxLengthPhoneNo
  override lazy val validationMaxCharityNumberLength: Int = IhtProperties.validationMaxCharityNumberLength
  override lazy val validationMinCharityNumberLength: Int = IhtProperties.validationMinCharityNumberLength
  override lazy val validationTrustMaxValue: BigDecimal = IhtProperties.validationTrustMaxValue
  override lazy val validationForeignAssetMaxValue: BigDecimal = IhtProperties.validationForeignAssetMaxValue
  override lazy val validationTotalAssetMaxValue: BigDecimal = IhtProperties.validationTotalAssetMaxValue
  override lazy val tnrbThresholdLimit: BigDecimal = IhtProperties.tnrbThresholdLimit
  override lazy val transferredNilRateBand: BigDecimal = IhtProperties.transferredNilRateBand
  override lazy val exemptionsThresholdValue: BigDecimal = IhtProperties.exemptionsThresholdValue
  override lazy val giftsMaxValue: BigDecimal = IhtProperties.giftsMaxValue
  override lazy val taxThreshold: BigDecimal = IhtProperties.taxThreshold
  override lazy val grossEstateLimit: BigDecimal = IhtProperties.grossEstateLimit
  override lazy val applicantCountryEnglandOrWales: String = IhtProperties.applicantCountryEnglandOrWales
  override lazy val applicantCountryScotland: String = IhtProperties.applicantCountryScotland
  override lazy val applicantCountryNorthernIreland: String = IhtProperties.applicantCountryNorthernIreland
  override lazy val applicantCountryOther: String = IhtProperties.applicantCountryOther
  override lazy val roleLeadExecutor: String = IhtProperties.roleLeadExecutor
  override lazy val roleExecutor: String = IhtProperties.roleExecutor
  override lazy val roleAdministrator: String = IhtProperties.roleAdministrator
  override lazy val domicileEnglandOrWales: String = IhtProperties.domicileEnglandOrWales
  override lazy val domicileScotland: String = IhtProperties.domicileScotland
  override lazy val domicileNorthernIreland: String = IhtProperties.domicileNorthernIreland
  override lazy val domicileOther: String = IhtProperties.domicileOther
  override lazy val statusMarried: String = IhtProperties.statusMarried
  override lazy val statusSingle: String = IhtProperties.statusSingle
  override lazy val statusDivorced: String = IhtProperties.statusDivorced
  override lazy val statusWidowed: String = IhtProperties.statusWidowed
  override lazy val linkGovUkIht: String = IhtProperties.linkGovUkIht
  override lazy val linkRegistrationKickOut: String = IhtProperties.linkRegistrationKickOut
  override lazy val linkEstateReportKickOut: String = IhtProperties.linkEstateReportKickOut
  override lazy val linkGovUk: String = IhtProperties.linkGovUk
  override lazy val linkIHT401: String = IhtProperties.linkIHT401
  override lazy val linkExitToGovUKIHTForms: String = IhtProperties.linkExitToGovUKIHTForms
  override lazy val linkScottishCourtAndTribunal: String = IhtProperties.linkScottishCourtAndTribunal
  override lazy val linkIHT205: String = IhtProperties.linkIHT205
  override lazy val linkContactHMRC: String = IhtProperties.linkContactHMRC
  override lazy val charityLink: String = IhtProperties.charityLink
  override lazy val correctiveAccountsLink: String = IhtProperties.correctiveAccountsLink
  override lazy val giftsStartDay: Int = IhtProperties.giftsStartDay
  override lazy val giftsStartMonth: Int = IhtProperties.giftsStartMonth
  override lazy val giftsYears: Int = 7
  override lazy val ownershipDeceasedOnly: String = IhtProperties.ownershipDeceasedOnly
  override lazy val ownershipJoint: String = IhtProperties.ownershipJoint
  override lazy val ownershipInCommon: String = IhtProperties.ownershipInCommon
  override lazy val tenureFreehold: String = IhtProperties.tenureFreehold
  override lazy val tenureLeasehold: String = IhtProperties.tenureLeasehold
  override lazy val propertyTypeDeceasedHome: String = IhtProperties.propertyTypeDeceasedHome
  override lazy val propertyTypeOtherResidentialBuilding: String = IhtProperties.propertyTypeOtherResidentialBuilding
  override lazy val propertyTypeNonResidential: String = IhtProperties.propertyTypeNonResidential
  override lazy val questionnaireEasyToUseVeryEasy: String = IhtProperties.questionnaireEasyToUseVeryEasy
  override lazy val questionnaireEasyToUseEasy: String = IhtProperties.questionnaireEasyToUseEasy
  override lazy val questionnaireEasyToUseNeither: String = IhtProperties.questionnaireEasyToUseNeither
  override lazy val questionnaireEasyToUseDifficult: String = IhtProperties.questionnaireEasyToUseDifficult
  override lazy val questionnaireEasyToUseVeryDifficult: String = IhtProperties.questionnaireEasyToUseVeryDifficult
  override lazy val questionnaireFeelingAboutExperienceVerySatisfied: String = IhtProperties.questionnaireFeelingAboutExperienceVerySatisfied
  override lazy val questionnaireFeelingAboutExperienceSatisfied: String = IhtProperties.questionnaireFeelingAboutExperienceSatisfied
  override lazy val questionnaireFeelingAboutExperienceNeither: String = IhtProperties.questionnaireFeelingAboutExperienceNeither
  override lazy val questionnaireFeelingAboutExperienceDissatisfied: String = IhtProperties.questionnaireFeelingAboutExperienceDissatisfied
  override lazy val questionnaireFeelingAboutExperienceVeryDissatisfied: String = IhtProperties.questionnaireFeelingAboutExperienceVeryDissatisfied
  override lazy val giftsInYearMaxExemptionsValue: Int = IhtProperties.giftsInYearMaxExemptionsValue
  override lazy val maximumAdditionalCoExecutors: Int = IhtProperties.maximumAdditionalCoExecutors
  override lazy val dateOfDeathMinValidationDate: LocalDate = IhtProperties.dateOfDeathMinValidationDate
  override lazy val dateOfDeathMaxValidationDate: LocalDate = IhtProperties.dateOfDeathMaxValidationDate
  override lazy val dateOfPredeceasedForTnrbEligibility: LocalDate = IhtProperties.dateOfPredeceasedForTnrbEligibility
  override lazy val dateOfCivilPartnershipInclusion: LocalDate = IhtProperties.dateOfCivilPartnershipInclusion
  override lazy val ETMPAssetCodeMoney: String = IhtProperties.ETMPAssetCodeMoney
  override lazy val ETMPAssetCodeHouseHold: String = IhtProperties.ETMPAssetCodeHouseHold
  override lazy val ETMPAssetCodePrivatePension: String = IhtProperties.ETMPAssetCodePrivatePension
  override lazy val ETMPAssetCodeStockShareNotListed: String = IhtProperties.ETMPAssetCodeStockShareNotListed
  override lazy val ETMPAssetCodeStockShareListed: String = IhtProperties.ETMPAssetCodeStockShareListed
  override lazy val ETMPAssetCodeInsurancePolicy: String = IhtProperties.ETMPAssetCodeInsurancePolicy
  override lazy val ETMPAssetCodeBusinessInterest: String = IhtProperties.ETMPAssetCodeBusinessInterest
  override lazy val ETMPAssetCodeNominatedAsset: String = IhtProperties.ETMPAssetCodeNominatedAsset
  override lazy val ETMPAssetCodeForeignAsset: String = IhtProperties.ETMPAssetCodeForeignAsset
  override lazy val ETMPAssetCodeMoneyOwed: String = IhtProperties.ETMPAssetCodeMoneyOwed
  override lazy val ETMPAssetCodeOtherAsset: String = IhtProperties.ETMPAssetCodeOtherAsset
  override lazy val ETMPAssetCodeTrust: String = IhtProperties.ETMPAssetCodeTrust
  override lazy val ETMPAssetCodeGift: String = IhtProperties.ETMPAssetCodeGift
  override lazy val ETMPExemptionTypeGNCP: String = IhtProperties.ETMPExemptionTypeGNCP
  override lazy val DateRangeMonths: Integer = IhtProperties.DateRangeMonths
  override lazy val pdfStaticHeaders: Seq[(String, String)] = IhtProperties.pdfStaticHeaders
}
