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

package iht.utils

import iht.utils.{KickOutReason => KickOut}

/**
 *
 * Created by Vineet Tyagi on 22/09/15.
 *
 * Contains the KickOut reasons
 */
object KickOutReason {

  // Application Kickouts
  val TrustValueMoreThanMax = "TrustValueMoreThanMax"
  val ForeignAssetsValueMoreThanMax = "ForeignAssetsValueMoreThanMax"
  val TrustsMoreThanOne = "TrustsMoreThanOne"
  val AnnuitiesOnInsurance = "AnnuitiesOnInsurance"
  val PremiumsPaidForOtherPersonsPolicy = "PremiumsPaidForOtherPersonsPolicy"
  val AssetsTotalValueMoreThanMax = "AssetsTotalValueMoreThanMax"
  val SingleSectionMoreThanMax = "AssetsSingleSectionMoreThanMax"
  val PensionDisposedLastTwoYears = "PensionDisposedLastTwoYears"

  val PensionsValueMoreThanMax = "PensionsValueMoreThanMax"

  val AssetsTotalValueMoreThanThresholdAfterExemption = "Estate value is more than threshold after exemption and no tnrb"
  val TnrbEstateMoreThanThreshold = "Estate value is more than Tnrb threshold"
  val TnrbEstateValueIsMoreThanMaximum = "Estate value is more than maximum allowed limit"
  val EstateValueIsMoreThanMaximumLimit = "Estate value is more than maximum allowed limit"
  val WidowedCheckNotWidowed = "Deceased was not widowed"
  val PartnerDiedBeforeMinDate = "Deceased partner date of death is before 13 November 1974"
  val PartnerDiedBeforeMinDateOpc = "Deceased partner date of death is before 13 November 1974_Opc"
  val ExemptionEstateValueIsMoreThanMaximum = "Estate value is more than maximum allowed limit"
  val GiftsWithReservationOfBenefit = "Deceased has given gifts with reservation of benefits"
  val GiftsGivenInPast = "Deceased has given gifts in the past"
  val GiftsToTrust = "Deceased has given gifts to a trust"
  val GiftsMaxValue = "Gifts value bigger than max"
  val PartnerNotLivingInUk = "Partner is not living in the UK"
  val GiftMadeBeforeDeath = "Gift has been made before death"
  val StateClaimAnyBusiness = "State claims some business"
  val PartnerGiftWithResToOther = "Partner has been made gift with other"
  val PartnerBenFromTrust = "Partner has been benifitted from trust"
  val EstateBelowIhtThresholdApplied = "Estate below threshold has been applied "
  val JointAssetPassed = "Joint assets has not been passed"
  val PartnerDiedAfterDeceased = "Partner died after Deceased"
  val PartnerDiedAfterDeceasedOpc = "Partner died after Deceased_Opc"
  val NonJavaScript = "KickOut for non java script users"
  val IHTHome = "KickOut from IHTHome"
  val InTrustLessThanSevenYears = "In trust for less than seven years"
  val PartnerHomeInUK = "Is partners permanent home in uk"
  val InsuranceMoreThanMax = "Insurance more than max"

  val AssetsMoneyOwed = "MoneyOwed"
  val AssetsDeceasedMoneyOwed = "MoneyDeceasedOwed"
  val AssetsMoneyJointlyOwed = "MoneyJointlyOwed"

  val AssetsHouseholdDeceasedOwed = "HouseholdDeceasedOwed"
  val AssetsHouseholdJointlyOwed = "HouseholdJointlyOwned"

  val AssetsVehiclesDeceasedOwned = "VehiclesDeceasedOwned"
  val AssetsVehiclesJointlyOwned = "VehiclesJointlyOwned"

  val inFlight = Seq(TrustsMoreThanOne,
    TrustValueMoreThanMax,
    ForeignAssetsValueMoreThanMax,
    AssetsTotalValueMoreThanMax,
    AnnuitiesOnInsurance,
    PremiumsPaidForOtherPersonsPolicy,
    PensionDisposedLastTwoYears,
    GiftsWithReservationOfBenefit,
    GiftsToTrust,
    GiftsGivenInPast,
    GiftsMaxValue,
    InTrustLessThanSevenYears
  )

  val onDoneKickouts = Seq(
    TnrbEstateMoreThanThreshold,
    PartnerDiedBeforeMinDate,
    WidowedCheckNotWidowed,
    PartnerDiedAfterDeceased,
    PartnerNotLivingInUk,
    GiftMadeBeforeDeath,
    StateClaimAnyBusiness,
    PartnerGiftWithResToOther,
    PartnerBenFromTrust,
    EstateBelowIhtThresholdApplied,
    JointAssetPassed,
    ExemptionEstateValueIsMoreThanMaximum,
    EstateValueIsMoreThanMaximumLimit,
    NonJavaScript,
    IHTHome)
}
