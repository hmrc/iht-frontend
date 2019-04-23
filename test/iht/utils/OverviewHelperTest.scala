/*
 * Copyright 2019 HM Revenue & Customs
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
import iht.models.application.assets.{AllAssets, InsurancePolicy}
import iht.models.application.debts.{AllLiabilities, BasicEstateElementLiabilities}
import iht.testhelpers._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.{Lang, Messages}
import play.api.mvc.MessagesControllerComponents

class OverviewHelperTest extends FakeIhtApp with MockitoSugar {
  val mockControllerComponents: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val messages: Messages = mockControllerComponents.messagesApi.preferred(Seq(Lang.defaultLang)).messages

  val allAssetsAllFilled: AllAssets = CommonBuilder.buildAllAssetsWithAllSectionsFilled copy(
    insurancePolicy = Some(InsurancePolicy(policyInDeceasedName = Some(false), isJointlyOwned = Some(false),
    isInsurancePremiumsPayedForSomeoneElse = Some(false), isAnnuitiesBought = None,
      value = Some(BigDecimal(55.44)), shareValue = Some(BigDecimal(66.7)), isInTrust = None,
    coveredByExemption = None, sevenYearsBefore = None, moreThanMaxValue = None))
  )

  val allLiabilitiesAllFilled: AllLiabilities = CommonBuilder.buildAllLiabilitiesWithAllSectionsFilled

  "displayValue" must {

    "display total for properties" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        propertyList = CommonBuilder.buildPropertyList)

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionProperties, Some(true), Some(""))

      result mustBe "£24,690.00"
    }

    "display total for money" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(allAssetsAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionMoney, Some(true), Some(""))

      result mustBe "£200.00"
    }

    "display total for household" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(allAssetsAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionHousehold, Some(true), Some(""))

      result mustBe "£200.00"
    }

    "display total for private pension" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(allAssetsAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionPrivatePension, Some(true), Some(""))

      result mustBe "£100.00"
    }

    "display total for stocks and shares" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(allAssetsAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionStockAndShare, Some(true), Some(""))

      result mustBe "£200.00"
    }

    "display total for insurance policies" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(allAssetsAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionInsurancePolicy, Some(true), Some(""))

      result mustBe "£122.14"
    }

    "display total for business interests" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(allAssetsAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionBusinessInterest, Some(true), Some(""))

      result mustBe "£100.00"
    }

    "display total for vehicles" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(allAssetsAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionVehicles, Some(true), Some(""))

      result mustBe "£200.00"
    }

    "display total for nominated" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(allAssetsAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionNominated, Some(true), Some(""))

      result mustBe "£100.00"
    }

    "display total for held in trust" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(allAssetsAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionHeldInTrust, Some(true), Some(""))

      result mustBe "£100.00"
    }

    "display total for foreign" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(allAssetsAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionForeign, Some(true), Some(""))

      result mustBe "£100.00"
    }

    "display total for money owed" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(allAssetsAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionMoneyOwed, Some(true), Some(""))

      result mustBe "£100.00"
    }

    "display total for other" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(allAssetsAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionOther, Some(true), Some(""))

      result mustBe "£100.00"
    }

    "display total for mortgages" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        propertyList = CommonBuilder.buildPropertyList,
        allLiabilities = Some(allLiabilitiesAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionMortgages, Some(true), Some(""))

      result mustBe "£7,000.00"
    }

    "display total for funeral expenses" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = Some(allLiabilitiesAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionFuneralExpenses, Some(true), Some(""))

      result mustBe "£4,200.00"
    }

    "display total for debts owed from trust" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = Some(allLiabilitiesAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionDebtsOwedFromTrust, Some(true), Some(""))

      result mustBe "£1,200.00"
    }

    "display total for debts outside uk" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = Some(allLiabilitiesAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionDebtsOwedToAnyoneOutsideUK, Some(true), Some(""))

      result mustBe "£3,000.00"
    }

    "display total for joint assets" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = Some(allLiabilitiesAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionDebtsOwedOnJointAssets, Some(true), Some(""))

      result mustBe "£1,000.00"
    }

    "display total for debts other" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = Some(allLiabilitiesAllFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionDebtsOther, Some(true), Some(""))

      result mustBe "£1,000.00"
    }

    "display total for charities" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        charities = Seq(CommonBuilder.charity, CommonBuilder.charity2)
      )

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionExemptionsCharityValue, Some(true), Some(""))

      result mustBe "£89.89"
    }

    "returns total assets value when all tests are completed" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateAssets, Some(true), Some(""))

      result mustBe "£1,500.00"
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

      result mustBe "£1,000.00"

    }

    "returns total assets value when all assets are not complete" in {
      val allAssetsIncomplete = CommonBuilder.buildAllAssetsWithAllSectionsFilled copy (
        other = Some(CommonBuilder.buildBasicElement.copy(isOwned = Some(false), value = None)))

      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = Some(allAssetsIncomplete))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateAssets, Some(false), Some(""))

      result mustBe "£1,400.00"
    }

    "returns blank value when there is no assets and not complete " in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = None)

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateAssets, Some(false), Some(""))

      result mustBe empty
    }

    "returns total assets value if assets not started" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allAssets = None)
      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateAssets, None, Some(""))
      result mustBe empty
    }

    "returns total debts value when all debts are completed" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = Some(CommonBuilder.buildAllLiabilitiesWithAllSectionsFilled))

      val result = OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateDebts, Some(true), Some(""))

      result mustBe "£17,400.00"
    }

    "returns total debts value when value is entered for one of the section" in {
      val allLiabilities = CommonBuilder.buildAllLiabilities copy (
        funeralExpenses = Some(BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(44.3)))))

      val ad = CommonBuilder.buildApplicationDetails.copy(
        allLiabilities = Some(allLiabilities),
        status = TestHelper.AppStatusInProgress
      )

      OverviewHelper.displayValue(ad, Constants.AppSectionEstateDebts, Some(false), Some("")) mustBe "£44.30"

    }

    "returns total debts value when all debts are not complete" in {
      val tempLiabilities = CommonBuilder.buildAllLiabilitiesWithAllSectionsFilled copy (
        funeralExpenses = Some(BasicEstateElementLiabilities(isOwned = Some(true), value = None))
        )
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = Some(tempLiabilities))

      OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateDebts, Some(false), Some("")) mustBe "£13,200.00"
    }

    "returns blank value when there is no debts and not complete " in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = None)

      OverviewHelper.displayValue(appDetails, Constants.AppSectionEstateDebts, Some(false), Some("")) mustBe empty
    }

    "returns total debts value if debts not started" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = None)
      OverviewHelper.displayValue(appDetails, Constants.AppSectionEstateDebts, None, Some("")) mustBe empty
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

      result mustBe "£2,000.00"
    }

    "returns total gifts value when value is only entered for one of the section" in {
      val giftsForSevenYearsList = List(
        CommonBuilder.buildPreviousYearsGifts.copy(yearId = Some("1"), value = Some(BigDecimal(1000))))

      val appDetails = CommonBuilder.buildApplicationDetails copy(
        allGifts = Some(CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true),
          isGivenInLast7Years = Some(false))),
        giftsList = Some(giftsForSevenYearsList))

      OverviewHelper.displayValue(appDetails, Constants.AppSectionEstateGifts,
        Some(false), Some("")) mustBe "£1,000.00"

    }

    "returns blank value when there is no gifts and not complete " in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allGifts = None)

      OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateGifts, Some(false), Some("")) mustBe empty
    }

    "returns total gifts value if gifts not started" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allGifts = None)
      OverviewHelper.displayValue(appDetails,
        Constants.AppSectionEstateGifts, None, Some("")) mustBe empty
    }
  }
  
}
