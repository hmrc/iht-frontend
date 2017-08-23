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

package iht.utils.pdf

import iht.forms.FormTestHelper
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.PreviousYearsGifts
import iht.models.des.ihtReturn.Asset
import iht.testhelpers.CommonBuilder
import iht.testhelpers.IHTReturnTestHelper.{buildIHTReturnCorrespondingToApplicationDetailsAllFields, _}
import org.joda.time.LocalDate

import scala.collection.immutable.ListMap

/**
  * Created by david-beer on 21/11/16.
  */
class PdfFormatterTest extends FormTestHelper {

  val regDetails = CommonBuilder.buildRegistrationDetails1

  def incompleteTnrbIncreaseThreshold(ad: ApplicationDetails) = {
    ad copy(
      increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility),
      widowCheck = None
    )
  }

  def completeTnrbIncreaseThreshold(ad: ApplicationDetails) = {
    val tnrb = CommonBuilder.buildTnrbEligibility copy (
      dateOfPreDeceased = CommonBuilder.DefaultPartnerDOD
      )
    ad copy(
      increaseIhtThreshold = Some(tnrb),
      widowCheck = Some(CommonBuilder.buildWidowedCheck)
    )
  }

  def completeTnrbNotIncreaseThreshold(ad: ApplicationDetails) = {
    val tnrb = CommonBuilder.buildTnrbEligibility copy(
      isPartnerLivingInUk = Some(false),
      dateOfPreDeceased = CommonBuilder.DefaultPartnerDOD
    )
    ad copy(
      increaseIhtThreshold = Some(tnrb),
      widowCheck = Some(CommonBuilder.buildWidowedCheck)
    )
  }

  "display value" must {
    "must return the year from specified date" in {
      val result = PdfFormatter.getYearFromDate("1990-06-05")
      result shouldBe 1990
    }

    "must return Australia fo AU" in {
      val result = PdfFormatter.countryName("AU")
      result shouldBe "Australia"
    }
  }

  "transform asset descriptions correctly for display" in {
    val etmpTitlesMappedToPDFMessageKeys = ListMap(
      "Rolled up bank and building society accounts" -> "iht.estateReport.assets.money.upperCaseInitial",
      "Rolled up household and personal goods" -> "iht.estateReport.assets.householdAndPersonalItems.title",
      "Rolled up pensions" -> "iht.estateReport.assets.privatePensions",
      "Rolled up unlisted stocks and shares" -> "iht.estateReport.assets.stocksAndSharesNotListed",
      "Rolled up quoted stocks and shares" -> "iht.estateReport.assets.stocksAndSharesListed",
      "Rolled up life assurance policies" -> "iht.estateReport.assets.insurancePolicies",
      "Rolled up business assets" -> "iht.estateReport.assets.businessInterests.title",
      "Rolled up nominated assets" -> "iht.estateReport.assets.nominated",
      "Rolled up trust assets" -> "iht.estateReport.assets.heldInATrust.title",
      "Rolled up foreign assets" -> "iht.estateReport.assets.foreign.title",
      "Rolled up money owed to deceased" -> "iht.estateReport.assets.moneyOwed",
      "Rolled up other assets" -> "page.iht.application.assets.main-section.other.title",
      "Deceased's residence" -> "page.iht.application.assets.propertyType.deceasedHome.label"
    )
    val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "")
    val expectedSetOfAssets = ihtReturn.freeEstate.flatMap(_.estateAssets)
      .fold[Set[Asset]](Set.empty)(identity).map { asset =>
      val newAssetDescription = asset.assetDescription.map(x =>
        etmpTitlesMappedToPDFMessageKeys.get(x) match {
          case None => x
          case Some(newMessageKey) => messagesApi(newMessageKey, regDetails.deceasedDetails.fold("")(_.name))
        }
      )
      asset copy (assetDescription = newAssetDescription)
    }
    val result = PdfFormatter.transform(ihtReturn, regDetails, messages)
    val setOfAssets = result.freeEstate.flatMap(_.estateAssets).fold[Set[Asset]](Set.empty)(identity)
    setOfAssets shouldBe expectedSetOfAssets
  }

  "transform" must {
    "transform the marital status" in {
      val rd = PdfFormatter.transform(CommonBuilder.buildRegistrationDetails4, messages)
      val result = rd.deceasedDetails.flatMap(_.maritalStatus).fold("")(identity)
      result shouldBe messagesApi("page.iht.registration.deceasedDetails.maritalStatus.civilPartnership.label")
    }

    "map the tenure value from messages file" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = CommonBuilder.buildPropertyList)
      val appDetailsAfterFormatting = PdfFormatter.transform(appDetails, regDetails, messages)

      val result = appDetailsAfterFormatting.propertyList.head.tenure
      result shouldBe Some(messagesApi("page.iht.application.assets.tenure.freehold.label"))
    }

    "map the property type value from messages file" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = CommonBuilder.buildPropertyList)
      val appDetailsAfterFormatting = PdfFormatter.transform(appDetails, regDetails, messages)

      val result = appDetailsAfterFormatting.propertyList.head.propertyType
      result shouldBe Some(messagesApi("page.iht.application.assets.propertyType.deceasedHome.label"))
    }

    "map the property ownership value from messages file" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = CommonBuilder.buildPropertyList)
      val regDetails = CommonBuilder.buildRegistrationDetails1
      val appDetailsAfterFormatting = PdfFormatter.transform(appDetails, regDetails, messages)

      val result = appDetailsAfterFormatting.propertyList.head.typeOfOwnership
      result shouldBe Some(messagesApi("page.iht.application.assets.typeOfOwnership.deceasedOnly.label",
        regDetails.deceasedDetails.fold("")(_.name)))
    }
  }

  "estateOverviewDisplayMode" must {
    "return 1 when only assets, gifts and debts are filled in (base)" in {
      val ad = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts
      PdfFormatter.estateOverviewDisplayMode(ad) shouldBe 1
    }

    "return 2 when in addition to base Tnrb completed and exemptions locked" in {
      val ad =
        CommonBuilder.buildExemptionsWithNoValues(
          completeTnrbIncreaseThreshold(CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts) copy (
            hasSeenExemptionGuidance = Some(false)
            )
        )
      PdfFormatter.estateOverviewDisplayMode(ad) shouldBe 2
    }

    "return 3 when in addition to base Tnrb started but not completed and exemptions unlocked but zero" in {
      val ad =
        CommonBuilder.buildExemptionsWithNoValues(
          incompleteTnrbIncreaseThreshold(CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts) copy (
            hasSeenExemptionGuidance = Some(true)
            )
        )
      PdfFormatter.estateOverviewDisplayMode(ad) shouldBe 3
    }

    "return 3 when in addition to base Tnrb not increased and exemptions unlocked but zero" in {
      val ad =
        CommonBuilder.buildExemptionsWithNoValues(
          completeTnrbNotIncreaseThreshold(CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts) copy (
            hasSeenExemptionGuidance = Some(true)
            )
        )
      PdfFormatter.estateOverviewDisplayMode(ad) shouldBe 3
    }

    "return 4 when in addition to base Tnrb increased and exemptions unlocked but zero" in {
      val ad =
        CommonBuilder.buildExemptionsWithNoValues(
          completeTnrbIncreaseThreshold(CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts) copy (
            hasSeenExemptionGuidance = Some(true)
            )
        )
      PdfFormatter.estateOverviewDisplayMode(ad) shouldBe 4
    }

    "return 5 when in addition to base Tnrb increased and exemptions unlocked but more than zero" in {
      val ad =
        CommonBuilder.buildSomeExemptions(
          completeTnrbIncreaseThreshold(CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts) copy (
            hasSeenExemptionGuidance = Some(true)
            )
        )
      PdfFormatter.estateOverviewDisplayMode(ad) shouldBe 5
    }

    "return 6 when in addition to base Tnrb started but not completed and exemptions unlocked but more than zero" in {
      val ad =
        CommonBuilder.buildSomeExemptions(
          incompleteTnrbIncreaseThreshold(CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts) copy (
            hasSeenExemptionGuidance = Some(true)
            )
        )
      PdfFormatter.estateOverviewDisplayMode(ad) shouldBe 6
    }

    "return 6 when in addition to base Tnrb not increased and exemptions unlocked but more than zero" in {
      val ad =
        CommonBuilder.buildSomeExemptions(
          completeTnrbNotIncreaseThreshold(CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts) copy (
            hasSeenExemptionGuidance = Some(true)
            )
        )
      PdfFormatter.estateOverviewDisplayMode(ad) shouldBe 6
    }
  }

  //  "combineGiftSets" must {
  //    "combine two sets updating with values in second set" in {
  //      val expectedGifts = Set(
  //        makeGiftWithOutExemption(111, toDate("2008-04-05")),
  //        makeGiftWithOutExemption(5000, toDate("2009-04-05")),
  //        makeGiftWithOutExemption(222, toDate("2010-04-05")),
  //        makeGiftWithOutExemption(5000, toDate("2011-04-05")),
  //        makeGiftWithOutExemption(5000, toDate("2012-04-05")),
  //        makeGiftWithOutExemption(333, toDate("2013-04-05")),
  //        makeGiftWithOutExemption(444, toDate("2014-10-05"))
  //      )
  //
  //      val gifts1 = Set(
  //        makeGiftWithOutExemption(3000, toDate("2008-04-05")),
  //        makeGiftWithOutExemption(5000, toDate("2009-04-05")),
  //        makeGiftWithOutExemption(5000, toDate("2010-04-05")),
  //        makeGiftWithOutExemption(5000, toDate("2011-04-05")),
  //        makeGiftWithOutExemption(5000, toDate("2012-04-05")),
  //        makeGiftWithOutExemption(5000, toDate("2013-04-05")),
  //        makeGiftWithOutExemption(7000, toDate("2014-10-05"))
  //      )
  //
  //      val gifts2 = Set(
  //        makeGiftWithOutExemption(111, toDate("2008-04-05")),
  //        makeGiftWithOutExemption(222, toDate("2010-04-05")),
  //        makeGiftWithOutExemption(333, toDate("2013-04-05")),
  //        makeGiftWithOutExemption(444, toDate("2014-10-05"))
  //      )
  //
  //      PdfFormatter.combineGiftSets(gifts1, gifts2) shouldBe expectedGifts
  //    }
  //  }

  "padGifts" must {
    "pad correctly where 7 years exactly" in {
      val dateOfDeath = CommonBuilder.DefaultDOD // LocalDate(2014, 10, 5)

      val expectedGifts = Set(Set(
        makeGiftWithOutExemption(444, toDate("2008-04-05")),
        makeGiftWithOutExemption(555, toDate("2009-04-05")),
        makeGiftWithOutExemption(111, toDate("2010-04-05")),
        makeGiftWithOutExemption(222, toDate("2011-04-05")),
        makeGiftWithOutExemption(666, toDate("2012-04-05")),
        makeGiftWithOutExemption(333, toDate("2013-04-05")),
        makeGiftWithOutExemption(777, toDate("2014-04-05")),
        makeGiftWithOutExemption(888, toDate("2014-10-05"))
      ))

      val gifts = Set(Set(
        makeGiftWithOutExemption(444, toDate("2008-04-05")),
        makeGiftWithOutExemption(555, toDate("2009-04-05")),
        makeGiftWithOutExemption(111, toDate("2010-04-05")),
        makeGiftWithOutExemption(222, toDate("2011-04-05")),
        makeGiftWithOutExemption(666, toDate("2012-04-05")),
        makeGiftWithOutExemption(333, toDate("2013-04-05")),
        makeGiftWithOutExemption(777, toDate("2014-04-05")),
        makeGiftWithOutExemption(888, toDate("2014-10-05"))
      ))
      val result = gifts.map(setGifts => PdfFormatter.padGifts(setGifts, dateOfDeath))

      result shouldBe expectedGifts
    }

    //    "pad correctly where < 7 years" in {
    //      val dateOfDeath = CommonBuilder.DefaultDOD // LocalDate(2014, 10, 5)
    //
    //      val expectedGifts = Set(Set(
    //        makeGiftWithOutExemption(0,   toDate("2007-10-06")),
    //        makeGiftWithOutExemption(0,   toDate("2008-04-06")),
    //        makeGiftWithOutExemption(111, toDate("2009-04-06")),
    //        makeGiftWithOutExemption(222, toDate("2010-04-06")),
    //        makeGiftWithOutExemption(0,   toDate("2011-04-06")),
    //        makeGiftWithOutExemption(333, toDate("2012-04-06")),
    //        makeGiftWithOutExemption(0,   toDate("2013-04-06")),
    //        makeGiftWithOutExemption(0,   toDate("2014-04-06"))
    //      ))
    //
    //      val gifts = Set(Set(
    //        makeGiftWithOutExemption(111, toDate("2009-04-06")),
    //        makeGiftWithOutExemption(222, toDate("2010-04-06")),
    //        makeGiftWithOutExemption(333, toDate("2012-04-06"))
    //      ))
    //      val result = gifts.map(setGifts => PdfFormatter.padGifts(setGifts, dateOfDeath))
    //
    //      result shouldBe expectedGifts
    //    }
  }

}