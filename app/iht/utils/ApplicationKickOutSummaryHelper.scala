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

import iht.utils.KickOutReason._
import iht.utils.{ApplicationStatus => AppStatus, KickOutReason => KickOut}
import play.api.i18n.Messages

import scala.collection.immutable.ListMap

object ApplicationKickOutSummaryHelper {

  /**
    * Summary paragraph displayed at the top of the page.
    */
  lazy val summary = ListMap(
    /* Assets */
    TrustsMoreThanOne -> "page.iht.application.assets.kickout.trustsMoreThanOne.summary",
    ForeignAssetsValueMoreThanMax -> "page.iht.application.assets.kickout.foreignAssetsValueMoreThanMax.summary",
    TrustValueMoreThanMax -> "page.iht.application.assets.kickout.trustValueMoreThanMax.summary",
    AnnuitiesOnInsurance -> "page.iht.application.assets.kickout.annuitiesOnInsurance.summary",
    PensionDisposedLastTwoYears -> "page.iht.application.assets.kickout.pensionDisposedLastTwoYears.summary",
    PensionsValueMoreThanMax -> "iht.estateReport.assets.kickout.MoreThan1Million",
    InTrustLessThanSevenYears -> "page.iht.application.assets.kickout.trustlessthansevenyears.summary",
    SingleSectionMoreThanMax -> "iht.estateReport.assets.kickout.MoreThan1Million",
    AssetsTotalValueMoreThanMax -> "iht.estateReport.assets.kickout.MoreThan1Million",
    InsuranceMoreThanMax -> "page.iht.application.assets.kickout.insuranceMoreThanMax.summary",
    AssetsMoneyOwed -> "iht.estateReport.assets.kickout.MoreThan1Million",
    AssetsDeceasedMoneyOwed -> "iht.estateReport.assets.kickout.MoreThan1Million",
    AssetsMoneyJointlyOwed -> "iht.estateReport.assets.kickout.MoreThan1Million",
    AssetsHouseholdDeceasedOwed -> "iht.estateReport.assets.kickout.MoreThan1Million",
    AssetsHouseholdJointlyOwed -> "iht.estateReport.assets.kickout.MoreThan1Million",
    AssetsVehiclesDeceasedOwned -> "iht.estateReport.assets.kickout.MoreThan1Million",
    AssetsVehiclesJointlyOwned -> "iht.estateReport.assets.kickout.MoreThan1Million",

    /* Gifts */
    GiftsWithReservationOfBenefit -> "page.iht.application.gifts.kickout.withReservationOfBenefit.summary",
    GiftsGivenInPast -> "page.iht.application.gifts.kickout.givenInPast.summary",
    GiftsToTrust -> "page.iht.application.gifts.kickout.toTrust.summary",
    GiftsMaxValue -> "page.iht.application.gifts.kickout.maxValue.summary",

    /* Exemptions */
    PartnerHomeInUK -> "page.iht.application.exemptions.kickout.homeNotInUK.summary",

    /* Pre-pre TNRB Eligibility */
    TnrbEstateMoreThanThreshold -> "page.iht.application.tnrb.kickout.estateMoreThanThreshold.summary",

    /* Pre-TNRB Eligibility */
    WidowedCheckNotWidowed -> "page.iht.application.tnrb.kickout.estateMoreThanThreshold.summary",
    PartnerDiedBeforeMinDate -> "page.iht.application.tnrb.kickout.estateMoreThanThreshold.summary",

    /* Pre-TNRB Eligibility Opc */
    PartnerDiedBeforeMinDateOpc -> "page.iht.application.tnrb.kickout.estateMoreThanThreshold.summary",

    /* TNRB Eligibility */
    PartnerNotLivingInUk -> "page.iht.application.tnrb.kickout.partnerNotLivingInUk.summary",
    GiftMadeBeforeDeath -> "page.iht.application.tnrb.kickout.giftMadeBeforeDeath.summary",
    StateClaimAnyBusiness -> "page.iht.application.tnrb.kickout.stateClaimAnyBusiness.summary",
    PartnerGiftWithResToOther -> "page.iht.application.tnrb.kickout.partnerGiftWithResToOther.summary",
    PartnerBenFromTrust -> "page.iht.application.tnrb.kickout.partnerBenFromTrust.summary",
    EstateBelowIhtThresholdApplied -> "page.iht.application.tnrb.kickout.estateBelowIhtThresholdApplied.summary",
    JointAssetPassed -> "page.iht.application.tnrb.kickout.jointAssetPassed.summary",

    /* Back-end kickouts */
    ExemptionEstateValueIsMoreThanMaximum -> "page.iht.application.exemptions.kickout.estateMoreThanThreshold.summary",
    EstateValueIsMoreThanMaximumLimit -> "page.iht.application.tnrb.kickout.estateValueNotInLimit.summary",
    AssetsTotalValueMoreThanThresholdAfterExemption -> "page.iht.application.tnrb.kickout.estateMoreThanThreshold.summary",

    /* These ones could possibly be removed as they don't seem to be called. */
    TnrbEstateValueIsMoreThanMaximum -> "page.iht.application.tnrb.kickout.estateValueNotInLimit.summary",
    PremiumsPaidForOtherPersonsPolicy -> "page.iht.application.assets.kickout.premiumsPaidForOtherPersonsPolicy.summary",
    NonJavaScript -> "page.iht.application.tnrb.kickout.kickOutNonJavaScript.summary",
    IHTHome -> "page.iht.application.home.kickout.summary"
  )

  /**
    * Bullet points to be displayed below the summary.
    */
  def summaryBullets(kickoutReason: String)(implicit messages:Messages) =
    kickoutReason match {
      case KickOut.GiftsGivenInPast =>
        Seq(
          messages("iht.estateReport.assets.money.lowerCaseInitial"),
          messages("page.iht.application.gifts.kickout.givenInPast.summary.bullet3"),
          messages("page.iht.application.gifts.kickout.givenInPast.summary.bullet5"),
          messages("iht.estateReport.gifts.stocksAndSharesListed"),
            messages("page.iht.application.gifts.kickout.givenInPast.summary.bullet4")
        )
      case _ => Nil
    }

}
