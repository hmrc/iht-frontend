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
import iht.constants.Constants
import iht.models.application.debts.BasicEstateElementLiabilities
import iht.testhelpers._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class OverviewHelperTest extends UnitSpec with FakeIhtApp with MockitoSugar {

  "displayValue" must {

    "returns total assets value when all tests are completed" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateAssets, Some(true), Some(""))

      result shouldBe "£1,500.00"
    }

    "returns total assets value when value is entered for one of the section" in {
      val allAssetsWithValue = CommonBuilder.buildAllAssets copy (
        other = Some(CommonBuilder.buildBasicElement.copy(isOwned = Some(false), value = Some(BigDecimal(1000)))))

      val ad = CommonBuilder.buildApplicationDetails.copy(
        allAssets = Some(allAssetsWithValue),
        status = TestHelper.AppStatusInProgress
      )

      val result = OverviewHelper.displayValue(ad,
        Constants.AppSectionEstateAssets, Some(false), Some(""))

      result shouldBe "£1,000.00"

    }

    "returns total assets value when all assets are not complete" in {
      val allAssetsIncomplete = CommonBuilder.buildAllAssetsWithAllSectionsFilled copy (
        other = Some(CommonBuilder.buildBasicElement.copy(isOwned = Some(false), value = None)))

      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(allAssetsIncomplete))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateAssets, Some(false), Some(""))

      result shouldBe "£1,400.00"
    }

    "returns blank value when there is no assets and not complete " in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = None)

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateAssets, Some(false), Some(""))

      result shouldBe empty
    }

    "returns total assets value if assets not started" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = None)
      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateAssets, None, Some(""))
      result shouldBe empty
    }

    "returns total debts value when all debts are completed" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = Some(CommonBuilder.buildAllLiabilitiesWithAllSectionsFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateDebts, Some(true), Some(""))

      result shouldBe "£17,400.00"
    }

    "returns total debts value when value is entered for one of the section" in {
      val allLiabilities = CommonBuilder.buildAllLiabilities copy (
        funeralExpenses = Some(BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(44.3)))))

      val ad = CommonBuilder.buildApplicationDetails.copy(
        allLiabilities = Some(allLiabilities),
        status = TestHelper.AppStatusInProgress
      )

      OverviewHelper.displayValue(ad, Constants.AppSectionEstateDebts, Some(false), Some("")) shouldBe "£44.30"

    }

    "returns total debts value when all debts are not complete" in {
      val tempLiabilities = CommonBuilder.buildAllLiabilitiesWithAllSectionsFilled copy (
        funeralExpenses = Some(BasicEstateElementLiabilities(isOwned = Some(true), value = None))
        )
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = Some(tempLiabilities))

      OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateDebts, Some(false), Some("")) shouldBe "£13,200.00"
    }

    "returns blank value when there is no debts and not complete " in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = None)

      OverviewHelper.displayValue(appDetails, Constants.AppSectionEstateDebts, Some(false), Some("")) shouldBe empty
    }

    "returns total debts value if debts not started" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = None)
      OverviewHelper.displayValue(appDetails, Constants.AppSectionEstateDebts, None, Some("")) shouldBe empty
    }

    "returns total gifts value when all gifts value are entered" in {

      val giftsForSevenYearsList = List(
        CommonBuilder.buildPreviousYearsGifts.copy(yearId = Some("1"), value = Some(BigDecimal(2000))))

      val appDetails = CommonBuilder.buildApplicationDetails copy(
        allGifts = Some(CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true),
          isReservation = Some(false), isToTrust = Some(false), isGivenInLast7Years = Some(false))),
        giftsList = Some(giftsForSevenYearsList))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateGifts, Some(true), Some(""))

      result shouldBe "£2,000.00"
    }

    "returns total gifts value when value is only entered for one of the section" in {
      val giftsForSevenYearsList = List(
        CommonBuilder.buildPreviousYearsGifts.copy(yearId = Some("1"), value = Some(BigDecimal(1000))))

      val appDetails = CommonBuilder.buildApplicationDetails copy(
        allGifts = Some(CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true),
          isGivenInLast7Years = Some(false))),
        giftsList = Some(giftsForSevenYearsList))

      OverviewHelper.displayValue(appDetails, Constants.AppSectionEstateGifts,
        Some(false), Some("")) shouldBe "£1,000.00"

    }

    "returns blank value when there is no gifts and not complete " in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allGifts = None)

      OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateGifts, Some(false), Some("")) shouldBe empty
    }

    "returns total gifts value if gifts not started" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allGifts = None)
      OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateGifts, None, Some("")) shouldBe empty
    }
  }
  
}
