/*
 * Copyright 2020 HM Revenue & Customs
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
import iht.models.UkAddress
import iht.models.application.ApplicationDetails
import iht.models.application.assets.{AllAssets, Properties, Property}
import iht.models.des.ihtReturn.Asset
import iht.testhelpers.IHTReturnTestHelper.{buildIHTReturnCorrespondingToApplicationDetailsAllFields, _}
import iht.testhelpers.{CommonBuilder, IHTReturnTestHelper, TestHelper}
import org.joda.time.LocalDate
import play.api.i18n.Lang

import scala.collection.immutable.ListMap

/**
  * Created by david-beer on 21/11/16.
  */
class PdfFormatterTest extends FormTestHelper with PdfHelper {

  val regDetails = CommonBuilder.buildRegistrationDetails1
  val appConfig = mockAppConfig

  implicit val lang: Lang = Lang.defaultLang

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
      result mustBe 1990
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
    val result = transform(ihtReturn, regDetails, messages)
    val setOfAssets = result.freeEstate.flatMap(_.estateAssets).fold[Set[Asset]](Set.empty)(identity)
    setOfAssets.foreach { asset =>
      val expectedAssetDescription = expectedSetOfAssets
        .find(x => x.assetCode == asset.assetCode && x.howheld == asset.howheld).flatMap(_.assetDescription)
      asset.assetDescription mustBe expectedAssetDescription
    }
  }

  "transform" must {
    "transform the marital status" in {
      val rd = PdfFormatter.transform(CommonBuilder.buildRegistrationDetails4, messages)
      val result = rd.deceasedDetails.flatMap(_.maritalStatus).fold("")(identity)
      result mustBe messagesApi("page.iht.registration.deceasedDetails.maritalStatus.civilPartnership.label")
    }

    "transform the deceased country code to country name" in {
      val rd = PdfFormatter.transform(CommonBuilder.buildRegistrationDetails4, messages)
      val result = rd.deceasedDetails.flatMap(_.ukAddress).map(_.countryCode).fold("")(identity)
      result mustBe messagesApi("country.GB")
    }

    "transform the applicant country code to country name" in {
      val rd = PdfFormatter.transform(CommonBuilder.buildRegistrationDetails4, messages)
      val result = rd.applicantDetails.flatMap(_.ukAddress).map(_.countryCode).fold("")(identity)
      result mustBe messagesApi("country.GB")
    }

    "map the tenure value from messages file" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = CommonBuilder.buildPropertyList)
      val appDetailsAfterFormatting = PdfFormatter.transformWithApplicationDetails(appDetails, regDetails, messages)

      val result = appDetailsAfterFormatting.propertyList.head.tenure
      result mustBe Some(messagesApi("page.iht.application.assets.tenure.freehold.label"))
    }

    "map the property type value from messages file" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = CommonBuilder.buildPropertyList)
      val appDetailsAfterFormatting = PdfFormatter.transformWithApplicationDetails(appDetails, regDetails, messages)

      val result = appDetailsAfterFormatting.propertyList.head.propertyType
      result mustBe Some(messagesApi("page.iht.application.assets.propertyType.deceasedHome.label"))
    }

    "map the property ownership value from messages file" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = CommonBuilder.buildPropertyList)
      val regDetails = CommonBuilder.buildRegistrationDetails1
      val appDetailsAfterFormatting = PdfFormatter.transformWithApplicationDetails(appDetails, regDetails, messages)

      val result = appDetailsAfterFormatting.propertyList.head.typeOfOwnership
      result mustBe Some(messagesApi("page.iht.application.assets.typeOfOwnership.deceasedOnly.label",
        regDetails.deceasedDetails.fold("")(_.name)))
    }
  }

  "estateOverviewDisplayMode" must {
    "return 1 when only assets, gifts and debts are filled in (base)" in {
      val ad = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts
      PdfFormatter.estateOverviewDisplayMode(ad) mustBe 1
    }

    "return 2 when in addition to base Tnrb completed and exemptions locked" in {
      val ad =
        CommonBuilder.buildExemptionsWithNoValues(
          completeTnrbIncreaseThreshold(CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts) copy (
            hasSeenExemptionGuidance = Some(false)
            )
        )
      PdfFormatter.estateOverviewDisplayMode(ad) mustBe 2
    }

    "return 3 when in addition to base Tnrb started but not completed and exemptions unlocked but zero" in {
      val ad =
        CommonBuilder.buildExemptionsWithNoValues(
          incompleteTnrbIncreaseThreshold(CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts) copy (
            hasSeenExemptionGuidance = Some(true)
            )
        )
      PdfFormatter.estateOverviewDisplayMode(ad) mustBe 3
    }

    "return 3 when in addition to base Tnrb not increased and exemptions unlocked but zero" in {
      val ad =
        CommonBuilder.buildExemptionsWithNoValues(
          completeTnrbNotIncreaseThreshold(CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts) copy (
            hasSeenExemptionGuidance = Some(true)
            )
        )
      PdfFormatter.estateOverviewDisplayMode(ad) mustBe 3
    }

    "return 4 when in addition to base Tnrb increased and exemptions unlocked but zero" in {
      val ad =
        CommonBuilder.buildExemptionsWithNoValues(
          completeTnrbIncreaseThreshold(CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts) copy (
            hasSeenExemptionGuidance = Some(true)
            )
        )
      PdfFormatter.estateOverviewDisplayMode(ad) mustBe 4
    }

    "return 5 when in addition to base Tnrb increased and exemptions unlocked but more than zero" in {
      val ad =
        CommonBuilder.buildSomeExemptions(
          completeTnrbIncreaseThreshold(CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts) copy (
            hasSeenExemptionGuidance = Some(true)
            )
        )
      PdfFormatter.estateOverviewDisplayMode(ad) mustBe 5
    }

    "return 6 when in addition to base Tnrb started but not completed and exemptions unlocked but more than zero" in {
      val ad =
        CommonBuilder.buildSomeExemptions(
          incompleteTnrbIncreaseThreshold(CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts) copy (
            hasSeenExemptionGuidance = Some(true)
            )
        )
      PdfFormatter.estateOverviewDisplayMode(ad) mustBe 6
    }

    "return 6 when in addition to base Tnrb not increased and exemptions unlocked but more than zero" in {
      val ad =
        CommonBuilder.buildSomeExemptions(
          completeTnrbNotIncreaseThreshold(CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts) copy (
            hasSeenExemptionGuidance = Some(true)
            )
        )
      PdfFormatter.estateOverviewDisplayMode(ad) mustBe 6
    }
  }

  "combineGiftSets" must {
    "combine two sets updating with values in second set" in {
      val expectedGifts = Set(
        makeGiftWithOutExemption(111, toDate("2008-04-05")),
        makeGiftWithOutExemption(5000, toDate("2009-04-05")),
        makeGiftWithOutExemption(222, toDate("2010-04-05")),
        makeGiftWithOutExemption(5000, toDate("2011-04-05")),
        makeGiftWithOutExemption(5000, toDate("2012-04-05")),
        makeGiftWithOutExemption(333, toDate("2013-04-05")),
        makeGiftWithOutExemption(444, toDate("2014-10-05"))
      )

      val gifts1 = Seq(
        makeGiftWithOutExemption(3000, toDate("2008-04-05")),
        makeGiftWithOutExemption(5000, toDate("2009-04-05")),
        makeGiftWithOutExemption(5000, toDate("2010-04-05")),
        makeGiftWithOutExemption(5000, toDate("2011-04-05")),
        makeGiftWithOutExemption(5000, toDate("2012-04-05")),
        makeGiftWithOutExemption(5000, toDate("2013-04-05")),
        makeGiftWithOutExemption(7000, toDate("2014-10-05"))
      )

      val gifts2 = Seq(
        makeGiftWithOutExemption(111, toDate("2008-04-05")),
        makeGiftWithOutExemption(222, toDate("2010-04-05")),
        makeGiftWithOutExemption(333, toDate("2013-04-05")),
        makeGiftWithOutExemption(444, toDate("2014-10-05"))
      )

      PdfFormatter.combineGiftSets(gifts1, gifts2).toSet mustBe expectedGifts
    }
  }

  "padGifts" must {
    "pad correctly where 7 years exactly" in {
      val dateOfDeath = new LocalDate(2016, 10, 10)

      val expectedGifts = Set(Set(
        makeGiftWithOutExemption(444, toDate("2010-04-05")),
        makeGiftWithOutExemption(555, toDate("2011-04-05")),
        makeGiftWithOutExemption(111, toDate("2012-04-05")),
        makeGiftWithOutExemption(222, toDate("2013-04-05")),
        makeGiftWithOutExemption(666, toDate("2014-04-05")),
        makeGiftWithOutExemption(333, toDate("2015-04-05")),
        makeGiftWithOutExemption(777, toDate("2016-04-05")),
        makeGiftWithOutExemption(888, toDate("2016-10-10"))
      ))

      val gifts = Set(Seq(
        makeGiftWithOutExemption(444, toDate("2010-04-05")),
        makeGiftWithOutExemption(555, toDate("2011-04-05")),
        makeGiftWithOutExemption(111, toDate("2012-04-05")),
        makeGiftWithOutExemption(222, toDate("2013-04-05")),
        makeGiftWithOutExemption(666, toDate("2014-04-05")),
        makeGiftWithOutExemption(333, toDate("2015-04-05")),
        makeGiftWithOutExemption(777, toDate("2016-04-05")),
        makeGiftWithOutExemption(888, toDate("2016-10-10"))
      ))
      val result = gifts.map(setGifts => PdfFormatter.padGifts(setGifts, dateOfDeath).toSet)

      result mustBe expectedGifts
    }

    "pad correctly where < 7 years" in {
      val dateOfDeath = new LocalDate(2016, 10, 10)

      val expectedGifts = Set(Set(
        makeGiftWithOutExemption(444, toDate("2010-04-05")),
        makeGiftWithOutExemption(555, toDate("2011-04-05")),
        makeGiftWithOutExemption(0, toDate("2012-04-05")),
        makeGiftWithOutExemption(0, toDate("2013-04-05")),
        makeGiftWithOutExemption(666, toDate("2014-04-05")),
        makeGiftWithOutExemption(0, toDate("2015-04-05")),
        makeGiftWithOutExemption(777, toDate("2016-04-05")),
        makeGiftWithOutExemption(888, toDate("2016-10-10"))
      ))

      val gifts = Set(Seq(
        makeGiftWithOutExemption(444, toDate("2010-04-05")),
        makeGiftWithOutExemption(555, toDate("2011-04-05")),
        makeGiftWithOutExemption(666, toDate("2014-04-05")),
        makeGiftWithOutExemption(777, toDate("2016-04-05")),
        makeGiftWithOutExemption(888, toDate("2016-10-10"))
      ))
      val result = gifts.map(setGifts => PdfFormatter.padGifts(setGifts, dateOfDeath).toSet)

      result mustBe expectedGifts
    }
  }

  "estateOverviewDisplayModeForPostPdf" must {
    "return exemptions mode when exemptions value is greater than 0" in {

      val estateExemptions1 = CommonBuilder.buildEstateExemptions.copy(exemptionType = Some("Spouse"),
        overrideValue = Some(BigDecimal(5000)))
      val freeEstate = CommonBuilder.buildFreeEstate.copy(estateExemptions = Some(Seq(estateExemptions1)))
      val ihtReturnWithPositiveExemptions = CommonBuilder.buildIHTReturn.copy(freeEstate = Some(freeEstate))

      PdfFormatter.estateOverviewDisplayModeForPostPdf(ihtReturnWithPositiveExemptions) mustBe "exemption"

    }

    "return exemptions mode when total exemptions value is 0" in {
      val ihtReturnWithNoExemptions = CommonBuilder.buildIHTReturn
      PdfFormatter.estateOverviewDisplayModeForPostPdf(ihtReturnWithNoExemptions) mustBe "noExemption"
    }
  }

  private def createShareableBasicEstateElementNoShared(value: BigDecimal) =
    CommonBuilder.buildShareableBasicElementExtended.copy(
      value = Some(value), shareValue = None, isOwned = Some(true), isOwnedShare = Some(false)
    )

  private def createShareableBasicEstateElementSharedOnly(value: BigDecimal) =
    CommonBuilder.buildShareableBasicElementExtended.copy(
      value = None, shareValue = Some(value), isOwned = Some(false), isOwnedShare = Some(true)
    )

  private val buildAllAssetsWithAllSectionsFilled = {
    AllAssets(
      money = Some(createShareableBasicEstateElementSharedOnly(BigDecimal(2))),
      household = Some(createShareableBasicEstateElementNoShared(BigDecimal(8))),
      vehicles = None,
      privatePension = Some(CommonBuilder.buildPrivatePensionExtended.copy(isChanged = Some(false),
        value = Some(BigDecimal(7)), isOwned = Some(true))),
      stockAndShare = Some(CommonBuilder.buildStockAndShare.copy(
        valueNotListed = Some(BigDecimal(9)),
        valueListed = Some(BigDecimal(10)),
        value = None,
        isNotListed = Some(true),
        isListed = Some(true))),
      insurancePolicy = Some(CommonBuilder.buildInsurancePolicy.copy(
        isAnnuitiesBought = None,
        isInsurancePremiumsPayedForSomeoneElse = None,
        value = Some(BigDecimal(12)),
        shareValue = Some(BigDecimal(13)),
        policyInDeceasedName = Some(true),
        isJointlyOwned = Some(true),
        isInTrust = None,
        coveredByExemption = None,
        sevenYearsBefore = None,
        moreThanMaxValue = None
      )),
      businessInterest = Some(CommonBuilder.buildBasicElement.copy(value = Some(BigDecimal(14)), isOwned = Some(true))),
      nominated = Some(CommonBuilder.buildBasicElement.copy(value = Some(BigDecimal(16)), isOwned = Some(true))),
      heldInTrust = Some(CommonBuilder.buildAssetsHeldInTrust.copy(isOwned = Some(true), isMoreThanOne = None, value = Some(BigDecimal(17)))),
      foreign = Some(CommonBuilder.buildBasicElement.copy(value = Some(BigDecimal(18)), isOwned = Some(true))),
      moneyOwed = Some(CommonBuilder.buildBasicElement.copy(value = Some(BigDecimal(15)), isOwned = Some(true))),
      other = Some(CommonBuilder.buildBasicElement.copy(value = Some(BigDecimal(19)), isOwned = Some(true))),
      properties = Some(Properties(isOwned = Some(true)))
    )
  }

  private val optionUkAddress = Some(UkAddress("addr1", "addr2", None, None, CommonBuilder.DefaultPostCode))

  private val propertyDeceasedHome = Property(
    id = Some("1"),
    address = optionUkAddress,
    propertyType = TestHelper.PropertyTypeDeceasedHome,
    typeOfOwnership = TestHelper.TypesOfOwnershipDeceasedOnly,
    tenure = TestHelper.TenureFreehold,
    value = Some(100)
  )

  private val propertyOtherResidentialBuilding = Property(
    id = Some("2"),
    address = optionUkAddress,
    propertyType = TestHelper.PropertyTypeOtherResidentialBuilding,
    typeOfOwnership = TestHelper.TypesOfOwnershipJoint,
    tenure = TestHelper.TenureLeasehold,
    value = Some(200)
  )

  "createApplicationDetails" must {
    "create an app details object with info for each asset stored appropriately" in {
      val expectedResult = ApplicationDetails(
        allAssets = Some(buildAllAssetsWithAllSectionsFilled),
        propertyList = List(propertyDeceasedHome, propertyOtherResidentialBuilding)
      )

      val optionSetAsset = Some(Set(
        IHTReturnTestHelper.buildJointAssetMoney,
        IHTReturnTestHelper.buildAssetHouseholdAndPersonalItems,
        IHTReturnTestHelper.buildAssetPrivatePensions,
        IHTReturnTestHelper.buildAssetStocksAndSharesListed,
        IHTReturnTestHelper.buildAssetStocksAndSharesNotListed,
        IHTReturnTestHelper.buildAssetInsurancePoliciesOwned,
        IHTReturnTestHelper.buildJointAssetInsurancePoliciesOwned,
        IHTReturnTestHelper.buildAssetBusinessInterests,
        IHTReturnTestHelper.buildAssetNominatedAssets,
        IHTReturnTestHelper.buildAssetForeignAssets,
        IHTReturnTestHelper.buildAssetMoneyOwed,
        IHTReturnTestHelper.buildAssetOther,
        IHTReturnTestHelper.buildAssetsPropertiesDeceasedsHome,
        IHTReturnTestHelper.buildAssetsPropertiesOtherResidentialBuilding
      )
      )

      val optionSetTrust = Some(IHTReturnTestHelper.buildTrusts)

      val result = createApplicationDetails(optionSetAsset, optionSetTrust)

      result.allAssets mustBe expectedResult.allAssets
      result.propertyList mustBe expectedResult.propertyList
    }
  }

  "transformAssets2" must {
    val emptyAssets = AllAssets()
    val assetsWithPropertyTag = AllAssets(properties = Some(Properties(isOwned = Some(true))))

    "set the isOwned flag to true for Other Residential Building asset (0017)" in {
      val tstAsset = Asset(assetCode = Some("0017"))
      transformAssets2(emptyAssets, tstAsset) mustBe Some(assetsWithPropertyTag)
    }

    "set the isOwned flag to true for Non-Residential asset (0018)" in {
      val tstAsset = Asset(assetCode = Some("0018"))
      transformAssets2(emptyAssets, tstAsset) mustBe Some(assetsWithPropertyTag)
    }

    "Return None if no asset code is provided" in {
      val tstAsset = Asset(assetCode = None)
      transformAssets2(emptyAssets, tstAsset) mustBe None
    }

    "Return None if an unknown asset code is provided" in {
      val tstAsset = Asset(assetCode = Some("8888"))
      transformAssets2(emptyAssets, tstAsset) mustBe None
    }
  }
}
