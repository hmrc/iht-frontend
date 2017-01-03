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

package iht.viewmodels.application.overview

import iht.models.application.assets._
import iht.models.application.basicElements.{BasicEstateElement, ShareableBasicEstateElement}
import iht.models.application.gifts.AllGifts
import iht.testhelpers.CommonBuilder
import iht.{FakeIhtApp, TestUtils}
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

class AssetsAndGiftsSectionViewModelTest
  extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with BeforeAndAfter {

  val applicationDetails = CommonBuilder.buildApplicationDetails

  "Assets and Gifts view model" must {

    //region Assets overview row tests

    "have an id of 'assets' for the assets row" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.assetRow.id shouldBe "assets"
    }

    "have the correct caption for the assets row" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.assetRow.label shouldBe Messages("iht.estateReport.assets.inEstate")
    }

    "have a blank value for assets when there are no assets" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.assetRow.value shouldBe ""
    }

    "have a blank value for assets when there are assets but no values have been given" in {
      val appDetails = applicationDetails copy
        (allAssets = Some(AllAssets(money = Some(ShareableBasicEstateElement(None, None, None)))))
      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.assetRow.value shouldBe ""
    }

    "have the correct value with a pound sign for assets where there are some assets" in {
      val appDetails = CommonBuilder.buildApplicationDetailsWithAllAssets
      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.assetRow.value shouldBe "£54,345.00"
    }

    "have the correct text when all answers to assets questions are 'No'" in {
      val appDetails = applicationDetails.copy (allAssets = Some(buildAllAssetsAnsweredNo))

      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)
      viewModel.assetRow.value shouldBe Messages("site.noAssets")
    }

    "show View or Change when all assets are completed" in {
      val appDetails = CommonBuilder.buildApplicationDetailsWithAllAssets

      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.assetRow.linkText shouldBe Messages("iht.viewOrChange")
    }

    "show Start when no assets questions have been answered" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.assetRow.linkText shouldBe Messages("iht.start")
    }

    "show Give more details when some assets questions have been answered" in {
      val appDetails = applicationDetails copy (allAssets = Some(AllAssets(stockAndShare = Some(CommonBuilder.buildStockAndShare.copy(
        valueNotListed = Some(BigDecimal(100)),
        valueListed = Some(BigDecimal(100)),
        value = Some(BigDecimal(100)),
        isNotListed = Some(true),
        isListed = Some(true))))))

      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.assetRow.linkText shouldBe Messages("iht.giveMoreDetails")
    }

    "have the correct URL for the assets link" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.assetRow.linkUrl shouldBe iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad()
    }

    //endregion

    //region Gifts overview row tests

    "have an id of 'gifts' for the gifts row" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.giftRow.id shouldBe "gifts"
    }

    "have the correct caption for the gifts row" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.giftRow.label shouldBe Messages("iht.estateReport.gifts.givenAway.title")
    }

    "have a blank value for gifts when there are no gifts" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.giftRow.value shouldBe ""
    }

    "have a blank value for gifts when there are gifts but no values have been given" in {
      val appDetails = applicationDetails copy (allGifts = Some(CommonBuilder.buildAllGiftsWithValues),
        giftsList = Some(Seq(CommonBuilder.buildPreviousYearsGifts.copy(None, None, None, None, None))))
      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.giftRow.value shouldBe ""
    }

    "have the correct value with a pound sign for gifts where there are some gifts" in {
      val appDetails = CommonBuilder.buildSomeGifts(applicationDetails)
      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.giftRow.value shouldBe "£3,000.00"
    }

    "have the correct text when all answers to gifts questions are 'No'" in {
      val appDetails = applicationDetails copy (allGifts = Some(buildAllGiftsAnsweredNo))
      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.giftRow.value shouldBe Messages("page.iht.application.overview.gifts.nonGiven")
    }

    "show View or Change when all gifts are completed" in {
      val appDetails = CommonBuilder.buildSomeGifts(applicationDetails)

      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.giftRow.linkText shouldBe Messages("iht.viewOrChange")
    }

    "show Start when no gifts questions have been answered" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.giftRow.linkText shouldBe Messages("iht.start")
    }

    "show Give more details when some gifts questions have been answered" in {
      val allGifts = CommonBuilder.buildAllGifts copy (isReservation = Some(true))
      val appDetails = applicationDetails copy (allGifts = Some(allGifts))

      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.giftRow.linkText shouldBe Messages("iht.giveMoreDetails")
    }

    "show Give more details when only gift with reservation and gifts given away in 7 years have been answered" in {
      val giftsValues = Seq(CommonBuilder.buildPreviousYearsGifts)
      val allGifts = CommonBuilder.buildAllGifts copy (isGivenAway = Some(true))
      val appDetails = applicationDetails copy (allGifts = Some(allGifts), giftsList = Some(giftsValues))

      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.giftRow.linkText shouldBe Messages("iht.giveMoreDetails")
    }

    "have the correct URL for the gifts link when the user has not answered any gifts questions" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.giftRow.linkUrl shouldBe iht.controllers.application.gifts.guidance.routes.WhatIsAGiftController.onPageLoad()
    }

    "have the correct URL for the gifts link when the user has answered some gifts questions" in {
      val allGifts = CommonBuilder.buildAllGifts copy (isGivenAway = Some(true))
      val appDetails = applicationDetails copy (allGifts = Some(allGifts))
      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.giftRow.linkUrl shouldBe iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad()
    }

    //endregion

    //region Assets and gifts total row

    "have an id of 'AssetsGiftsRow' for the assets and gifts total" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.totalRow.id shouldBe "assetsGiftsTotal"
    }

    "have the correct caption for the assets and gifts total" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.totalRow.label shouldBe Messages("page.iht.application.estateOverview.valueOfAssetsAndGifts")
    }

    "have a blank value for assets and gifts total when there are no assets or debts" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.totalRow.value shouldBe "£0.00"
    }

    "have the correct value with a pound sign for assets and gifts total where there are some assets and gifts" in {
      val appDetails = CommonBuilder.buildSomeGifts(CommonBuilder.buildApplicationDetailsWithAllAssets)
      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.totalRow.value shouldBe "£57,345.00"
    }

    //endregion

    // TODO: Move these elsewhere if and when common builder is refactored
    lazy val buildAllAssetsAnsweredNo = AllAssets(
        money = Some(ShareableBasicEstateElement(isOwned = Some(false), isOwnedShare = Some(false), value = None, shareValue = None)),
        household = Some(ShareableBasicEstateElement(isOwned = Some(false), isOwnedShare = Some(false), value = None, shareValue = None)),
        vehicles = Some(ShareableBasicEstateElement(isOwned = Some(false), isOwnedShare = Some(false), value = None, shareValue = None)),
        privatePension = Some(PrivatePension(isOwned = Some(false), isChanged = None, value = None)),
        stockAndShare = Some(StockAndShare(isListed = Some(false), isNotListed = Some(false), value = None, valueListed = None,
          valueNotListed = None)),
        insurancePolicy = Some(InsurancePolicy(policyInDeceasedName = Some(false), isJointlyOwned = Some(false),
          isInsurancePremiumsPayedForSomeoneElse = Some(false), isAnnuitiesBought = None, value = None, shareValue = None, isInTrust = None,
          coveredByExemption = None, sevenYearsBefore = None, moreThanMaxValue = None)),
        businessInterest = Some(BasicEstateElement(isOwned = Some(false), value = None)),
        moneyOwed = Some(BasicEstateElement(isOwned = Some(false), value = None)),
        nominated = Some(BasicEstateElement(isOwned = Some(false), value = None)),
        heldInTrust = Some(HeldInTrust(isOwned = Some(false), value = None, isMoreThanOne = None)),
        foreign = Some(BasicEstateElement(isOwned = Some(false), value = None)),
        other = Some(BasicEstateElement(isOwned = Some(false), value = None)),
        properties = Some(Properties(isOwned = Some(false))))

    lazy val buildAllGiftsAnsweredNo = AllGifts(
      isGivenAway = Some(false),
      isReservation = Some(false),
      isToTrust = Some(false),
      isGivenInLast7Years = Some(false),
      action = None)
  }
}
