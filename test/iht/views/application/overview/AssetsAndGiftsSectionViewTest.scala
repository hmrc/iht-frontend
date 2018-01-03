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

package iht.views.application.overview

import iht.viewmodels.application.overview.{AssetsAndGiftsSectionViewModel, NotStarted, OverviewRow, OverviewRowWithoutLink}
import iht.views.ViewTestHelper
import iht.views.html.application.overview.assets_and_gifts_section
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Call

class AssetsAndGiftsSectionViewTest extends ViewTestHelper {

  def dummyOverviewRow = OverviewRow("", "", "", NotStarted, Call("", ""), "")
  def dummyTotalRow = OverviewRowWithoutLink("", "", "", "")

  val dummyAssetsAndGiftsSection = AssetsAndGiftsSectionViewModel(
    behaveAsIncreasingTheEstateSection = false,
    assetRow = dummyOverviewRow,
    giftRow = dummyOverviewRow,
    totalRow = dummyTotalRow
  )

  "assets and gifts section" must {

    "have no message keys in html" in {
      implicit val request = createFakeRequest()
      val viewModel: AssetsAndGiftsSectionViewModel = dummyAssetsAndGiftsSection copy (behaveAsIncreasingTheEstateSection = true)

      val view = assets_and_gifts_section(viewModel).toString
      noMessageKeysShouldBePresent(view)
    }

    "show the correct title when asked to" in {
      implicit val request = createFakeRequest()
      val viewModel: AssetsAndGiftsSectionViewModel = dummyAssetsAndGiftsSection copy (behaveAsIncreasingTheEstateSection = true)

      val view = assets_and_gifts_section(viewModel).toString
      val doc = asDocument(view)
      val header = doc.getElementsByTag("h2")
      header.text() should include(messagesApi("page.iht.application.estateOverview.totalAddedToTheEstateValue"))
    }

    "not show a title when asked not to" in {
      implicit val request = createFakeRequest()
      val viewModel = dummyAssetsAndGiftsSection copy (behaveAsIncreasingTheEstateSection = false)
      val view = assets_and_gifts_section(viewModel).toString
      val doc = asDocument(view)
      doc.getElementsByTag("h2").first.classNames contains "visually-hidden"
    }

    "contain the Assets row" in {
      implicit val request = createFakeRequest()

      val viewModel = dummyAssetsAndGiftsSection copy (
        assetRow = OverviewRow(id = "assets", label = messagesApi("iht.estateReport.assets.inEstate"),
          completionStatus = NotStarted,
          value = "",
          linkUrl = Call("Get","localhost"),
          qualifyingText = ""))

      val view = assets_and_gifts_section(viewModel).toString
      val doc = asDocument(view)
      view should include(messagesApi("iht.estateReport.assets.inEstate"))
      assertRenderedById(doc, "assets-row")
    }

    "contain the Gifts row" in {
      implicit val request = createFakeRequest()

      val viewModel = dummyAssetsAndGiftsSection copy (
        giftRow = OverviewRow(id = "gifts", label = messagesApi("iht.estateReport.gifts.givenAway.title"),
          completionStatus = NotStarted,
          value = "",
          linkUrl = Call("Get","localhost"),
          qualifyingText = "")
        )

      val view = assets_and_gifts_section(viewModel).toString
      val doc = asDocument(view)
      view should include(messagesApi("iht.estateReport.gifts.givenAway.title"))
      assertRenderedById(doc, "gifts-row")
    }

    "contain a Total row" in {
      implicit val request = createFakeRequest()

      val viewModel = dummyAssetsAndGiftsSection copy (
        totalRow = OverviewRowWithoutLink(id = "assetsGiftsTotal",
          label = "",
          value = "£1,234.56",
          qualifyingText = "")
        )

      val view = assets_and_gifts_section(viewModel).toString()
      view should include(messagesApi("£1,234.56"))
    }
  }
}
