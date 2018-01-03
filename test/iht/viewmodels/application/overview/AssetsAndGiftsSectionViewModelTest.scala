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

package iht.viewmodels.application.overview

import iht.models.application.assets._
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper._
import iht.{FakeIhtApp, TestUtils}
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.test.UnitSpec

class AssetsAndGiftsSectionViewModelTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with BeforeAndAfter {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  val applicationDetails = CommonBuilder.buildApplicationDetails

  val emptyApplicationDetails = CommonBuilder.buildApplicationDetails
  val ukAddress = Some(CommonBuilder.DefaultUkAddress)

  private def propertyValue(value: BigDecimal) = Some(value)

  "Assets and Gifts view model" must {

    //region Assets overview row tests

    "have an id of 'assets' for the assets row" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.assetRow.id shouldBe EstateAssetsID
    }

    "have the correct caption for the assets row" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.assetRow.label shouldBe messagesApi("iht.estateReport.assets.inEstate")
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
      val appDetails = applicationDetails.copy(allAssets = Some(CommonBuilder.buildAllAssetsAnsweredNo))

      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)
      viewModel.assetRow.value shouldBe messagesApi("site.noAssets")
    }

    "show View or Change when all assets are completed" in {
      val appDetails = CommonBuilder.buildApplicationDetailsWithAllAssets

      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.assetRow.linkText shouldBe messagesApi("iht.viewOrChange")
    }

    "show Start when no assets questions have been answered" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.assetRow.linkText shouldBe messagesApi("iht.start")
    }

    "show Give more details when some assets questions have been answered" in {
      val appDetails = applicationDetails copy (allAssets = Some(AllAssets(stockAndShare = Some(CommonBuilder.buildStockAndShare.copy(
        valueNotListed = Some(BigDecimal(100)),
        valueListed = Some(BigDecimal(100)),
        value = Some(BigDecimal(100)),
        isNotListed = Some(true),
        isListed = Some(true))))))

      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.assetRow.linkText shouldBe messagesApi("iht.giveMoreDetails")
    }

    "have the correct URL for the assets link" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.assetRow.linkUrl shouldBe iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad()
    }

    //endregion

    //region Gifts overview row tests

    "have an id of 'gifts' for the gifts row" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.giftRow.id shouldBe EstateGiftsID
    }

    "have the correct caption for the gifts row" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.giftRow.label shouldBe messagesApi("iht.estateReport.gifts.givenAway.title")
    }

    "have a blank value for gifts when there are no gifts" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.giftRow.value shouldBe ""
    }

    "have a blank value for gifts when there are gifts but no values have been given" in {
      val appDetails = applicationDetails copy(allGifts = Some(CommonBuilder.buildAllGiftsWithValues),
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
      val allGifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(false),
        isReservation = Some(false),
        isToTrust = Some(false),
        isGivenInLast7Years = Some(false),
        action = None)

      val appDetails = applicationDetails copy (allGifts = Some(allGifts))
      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.giftRow.value shouldBe messagesApi("page.iht.application.overview.gifts.nonGiven")
    }

    "show View or Change when all gifts are completed" in {
      val appDetails = CommonBuilder.buildSomeGifts(applicationDetails)

      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.giftRow.linkText shouldBe messagesApi("iht.viewOrChange")
    }

    "show Start when no gifts questions have been answered" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.giftRow.linkText shouldBe messagesApi("iht.start")
    }

    "show Give more details when some gifts questions have been answered" in {
      val allGifts = CommonBuilder.buildAllGifts copy (isReservation = Some(true))
      val appDetails = applicationDetails copy (allGifts = Some(allGifts))

      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.giftRow.linkText shouldBe messagesApi("iht.giveMoreDetails")
    }

    "show Give more details when only gift with reservation and gifts given away in 7 years have been answered" in {
      val giftsValues = Seq(CommonBuilder.buildPreviousYearsGifts)
      val allGifts = CommonBuilder.buildAllGifts copy (isGivenAway = Some(true))
      val appDetails = applicationDetails copy(allGifts = Some(allGifts), giftsList = Some(giftsValues))

      val viewModel = AssetsAndGiftsSectionViewModel(appDetails, false)

      viewModel.giftRow.linkText shouldBe messagesApi("iht.giveMoreDetails")
    }

    "have the correct URL for the gifts link when the user has not answered any gifts questions" in {
      val viewModel = AssetsAndGiftsSectionViewModel(applicationDetails, false)

      viewModel.giftRow.linkUrl shouldBe iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad()
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

      viewModel.totalRow.label shouldBe messagesApi("page.iht.application.estateOverview.valueOfAssetsAndGifts")
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

  }

  "isValueEnteredForAssets" must {
    "return false if applicationDetails is empty" in {
      AssetsAndGiftsSectionViewModel.isValueEnteredForAssets(emptyApplicationDetails) shouldBe false
    }

    "return true if applicationDetails has a money with 0 value" in {
      val appDetails = emptyApplicationDetails.copy(allAssets = Some(AllAssets(
        money = Some(CommonBuilder.buildShareableBasicElementExtended.copy(
          value = Some(BigDecimal(0)),
          shareValue = None)
        ))))

      AssetsAndGiftsSectionViewModel.isValueEnteredForAssets(appDetails) shouldBe true
    }

    "return true if applicationDetails has a money with value other than 0" in {
      val appDetails = emptyApplicationDetails.copy(allAssets = Some(CommonBuilder.buildAllAssets.copy(
        money = Some(CommonBuilder.buildShareableBasicElementExtended.copy(
          value = Some(BigDecimal(100)),
          shareValue = None)
        ))))

      AssetsAndGiftsSectionViewModel.isValueEnteredForAssets(appDetails) shouldBe true
    }

    "return true if applicationDetails has a property with 0 value" in {
      val appDetails = emptyApplicationDetails.copy(propertyList = List(CommonBuilder.buildProperty.copy(
        id = Some("2"),
        address = ukAddress,
        propertyType = None,
        typeOfOwnership = None,
        tenure = None,
        value = propertyValue(0)
      )))

      AssetsAndGiftsSectionViewModel.isValueEnteredForAssets(appDetails) shouldBe true
    }

    "return true if applicationDetails has a property with value other than 0" in {
      val appDetails = emptyApplicationDetails.copy(propertyList = List(CommonBuilder.buildProperty.copy(
        id = Some("2"),
        address = ukAddress,
        propertyType = None,
        typeOfOwnership = None,
        tenure = None,
        value = propertyValue(7500)
      )))

      AssetsAndGiftsSectionViewModel.isValueEnteredForAssets(appDetails) shouldBe true
    }
  }
}
