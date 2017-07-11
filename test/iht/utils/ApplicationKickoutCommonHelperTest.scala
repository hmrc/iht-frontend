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

import iht.FakeIhtApp
import iht.models.UkAddress
import iht.models.application.assets.Property
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.models.application.exemptions.BasicExemptionElement
import iht.testhelpers.CommonBuilder
import iht.testhelpers.CommonBuilder._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class ApplicationKickoutCommonHelperTest extends UnitSpec with MockitoSugar with FakeIhtApp {

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC123")
    )

  "update application details with correct kickout reason and status for TrustsMoreThanOne" in {
    CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.TrustsMoreThanOne) foreach { ad => {
      val result =
        ApplicationKickOutNonSummaryHelper.check(prioritySection=Some(ApplicationKickOutHelper.ApplicationSectionAssetsInTrust),
            registrationDetails=registrationDetails, applicationDetails=ad,
            sectionTotal=Seq(BigDecimal(0)))
        result shouldBe Some(KickOutReason.TrustsMoreThanOne)
      }
    }
  }

  "update application details with correct kickout reason and status for ForeignAssetsValueMoreThanMax" in {
    CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.ForeignAssetsValueMoreThanMax) foreach { ad =>
      val result =
        ApplicationKickOutNonSummaryHelper.check(prioritySection=Some(ApplicationKickOutHelper.ApplicationSectionAssetsForeign),
          registrationDetails=registrationDetails, applicationDetails=ad, sectionTotal=Seq(BigDecimal(0)))
      result shouldBe Some(KickOutReason.ForeignAssetsValueMoreThanMax)
    }
  }

  "update application details with correct kickout reason and status for TrustValueMoreThanMax" in {
    CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.TrustValueMoreThanMax) foreach { ad =>
      val result =
        ApplicationKickOutNonSummaryHelper.check(prioritySection=Some(ApplicationKickOutHelper.ApplicationSectionAssetsMoneyDeceasedOwned),
          registrationDetails=registrationDetails, applicationDetails=ad, sectionTotal=Seq(BigDecimal(0)))
      result shouldBe Some(KickOutReason.TrustValueMoreThanMax)
    }
  }

  "update application details with correct kickout reason and status for PensionDisposedLastTwoYears" in {
    CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.PensionDisposedLastTwoYears) foreach { ad =>
      val result =
        ApplicationKickOutNonSummaryHelper.check(prioritySection=Some(ApplicationKickOutHelper.ApplicationSectionAssetsPensions),
          registrationDetails=registrationDetails, applicationDetails=ad, sectionTotal=Seq(BigDecimal(0)))
      result shouldBe Some(KickOutReason.PensionDisposedLastTwoYears)
    }
  }


  "update application details with correct kickout reason and status for InTrustLessThanSevenYears" in {
    CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.InTrustLessThanSevenYears) foreach { ad =>
      val result =
        ApplicationKickOutNonSummaryHelper.check(prioritySection=Some(ApplicationKickOutHelper.ApplicationSectionAssetsInTrust),
          registrationDetails=registrationDetails, applicationDetails=ad, sectionTotal=Seq(BigDecimal(0)))
      result shouldBe Some(KickOutReason.InTrustLessThanSevenYears)
    }
  }

  "update application details with correct kickout reason and status for InsuranceMoreThanMax" in {
    CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.InsuranceMoreThanMax) foreach { ad =>
      val result =
        ApplicationKickOutNonSummaryHelper.check(prioritySection=
          Some(ApplicationKickOutHelper.ApplicationSectionAssetsInsurancePoliciesMoreThanMax),
          registrationDetails=registrationDetails, applicationDetails=ad, sectionTotal=Seq(BigDecimal(0)))
      result shouldBe Some(KickOutReason.InsuranceMoreThanMax)
    }
  }

  "update application details with correct kickout reason and status for GiftsWithReservationOfBenefit" in {
    CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.GiftsWithReservationOfBenefit) foreach { ad =>
      val result =
        ApplicationKickOutNonSummaryHelper.check(prioritySection=Some(ApplicationKickOutHelper.ApplicationSectionGiftsWithReservation),
          registrationDetails=registrationDetails, applicationDetails=ad, sectionTotal=Seq(BigDecimal(0)))
      result shouldBe Some(KickOutReason.GiftsWithReservationOfBenefit)
    }
  }

  "update application details with correct kickout reason and status for GiftsGivenInPast" in {
    CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.GiftsGivenInPast) foreach { ad =>
      val result =
        ApplicationKickOutNonSummaryHelper.check(prioritySection=Some(ApplicationKickOutHelper.ApplicationSectionGiftsGivenAway),
          registrationDetails=registrationDetails, applicationDetails=ad, sectionTotal=Seq(BigDecimal(0)))
      result shouldBe Some(KickOutReason.GiftsToTrust)
    }
  }

  "update application details with correct kickout reason and status for GiftsToTrust" in {
    CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.GiftsToTrust) foreach { ad =>
      val result =
        ApplicationKickOutNonSummaryHelper.check(prioritySection=Some(ApplicationKickOutHelper.ApplicationSectionGiftsGivenAway),
          registrationDetails=registrationDetails, applicationDetails=ad, sectionTotal=Seq(BigDecimal(0)))
      result shouldBe Some(KickOutReason.GiftsGivenInPast)
    }
  }

  "update application details with correct kickout reason and status for GiftsMaxValue" in {
    CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.GiftsMaxValue) foreach { ad =>
      val result =
        ApplicationKickOutNonSummaryHelper.check(prioritySection=Some(ApplicationKickOutHelper.ApplicationSectionGiftsGivenAway),
          registrationDetails=registrationDetails, applicationDetails=ad, sectionTotal=Seq(BigDecimal(0)))
      result shouldBe Some(KickOutReason.GiftsMaxValue)
    }
  }

  "update application details with correct kickout reason and status for AssetsTotalValueMoreThanMax when " +
    "all asset sections filled in with value" in {
    CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.AssetsTotalValueMoreThanMax) foreach { ad =>
      val result =
        ApplicationKickOutNonSummaryHelper.check(prioritySection=None,
          registrationDetails=registrationDetails, applicationDetails=ad, sectionTotal=Seq(BigDecimal(0)))
      result shouldBe Some(KickOutReason.AssetsTotalValueMoreThanMax)
    }
  }

  "update application details with correct kickout reason and status where TrustsMoreThanOne applies " +
    "but last section changed was money and > £1 million" in {
    CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.TrustsMoreThanOne) foreach { ad =>
      CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.TrustValueMoreThanMax) foreach { ad =>
        val result =
          ApplicationKickOutNonSummaryHelper.check(prioritySection=Some(ApplicationKickOutHelper.ApplicationSectionAssetsMoneyDeceasedOwned),
            registrationDetails=registrationDetails, applicationDetails=ad, sectionTotal=Seq(BigDecimal(1000001)))
        result shouldBe Some(KickOutReason.SingleSectionMoreThanMax)
      }
    }
  }

  "update application details with correct kickout reason and status for PartnerHomeInUK" in {
    CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.PartnerHomeInUK) foreach { ad =>
      val result =
        ApplicationKickOutNonSummaryHelper.check(prioritySection=Some(ApplicationKickOutHelper.ApplicationSectionGiftsGivenAway),
          registrationDetails=registrationDetails, applicationDetails=ad, sectionTotal=Seq(BigDecimal(0)))
      result shouldBe Some(KickOutReason.PartnerHomeInUK)
    }
  }

  "update application details with correct kickout reason and status for AssetsTotalValueMoreThanThresholdAfterExemption" in {

    val appDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
      allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
        money = Some(ShareableBasicEstateElement(Some(400000),Some(200000), Some(true), Some(true)))
      )),
      allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
        partner = Some(CommonBuilder.buildPartnerExemption.copy(isPartnerHomeInUK = Some(true))),
        charity = Some(BasicExemptionElement(Some(true))),
        qualifyingBody = Some(BasicExemptionElement(Some(false))))),
      charities = Seq(CommonBuilder.buildCharity.copy(
        Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(40000)))))


    val result = ApplicationKickOutNonSummaryHelper.updateKickout(checks =ApplicationKickOutNonSummaryHelper.checksBackend,
          registrationDetails=registrationDetails, applicationDetails=appDetails)
      result.kickoutReason shouldBe Some(KickOutReason.AssetsTotalValueMoreThanThresholdAfterExemption)

  }

  def prop(id: String, value: Int) =
    Property(Some(id), Some(UkAddress( "a", "a", None, None, CommonBuilder.DefaultPostCode, "GB")), None, None, None, Some(value))

  val ad = kickoutUpdateGift(kickoutApplicationDetails, isRes = false, isToTrust = false, isLast7 = false, 1, 5, 8, 0, 0, 0, 0) copy(
    propertyList = List[Property](prop("1", 100), prop("2", 200), prop("3", 300)))

  Seq(ApplicationKickOutHelper.ApplicationSectionAssetsBusinessInterests -> Tuple2(Seq(BigDecimal(250)), None),
    ApplicationKickOutHelper.ApplicationSectionAssetsForeign -> Tuple2(Seq(BigDecimal(20)), None),
    ApplicationKickOutHelper.ApplicationSectionAssetsInsurancePoliciesJointlyOwned -> Tuple2(Seq(BigDecimal(8000)), None),
    ApplicationKickOutHelper.ApplicationSectionAssetsInsurancePoliciesOwnedByDeceased -> Tuple2(Seq(BigDecimal(9000)), None),
    ApplicationKickOutHelper.ApplicationSectionAssetsInTrust -> Tuple2(Seq(BigDecimal(25)), None),
    ApplicationKickOutHelper.ApplicationSectionAssetsMoneyDeceasedOwned -> Tuple2(Seq(BigDecimal(400000)), None),
    ApplicationKickOutHelper.ApplicationSectionAssetsMoneyJointlyOwned -> Tuple2(Seq(BigDecimal(50)), None),
    ApplicationKickOutHelper.ApplicationSectionAssetsHouseholdDeceasedOwned -> Tuple2(Seq(BigDecimal(200000)), None),
    ApplicationKickOutHelper.ApplicationSectionAssetsHouseholdJointlyOwned -> Tuple2(Seq(BigDecimal(60)), None),
    ApplicationKickOutHelper.ApplicationSectionAssetsVehiclesDeceasedOwned -> Tuple2(Seq(BigDecimal(240000)), None),
    ApplicationKickOutHelper.ApplicationSectionAssetsVehiclesJointlyOwned -> Tuple2(Seq(BigDecimal(70)), None),
    ApplicationKickOutHelper.ApplicationSectionAssetsMoneyOwed -> Tuple2(Seq(BigDecimal(4)), None),
    ApplicationKickOutHelper.ApplicationSectionAssetsNominatedAssets -> Tuple2(Seq(BigDecimal(200)), None),
    ApplicationKickOutHelper.ApplicationSectionAssetsOther -> Tuple2(Seq(BigDecimal(2)), None),
    ApplicationKickOutHelper.ApplicationSectionAssetsPensionsValue -> Tuple2(Seq(BigDecimal(500)), None),
    ApplicationKickOutHelper.ApplicationSectionProperties -> Tuple2(Seq(BigDecimal(100)), Some("1")),
    ApplicationKickOutHelper.ApplicationSectionProperties -> Tuple2(Seq(BigDecimal(200)), Some("2")),
    ApplicationKickOutHelper.ApplicationSectionProperties -> Tuple2(Seq(BigDecimal(300)), Some("3"))
    ).foreach { section =>
      "get section total should calculate correct total for " + section._1 + " section " + section._2._2.fold("")(xx=>"(" + xx + ")") in {
        ApplicationKickOutNonSummaryHelper.getSectionTotal(Some(section._1), section._2._2,
           ad) shouldBe section._2._1
    }
  }
}
