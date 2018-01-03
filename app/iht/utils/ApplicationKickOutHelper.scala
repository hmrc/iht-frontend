/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.enums.KickOutSource
import iht.utils.KickOutReason._
import iht.utils.{ApplicationStatus => AppStatus, KickOutReason => KickOut}
import play.api.mvc.Call

import scala.collection.immutable.ListMap

object ApplicationKickOutHelper {
  private lazy val estateOverviewControllerURL =
    iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef("").url
  lazy val SeenFirstKickoutPageCacheKey = "seenFirstKickoutPage"

  type FunctionListMap = ListMap[String, (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean]

  /**
    * Key used to store last assets section visited before kicked out.
    */
  lazy val applicationLastSectionKey = "applicationLastSection"

  /**
    * Key used to store last id saved before kicked out, e.g. property ID.
    */
  lazy val applicationLastIDKey = "applicationLastID"

  /**
    * Constants used to represent a section in application.
    */
  lazy val ApplicationSectionProperties = "Properties"

  lazy val ApplicationSectionAssetsMoneyDeceasedOwned = "MoneyDeceasedOwed"
  lazy val ApplicationSectionAssetsMoneyJointlyOwned = "MoneyJointlyOwed"

  lazy val ApplicationSectionAssetsHouseholdDeceasedOwned = "HouseholdDeceasedOwned"
  lazy val ApplicationSectionAssetsHouseholdJointlyOwned = "HouseholdJointlyOwned"

  lazy val ApplicationSectionAssetsVehiclesDeceasedOwned = "VehiclesDeceasedOwned"
  lazy val ApplicationSectionAssetsVehiclesJointlyOwned = "VehiclesJointlyOwned"

  lazy val ApplicationSectionAssetsPensions = "Pensions"
  lazy val ApplicationSectionAssetsPensionsValue = "PensionsValue"

  lazy val ApplicationSectionAssetsStocksAndSharesListed = "StocksListed"
  lazy val ApplicationSectionAssetsStocksAndSharesNotListed = "StocksNotListed"

  lazy val ApplicationSectionAssetsInsurancePoliciesOwnedByDeceased = "InsuranceOwnedByDeceased"
  lazy val ApplicationSectionAssetsInsurancePoliciesJointlyOwned = "InsuranceJointlyOwned"
  lazy val ApplicationSectionAssetsInsurancePoliciesAnnuities = "Insurance-Annuities"
  lazy val ApplicationSectionAssetsInsurancePoliciesMoreThanMax = "Insurance-MoreThanMax"
  lazy val ApplicationSectionAssetsInsurancePolicies7Years = "Insurance-7-Years"

  lazy val ApplicationSectionAssetsBusinessInterests = "BusinessInterests"

  lazy val ApplicationSectionAssetsNominatedAssets = "Nominated"

  lazy val ApplicationSectionAssetsInTrust = "InTrust"
  lazy val ApplicationSectionAssetsMoreThanOneTrust = "MoreThanOneTrust"
  lazy val ApplicationSectionAssetsTrustsValue = "TrustsValue"

  lazy val ApplicationSectionAssetsForeign = "Foreign"

  lazy val ApplicationSectionAssetsMoneyOwed = "MoneyOwed"

  lazy val ApplicationSectionGiftsWithReservation = "Reservation"
  lazy val ApplicationSectionGiftsGivenAway = "GivenAway"
  lazy val ApplicationSectionGiftDetails = "GiftDetails"
  lazy val ApplicationSectionExemptionsSpouse = "ExemptionsSpouse"
  lazy val ApplicationSectionAssetsOther = "Other"

  /**
    * Kickouts mapped to application section.
    */
  lazy val sections = ListMap(
    /* Assets */
    TrustsMoreThanOne -> ApplicationSectionAssetsMoreThanOneTrust,
    ForeignAssetsValueMoreThanMax -> ApplicationSectionAssetsForeign,
    TrustValueMoreThanMax -> ApplicationSectionAssetsTrustsValue,
    AnnuitiesOnInsurance -> ApplicationSectionAssetsInsurancePoliciesAnnuities,
    PensionDisposedLastTwoYears -> ApplicationSectionAssetsPensions,
    PensionsValueMoreThanMax -> ApplicationSectionAssetsPensionsValue,
    InTrustLessThanSevenYears -> ApplicationSectionAssetsInsurancePolicies7Years,
    InsuranceMoreThanMax -> ApplicationSectionAssetsInsurancePoliciesMoreThanMax,
    AssetsMoneyOwed -> ApplicationSectionAssetsMoneyOwed,
    AssetsDeceasedMoneyOwed -> ApplicationSectionAssetsMoneyDeceasedOwned,
    AssetsMoneyJointlyOwed -> ApplicationSectionAssetsMoneyJointlyOwned,

    /* Gifts */
    GiftsWithReservationOfBenefit -> ApplicationSectionGiftsWithReservation,
    GiftsGivenInPast -> ApplicationSectionGiftsGivenAway,
    GiftsToTrust -> ApplicationSectionGiftsGivenAway,
    GiftsMaxValue -> ApplicationSectionGiftsGivenAway,
    PartnerHomeInUK -> ApplicationSectionExemptionsSpouse
  )

  lazy val KickoutSequenceWidowCheck = Seq(WidowedCheckNotWidowed, PartnerDiedBeforeMinDate)

  /**
    * The kickout source, for metrics.
    */
  lazy val sources = ListMap(
    /* Assets */
    TrustsMoreThanOne -> KickOutSource.ASSET,
    ForeignAssetsValueMoreThanMax -> KickOutSource.ASSET,
    TrustValueMoreThanMax -> KickOutSource.ASSET,
    AnnuitiesOnInsurance -> KickOutSource.ASSET,
    PensionDisposedLastTwoYears -> KickOutSource.ASSET,
    PensionsValueMoreThanMax -> KickOutSource.ASSET,
    InTrustLessThanSevenYears -> KickOutSource.ASSET,
    SingleSectionMoreThanMax -> KickOutSource.ASSET,
    AssetsTotalValueMoreThanMax -> KickOutSource.ASSET,
    InsuranceMoreThanMax -> KickOutSource.ASSET,
    AssetsMoneyOwed -> KickOutSource.ASSET,
    AssetsDeceasedMoneyOwed -> KickOutSource.ASSET,
    AssetsMoneyJointlyOwed -> KickOutSource.ASSET,
    AssetsHouseholdDeceasedOwed -> KickOutSource.ASSET,
    AssetsHouseholdJointlyOwed -> KickOutSource.ASSET,
    AssetsVehiclesDeceasedOwned -> KickOutSource.ASSET,
    AssetsVehiclesJointlyOwned -> KickOutSource.ASSET,

    /* Gifts */
    GiftsWithReservationOfBenefit -> KickOutSource.GIFT,
    GiftsGivenInPast -> KickOutSource.GIFT,
    GiftsToTrust -> KickOutSource.GIFT,
    GiftsMaxValue -> KickOutSource.GIFT,

    PartnerHomeInUK -> KickOutSource.EXEMPTIONS,

    /* Pre-pre TNRB Eligibility */
    TnrbEstateMoreThanThreshold -> KickOutSource.TNRB,

    /* Pre-TNRB Eligibility */
    WidowedCheckNotWidowed -> KickOutSource.TNRB,
    PartnerDiedBeforeMinDate -> KickOutSource.TNRB,

    /* Pre-TNRB Eligibility For Opc*/
    PartnerDiedBeforeMinDateOpc -> KickOutSource.TNRB,

    /* TNRB Eligibility */
    PartnerNotLivingInUk -> KickOutSource.TNRB,
    GiftMadeBeforeDeath -> KickOutSource.TNRB,
    StateClaimAnyBusiness -> KickOutSource.TNRB,
    PartnerGiftWithResToOther -> KickOutSource.TNRB,
    PartnerBenFromTrust -> KickOutSource.TNRB,
    EstateBelowIhtThresholdApplied -> KickOutSource.TNRB,
    JointAssetPassed -> KickOutSource.TNRB,

    /* Back-end kickouts */
    ExemptionEstateValueIsMoreThanMaximum -> KickOutSource.EXEMPTIONS,
    EstateValueIsMoreThanMaximumLimit -> KickOutSource.EXEMPTIONS,
    AssetsTotalValueMoreThanThresholdAfterExemption -> KickOutSource.HOME,

    /* These ones could possibly be removed as they don't seem to be called. */
    TnrbEstateValueIsMoreThanMaximum -> KickOutSource.TNRB,
    PremiumsPaidForOtherPersonsPolicy -> KickOutSource.TNRB,
    NonJavaScript -> KickOutSource.TNRB,
    IHTHome -> KickOutSource.TNRB
  )

  /**
    * True if the estate value and threshold value should be displayed on the kickout page for a given kickout.
    */
  def shoulddisplayEstateValueAndThreshold(kickOutReason: String) = {
    val displayEstateValueAndThreshold = Set(TnrbEstateMoreThanThreshold,
      AssetsTotalValueMoreThanThresholdAfterExemption, WidowedCheckNotWidowed, PartnerDiedBeforeMinDate,
      PartnerDiedBeforeMinDateOpc, EstateValueIsMoreThanMaximumLimit)
    displayEstateValueAndThreshold.contains(kickOutReason)
  }

  private val unParameterizedSectionCalls = ListMap[String, Option[Call]](
    ApplicationSectionAssetsMoneyOwed -> Some(iht.controllers.application.assets.routes.MoneyOwedController.onPageLoad()),
    ApplicationSectionAssetsMoneyDeceasedOwned ->
      Some(iht.controllers.application.assets.money.routes.MoneyDeceasedOwnController.onPageLoad()),
    ApplicationSectionAssetsHouseholdDeceasedOwned ->
      Some(iht.controllers.application.assets.household.routes.HouseholdDeceasedOwnController.onPageLoad()),
    ApplicationSectionAssetsHouseholdJointlyOwned ->
      Some(iht.controllers.application.assets.household.routes.HouseholdJointlyOwnedController.onPageLoad()),
    ApplicationSectionAssetsVehiclesDeceasedOwned ->
      Some(iht.controllers.application.assets.vehicles.routes.VehiclesDeceasedOwnController.onPageLoad()),
    ApplicationSectionAssetsVehiclesJointlyOwned ->
      Some(iht.controllers.application.assets.vehicles.routes.VehiclesJointlyOwnedController.onPageLoad()),
    ApplicationSectionAssetsPensions ->
      Some(iht.controllers.application.assets.pensions.routes.PensionsChangedQuestionController.onPageLoad()),
    ApplicationSectionAssetsPensionsValue ->
      Some(iht.controllers.application.assets.pensions.routes.PensionsValueController.onPageLoad()),
    ApplicationSectionAssetsStocksAndSharesListed ->
      Some(iht.controllers.application.assets.stocksAndShares.routes.StocksAndSharesListedController.onPageLoad()),
    ApplicationSectionAssetsStocksAndSharesNotListed ->
      Some(iht.controllers.application.assets.stocksAndShares.routes.StocksAndSharesNotListedController.onPageLoad()),
    ApplicationSectionAssetsInsurancePoliciesJointlyOwned ->
      Some(iht.controllers.application.assets.insurancePolicy.routes.InsurancePolicyDetailsJointController.onPageLoad()),
    ApplicationSectionAssetsInsurancePoliciesOwnedByDeceased ->
      Some(iht.controllers.application.assets.insurancePolicy.routes.InsurancePolicyDetailsDeceasedOwnController.onPageLoad()),
    ApplicationSectionAssetsBusinessInterests ->
      Some(iht.controllers.application.assets.routes.BusinessInterestsController.onPageLoad()),
    ApplicationSectionAssetsNominatedAssets ->
      Some(iht.controllers.application.assets.routes.NominatedController.onPageLoad()),
    ApplicationSectionAssetsForeign ->
      Some(iht.controllers.application.assets.routes.ForeignController.onPageLoad()),
    ApplicationSectionAssetsMoneyJointlyOwned ->
      Some(iht.controllers.application.assets.money.routes.MoneyJointlyOwnedController.onPageLoad()),
    ApplicationSectionAssetsOther ->
      Some(iht.controllers.application.assets.routes.OtherController.onPageLoad()),
    ApplicationSectionGiftsWithReservation ->
      Some(iht.controllers.application.gifts.routes.WithReservationOfBenefitController.onPageLoad()),
    ApplicationSectionExemptionsSpouse ->
      Some(iht.controllers.application.exemptions.partner.routes.PartnerPermanentHomeQuestionController.onPageLoad()),
    ApplicationSectionAssetsInsurancePolicies7Years ->
      Some(iht.controllers.application.assets.insurancePolicy.routes.InsurancePolicyDetailsInTrustController.onPageLoad()),
    ApplicationSectionAssetsInsurancePoliciesAnnuities ->
      Some(iht.controllers.application.assets.insurancePolicy.routes.InsurancePolicyDetailsAnnuityController.onPageLoad()),
    ApplicationSectionAssetsInsurancePoliciesMoreThanMax ->
      Some(iht.controllers.application.assets.insurancePolicy.routes.InsurancePolicyDetailsMoreThanMaxValueController.onPageLoad())
  )

  private def sectionCall(applicationSection: String, lastID: Option[String]): Option[Call] = {
    unParameterizedSectionCalls.get(applicationSection) match {
      case None =>
        applicationSection match {
          case ApplicationSectionProperties =>
            lastID.map(li => iht.controllers.application.assets.properties.routes.PropertyValueController.onEditPageLoadForKickout(li))
          case ApplicationSectionGiftDetails =>
            lastID.map(li => iht.controllers.application.gifts.routes.GiftsDetailsController.onPageLoadForKickout(li))
          case _ => None
        }
      case Some(optionCall) => optionCall
    }
  }

  private def sectionCallAsLeft(as: String) = sectionCall(as, None).fold(throw new RuntimeException("Back link not found:" + as))(Left(_))


  /**
    * Return link urls, as a ListMap of section to Either:
    * Left: a fixed url.
    * Right: None indicates the url should be the last application section and id saved, else url to which the iht ref
    * is appended.
    */
  lazy val returnLinkUrls: ListMap[String, Either[Call, Option[String]]] = ListMap(
    /* Assets */
    TrustsMoreThanOne -> Left(iht.controllers.application.assets.trusts.routes.TrustsMoreThanOneQuestionController.onPageLoad()),
    ForeignAssetsValueMoreThanMax -> sectionCallAsLeft(ApplicationSectionAssetsForeign),
    TrustValueMoreThanMax -> Left(iht.controllers.application.assets.trusts.routes.TrustsValueController.onPageLoad()),
    AnnuitiesOnInsurance -> sectionCallAsLeft(ApplicationSectionAssetsInsurancePoliciesAnnuities),
    PensionDisposedLastTwoYears -> sectionCallAsLeft(ApplicationSectionAssetsPensions),
    PensionsValueMoreThanMax -> sectionCallAsLeft(ApplicationSectionAssetsPensionsValue),
    InTrustLessThanSevenYears -> sectionCallAsLeft(ApplicationSectionAssetsInsurancePolicies7Years),
    SingleSectionMoreThanMax -> Right(None),
    AssetsTotalValueMoreThanMax -> Right(Some(estateOverviewControllerURL)),
    InsuranceMoreThanMax -> sectionCallAsLeft(ApplicationSectionAssetsInsurancePoliciesMoreThanMax),
    AssetsMoneyOwed -> sectionCallAsLeft(ApplicationSectionAssetsMoneyOwed),
    AssetsDeceasedMoneyOwed -> sectionCallAsLeft(ApplicationSectionAssetsMoneyDeceasedOwned),
    AssetsMoneyJointlyOwed -> sectionCallAsLeft(ApplicationSectionAssetsMoneyJointlyOwned),
    AssetsHouseholdDeceasedOwed -> sectionCallAsLeft(ApplicationSectionAssetsHouseholdDeceasedOwned),
    AssetsHouseholdJointlyOwed -> sectionCallAsLeft(ApplicationSectionAssetsHouseholdJointlyOwned),
    AssetsVehiclesDeceasedOwned -> sectionCallAsLeft(ApplicationSectionAssetsVehiclesDeceasedOwned),
    AssetsVehiclesJointlyOwned -> sectionCallAsLeft(ApplicationSectionAssetsVehiclesJointlyOwned),

    /* Exemptions */
    PartnerHomeInUK -> sectionCallAsLeft(ApplicationSectionExemptionsSpouse),

    /* Gifts */
    GiftsWithReservationOfBenefit -> sectionCallAsLeft(ApplicationSectionGiftsWithReservation),
    GiftsGivenInPast -> Left(iht.controllers.application.gifts.routes.SevenYearsGivenInLast7YearsController.onPageLoad()),
    GiftsToTrust -> Left(iht.controllers.application.gifts.routes.SevenYearsToTrustController.onPageLoad()),
    GiftsMaxValue -> Left(iht.controllers.application.gifts.routes.SevenYearsGiftsValuesController.onPageLoad()),

    /* Pre-pre TNRB Eligibility */
    TnrbEstateMoreThanThreshold -> Right(Some(estateOverviewControllerURL)),
    AssetsTotalValueMoreThanThresholdAfterExemption -> Right(Some(estateOverviewControllerURL)),

    /* Pre-TNRB Eligibility */
    WidowedCheckNotWidowed -> Right(Some(estateOverviewControllerURL)),
    PartnerDiedBeforeMinDate -> Right(Some(estateOverviewControllerURL)),

    /* Pre-TNRB EligibilityOpc */
    PartnerDiedBeforeMinDateOpc -> Left(iht.controllers.application.tnrb.routes.DeceasedWidowCheckDateController.onPageLoad()),

    /* TNRB Eligibility */
    PartnerNotLivingInUk -> Left(iht.controllers.application.tnrb.routes.PermanentHomeController.onPageLoad()),
    GiftMadeBeforeDeath -> Left(iht.controllers.application.tnrb.routes.GiftsMadeBeforeDeathController.onPageLoad()),
    StateClaimAnyBusiness -> Left(iht.controllers.application.tnrb.routes.EstateClaimController.onPageLoad()),
    PartnerGiftWithResToOther -> Left(iht.controllers.application.tnrb.routes.GiftsWithReservationOfBenefitController.onPageLoad()),
    PartnerBenFromTrust -> Left(iht.controllers.application.tnrb.routes.BenefitFromTrustController.onPageLoad()),
    EstateBelowIhtThresholdApplied -> Left(iht.controllers.application.tnrb.routes.EstatePassedToDeceasedOrCharityController.onPageLoad()),
    JointAssetPassed -> Left(iht.controllers.application.tnrb.routes.JointlyOwnedAssetsController.onPageLoad())
  )

  /**
    * The url for the "return" link.
    */
  def returnLinkUrl(kickoutReason: String, ihtRef: String, applicationLastSection: Option[String],
                    applicationLastID: Option[String]): Option[Call] = {
    val returnLinkFound: Option[(String, Either[Call, Option[String]])] = returnLinkUrls.find(_._1 == kickoutReason)
    returnLinkFound.map {
      rl =>
        rl._2.fold[Call](
          callFound => callFound,
          stringOptionFound => {
            stringOptionFound.fold {
              val lastSectionAsCall: Option[Call] = applicationLastSection.flatMap(ls => sectionCall(ls, applicationLastID))
              lastSectionAsCall.fold(Call("GET", estateOverviewControllerURL + ihtRef))(identity)
            }(stringFound => Call.apply("GET", stringFound + ihtRef))
          }
        )
    }
  }

  lazy val emptyFunctionListMap: FunctionListMap = ListMap()

}
