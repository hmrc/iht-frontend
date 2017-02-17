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

import iht.constants.{IhtProperties, MockIhtProperties}
import org.joda.time.LocalDate
import iht.models.application.basicElements.ShareableBasicEstateElement

/**
 *
 * Created by Vineet Tyagi on 20/09/15.
 *
 */

object TestHelper {

  def getDateBeforeSevenYears(ihtProperties: IhtProperties, date: LocalDate) = {
    date.minusYears(ihtProperties.giftsYears).plusDays(1)
  }

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
  val TypesOfOwnershipDeceasedOnly = Some("Deceased only")

  val TenureFreehold = Some("Freehold")
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
