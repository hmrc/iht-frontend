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

package iht.testhelpers

import iht.models.application.basicElements.ShareableBasicEstateElement
import org.joda.time.LocalDate

/**
 *
 * Created by Vineet Tyagi on 20/09/15.
 *
 */

object TestHelper {
  val PreIHTKickOut = "preIHT"
  val PreService = "preService"

  val MaxCoExecutors = 3
  val SourceMultipleExecutor = "multipleExecutors"
  val SourceRegSummary = "registrationSummary"

  // Application Kickouts
  val KickOutAnnuitiesOnInsurance = "AnnuitiesOnInsurance"
  val KickOutTnrbClaimDateNotInLimit= "Claim date is not within limit"

  //IHT Home
  val AppStatusAwaitingReturn = "Awaiting Return"
  val AppStatusNotStarted = "Not Started"
  val AppStatusInProgress = "In Progress"
  val AppStatusInReview = "In Review"
  val AppStatusClosed = "Closed"
  val AppStatusKickOut = "Kick Out"
  val AppStatusClearanceGranted = "Clearance Granted"
  val AppStatusUnderEnquiry = "Under Enquiry"
  val AppStatusIneligibleApplication = "Ineligible Application"

  //fieldMapping keys
  val ApplicantCountryEnglandOrWales = "England or Wales"
  val ApplicantCountryScotland = "Scotland"
  val ApplicantCountryNorthernIreland = "Northern Ireland"

  val RoleLeadExecutor = "Lead Executor"
  val RoleExecutor = "Executor"
  val RoleAdministrator = "Administrator"
  val RoleAgent = "Agent"
  val RoleDonee = "Donee"
  val RoleTrustee = "Trustee"
  val RoleSettlor = "Settlor"

  val DomicileEnglandOrWales = "England or Wales"
  val domicileScotland = "Scotland"
  val domicileNI = "Northern Ireland"
  val domicileOther = "Other"

  val MaritalStatusMarried = "Married or in Civil Partnership"
  val MaritalStatusSingle = "Single"
  val MaritalStatusDivorced = "Divorced or Former Civil Partner"
  val MaritalStatusWidowed = "Widowed or a Surviving Civil Partner"

  val PropertyTypeDeceasedHome = Some("Deceased's home")
  val PropertyTypeOtherResidentialBuilding = Some("Other residential building")
  val TypesOfOwnershipDeceasedOnly = Some("Deceased only")
  val TypesOfOwnershipJoint = Some("Joint")

  val TenureFreehold = Some("Freehold")
  val TenureLeasehold = Some("Leasehold")
  val LinkRegistrationKickOut = "https://www.gov.uk/inheritance-tax"
  val LinkEstateReportKickOut = "https://www.gov.uk/government/publications/inheritance-tax-inheritance-tax-account-iht400"

   // Exemptions
  val ExemptionsGuidanceSeen = "ExemptionGuidanceSeen"
  val lastQuestionUrl = "lastQuestionUrl"

  //Gifts
  val GiftsGuidanceSeen = "GiftsGuidanceSeen"
  val GiftsMaxValue = 150000
  val KickOutGiftsWithReservationOfBenefit= "Deceased has given gifts with reservation of benefits"
  val KickOutGiftsGivenInPast= "Deceased has given gifts in the past"
  val KickOutGiftsToTrust= "Deceased has given gifts to a trust"
  val KickOutGiftsMaxValue= "Gifts value bigger than max"
  val KickOutAssetsTotalValueMoreThanMax = "AssetsTotalValueMoreThanMax"

  val ValidationTotalAssetMaxValue = 1000000

  val Yes = Some("Y")

  val dateOfPredeceasedForTnrbEligibility = new LocalDate(1974,11,13)

  lazy val civilPartnershipExclusionDate = new LocalDate(2005, 12, 5)
  lazy val civilPartnershipExclusionDateMinusOne = new LocalDate(2005, 12, 4)
  lazy val civilPartnershipExclusionDatePlusOne = new LocalDate(2005, 12, 6)

  lazy val spouseMessageKey = "page.iht.application.TnrbEligibilty.spouse.commonText"
  lazy val spouseOrCivilPartnerMessageKey = "page.iht.application.TnrbEligibilty.spouseOrCivilPartner.commonText"
  lazy val marriedMessageKey = "page.iht.application.tnrbEligibilty.partner.married.label"
  lazy val marriedOrInCivilPartnershipMessageKey = "page.iht.application.tnrbEligibilty.partner.marriedOrCivilPartnership.label"
  lazy val ihtPropertiesCorrectiveAccountsLink = "https://www.gov.uk/government/publications/inheritance-tax-corrective-account-c4"


lazy val AppSectionPropertiesID = "properties-buildings-and-land"
lazy val AppSectionMoneyID = "money"
lazy val AppSectionHouseholdID = "household-and-personal-items"
lazy val AppSectionVehiclesID = "motor-vehicles"
lazy val AppSectionPrivatePensionID = "private-pensions"
lazy val AppSectionStockAndShareID = "stocks-and-shares"
lazy val AppSectionInsurancePolicyID = "insurance-policies"
lazy val AppSectionBusinessInterestID = "business-interests"
lazy val AppSectionNominatedID = "nominated-assets"
lazy val AppSectionHeldInTrustID = "assets-held-in-a-trust"
lazy val AppSectionForeignID = "foreign-assets"
lazy val AppSectionMoneyOwedID = "money-owed"
lazy val AppSectionOtherID = "other-assets"
lazy val GiftsGivenAwayQuestionID = "gave-away-seven-years"
lazy val GiftsReservationBenefitSectionID = "with-reservation-of-benefit-section"
lazy val GiftsReservationBenefitQuestionID = "with-reservation-of-benefit"
lazy val GiftsSevenYearsSectionID = "seven-years-section"
lazy val GiftsSevenYearsQuestionID = "seven-years-anything-else"
lazy val GiftsSevenYearsQuestionID2 = "seven-years-property-not-to-person"
lazy val GiftsValueOfGiftsSectionID = "value-of-gifts"
lazy val GiftsValueOfGiftsQuestionID = "value-of-gifts"
lazy val GiftsValueDetailID = "value-of-gifts-for-period-"
lazy val DebtsMortgagesID = "mortgages"
lazy val DebtsFuneralExpensesID = "funeral-expenses"
lazy val DebtsOwedFromTrustID = "debts-owed-from-a-trust"
lazy val DebtsOwedOutsideUKID = "debts-owed-to-anyone-outside-uk"
lazy val DebtsOwedJointlyID = "debts-owed-on-jointly-owned-assets"
lazy val DebtsOtherID = "other-debts"
lazy val InsurancePayingToDeceasedSectionID = "paying-out-to-deceased"
lazy val InsurancePayingToDeceasedYesNoID = "paying-out-to-deceased"
lazy val InsurancePayingToDeceasedValueID = "paying-out-to-deceased-value"
lazy val InsuranceJointlyHeldSectionID = "jointly-held"
lazy val InsuranceJointlyHeldYesNoID = "jointly-held"
lazy val InsuranceJointlyHeldValueID = "jointly-held-value"
lazy val InsurancePaidForSomeoneElseSectionID = "paid-for-someone-else"
lazy val InsurancePaidForSomeoneElseYesNoID = "paid-for-someone-else"
lazy val InsurancePremiumnsYesNoID = "premiums-not-paying-out"
lazy val InsuranceAnnuityYesNoID = "annuity"
lazy val InsurancePlacedInTrustYesNoID = "placed-in-trust"
lazy val TnrbSpousePermanentHomeInUKID = "spouse-permanent-home-in-uk"
lazy val TnrbGiftsGivenAwayID = "gifts-given-away-before-death"
lazy val TnrbGiftsWithReservationID = "gifts-with-reservation-of-benefit"
lazy val TnrbEstateReliefID = "business-or-agricultural-relief"
lazy val TnrbSpouseBenefitFromTrustID = "spouse-benefit-from-trust"
lazy val TnrbEstatePassedToDeceasedID = "estate-passed-to-deceased"
lazy val TnrbJointAssetsPassedToDeceasedID = "joint-assets-passed-to-deceased"
lazy val TnrbSpouseMartialStatusID = "spouse-martial-status"
lazy val TnrbSpouseDateOfDeathID = "spouse-date-of-death"
lazy val TnrbSpouseNameID = "spouse-name"
lazy val TnrbSpouseDateOfMarriageID = "spouse-date-of-marriage"
lazy val AssetsPropertiesChangeID = "change-property-"
lazy val AssetsPropertiesDeleteID = "delete-property-"
lazy val AssetsPropertiesOwnedID = "property-owned"
lazy val AssetsPropertiesAddPropertyID = "add-property"
lazy val AssetsPropertiesPropertyAddressID = "property-address"
lazy val AssetsPropertiesPropertyKindID = "kind-of-property"
lazy val AssetsPropertiesPropertyOwnershipID = "type-of-ownership"
lazy val AssetsPropertiesTenureID = "freehold-leasehold"
lazy val AssetsPropertiesPropertyValueID = "property-value"
  
}

trait SharableOverviewData {

  val dataWithQuestionsAnsweredNo =
    Some(ShareableBasicEstateElement(value = None, shareValue = None, isOwned = Some(false), isOwnedShare = Some(false)))

  val dataWithQuestionsAnsweredYes =
    Some(ShareableBasicEstateElement(value = None, shareValue = None, isOwned = Some(true), isOwnedShare = Some(true)))

  val ownedAmount = 1234.0
  val ownedAmountDisplay = "£1,234.00"
  val jointAmount = 2345.0
  val jointAmountDisplay = "£2,345.00"

  val dataWithValues =
    Some(ShareableBasicEstateElement(value = Some(ownedAmount), shareValue = Some(jointAmount),
      isOwned = Some(true), isOwnedShare = Some(true)))
}
