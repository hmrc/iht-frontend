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

import iht.models.application.ApplicationDetails
import play.api.i18n.{Lang, Messages}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.constants.IhtProperties._
import play.api.Application

case class AssetsAndGiftsSectionViewModel(behaveAsIncreasingTheEstateSection: Boolean,
                                          assetRow: OverviewRow,
                                          giftRow: OverviewRow,
                                          totalRow: OverviewRowWithoutLink)

object AssetsAndGiftsSectionViewModel {


  def apply(applicationDetails: ApplicationDetails, behaveAsIncreasingTheEstateSection: Boolean)
           (implicit messages: Messages): AssetsAndGiftsSectionViewModel = {

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
      assetRow = OverviewRow(EstateAssetsID,
        messages("iht.estateReport.assets.inEstate"),
        DisplayValue(getAssetsDisplayValue(applicationDetails))(messages),
        RowCompletionStatus(applicationDetails.areAllAssetsCompleted),
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        assetsScreenreaderText)(messages),
      giftRow = OverviewRow(EstateGiftsID,
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

  def getAssetsDisplayValue(applicationDetails: ApplicationDetails) = applicationDetails.allAssets match {
    case None => NoValueEntered
    case Some(allAssets) if allAssets.areAllAssetsSectionsAnsweredNo => AllAnsweredNo("site.noAssets")
    case Some(allAssets) if !applicationDetails.isValueEnteredForAssets => NoValueEntered
    case _ => CurrentValue(applicationDetails.totalAssetsValue)
  }

  def getGiftsDisplayValue(applicationDetails: ApplicationDetails) = applicationDetails.allGifts match {
    case None => NoValueEntered
    case Some(allGifts) if allGifts.isGiftsSectionCompletedWithNoValue => AllAnsweredNo("page.iht.application.overview.gifts.nonGiven")
    case Some(allGifts) if !applicationDetails.isValueEnteredForPastYearsGifts => NoValueEntered
    case _ => CurrentValue(applicationDetails.totalGiftsValue)
  }

  def getScreenReaderQualifyingText(isComplete: RowCompletionStatus, moreDetailText: String, valueText: String, noValueText: String) =
    isComplete match {
      case NotStarted => noValueText
      case PartiallyComplete => moreDetailText
      case _ => valueText
  }

}
