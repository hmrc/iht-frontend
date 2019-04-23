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

package iht.viewmodels.application.overview

import iht.config.AppConfig
import iht.models.application.ApplicationDetails
import iht.models.application.basicElements.EstateElement
import iht.utils.CommonHelper
import play.api.i18n.Messages

case class AssetsAndGiftsSectionViewModel(behaveAsIncreasingTheEstateSection: Boolean,
                                          assetRow: OverviewRow,
                                          giftRow: OverviewRow,
                                          totalRow: OverviewRowWithoutLink)

object AssetsAndGiftsSectionViewModel {


  def apply(applicationDetails: ApplicationDetails, behaveAsIncreasingTheEstateSection: Boolean)
           (implicit messages: Messages, appConfig: AppConfig): AssetsAndGiftsSectionViewModel = {

    val assetsScreenreaderText = getScreenReaderQualifyingText(
      RowCompletionStatus(applicationDetails.areAllAssetsCompleted),
      messages("page.iht.application.overview.assets.screenReader.moreDetails.link"),
      messages("page.iht.application.overview.assets.screenReader.value.link"),
      messages("page.iht.application.overview.assets.screenReader.noValue.link")
    )

    val giftsScreenreaderText = getScreenReaderQualifyingText(
      RowCompletionStatus(applicationDetails.areAllAssetsCompleted),
      messages("page.iht.application.overview.gifts.screenReader.moreDetails.link"),
      messages("page.iht.application.overview.gifts.screenReader.value.link"),
      messages("page.iht.application.overview.gifts.screenReader.noValue.link")
    )

    AssetsAndGiftsSectionViewModel(
      behaveAsIncreasingTheEstateSection = behaveAsIncreasingTheEstateSection,
      assetRow = OverviewRow(appConfig.EstateAssetsID,
        messages("iht.estateReport.assets.inEstate"),
        DisplayValue(getAssetsDisplayValue(applicationDetails))(messages),
        RowCompletionStatus(applicationDetails.areAllAssetsCompleted),
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        assetsScreenreaderText)(messages),
      giftRow = OverviewRow(appConfig.EstateGiftsID,
        messages("iht.estateReport.gifts.givenAway.title"),
        DisplayValue(getGiftsDisplayValue(applicationDetails))(messages),
        RowCompletionStatus(applicationDetails.areAllGiftSectionsCompleted),
        iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad(),
        giftsScreenreaderText)(messages),
      totalRow = OverviewRowWithoutLink(
        id = "assetsGiftsTotal",
        label = messages("page.iht.application.estateOverview.valueOfAssetsAndGifts"),
        value = DisplayValue(CurrentValue(applicationDetails.totalValue))(messages),
        qualifyingText = "",
        headingLevel = "h3",
        headingClass = if (behaveAsIncreasingTheEstateSection) "visually-hidden" else ""
      )
    )
  }


  def isValueEnteredForAssets(ad:ApplicationDetails): Boolean = {
    val allAssets = ad.allAssets
    Seq[Option[EstateElement]](
      allAssets.flatMap(_.businessInterest),
      allAssets.flatMap(_.foreign),
      allAssets.flatMap(_.money),
      allAssets.flatMap(_.heldInTrust),
      allAssets.flatMap(_.household),
      allAssets.flatMap(_.insurancePolicy),
      allAssets.flatMap(_.moneyOwed),
      allAssets.flatMap(_.nominated),
      allAssets.flatMap(_.other),
      allAssets.flatMap(_.privatePension),
      allAssets.flatMap(_.stockAndShare),
      allAssets.flatMap(_.vehicles)
    ).flatten.exists(_.totalValue.isDefined) || ad.propertyList.nonEmpty
  }

  def getAssetsDisplayValue(applicationDetails: ApplicationDetails) = applicationDetails.allAssets match {
    case None => NoValueEntered
    case Some(allAssets) if allAssets.areAllAssetsSectionsAnsweredNo => AllAnsweredNo("site.noAssets")
    case Some(allAssets) if !isValueEnteredForAssets(applicationDetails) => NoValueEntered
    case _ => CurrentValue(applicationDetails.totalAssetsValue)
  }

  def getGiftsDisplayValue(applicationDetails: ApplicationDetails) = applicationDetails.allGifts match {
    case None => NoValueEntered
    case Some(allGifts) if allGifts.isGiftsSectionCompletedWithNoValue => AllAnsweredNo("page.iht.application.overview.gifts.nonGiven")
    case Some(allGifts) if !applicationDetails.isValueEnteredForPastYearsGifts => NoValueEntered
    case _ => CurrentValue(CommonHelper.getOrZero(applicationDetails.totalPastYearsGiftsOption))
  }

  def getScreenReaderQualifyingText(isComplete: RowCompletionStatus, moreDetailText: String, valueText: String, noValueText: String) =
    isComplete match {
      case NotStarted => noValueText
      case PartiallyComplete => moreDetailText
      case _ => valueText
  }

}
