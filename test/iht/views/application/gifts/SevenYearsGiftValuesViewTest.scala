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

package iht.views.application.gifts

import iht.models.application.gifts.PreviousYearsGifts
import iht.testhelpers.CommonBuilder
import iht.views.html.application.gift.seven_years_gift_values
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class SevenYearsGiftValuesViewTest extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidanceParagraphs = Set(
    Messages("page.iht.application.gifts.sevenYears.values.guidance")
  )

  override def pageTitle = Messages("iht.estateReport.gifts.valueOfGiftsGivenAway")

  override def browserTitle = Messages("page.iht.application.gifts.sevenYears.values.browserTitle")

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad(),
      Messages("page.iht.application.gifts.return.to.givenAwayBy", deceasedName)
    )
  )

  override def view = {
    val giftsList = Seq(
      PreviousYearsGifts(Some("1"), Some(1000.00), Some(33), Some("6 April 2014"), Some("12 December 2014")),
      PreviousYearsGifts(Some("2"), Some(1001.00), Some(44), Some("6 April 2013"), Some("5 April 2013")),
      PreviousYearsGifts(Some("3"), Some(1002.00), Some(55), Some("6 April 2012"), Some("5 April 2012"))
    )
    seven_years_gift_values(
      giftsList = giftsList,
      registrationDetails = registrationDetails,
      BigDecimal(100),
      BigDecimal(200),
      BigDecimal(300),
      previousYearsGiftsExemptionsIsDefined = true, previousYearsGiftsValueIsDefined = true).toString()
  }

  val giftsTableId = "gifts-list-table"

  override def tableCell(doc:Document, tableId:String, colNo: Int, rowNo: Int) = {
    val propertiesUl = doc.getElementById(tableId)
    val listItems = propertiesUl.getElementsByTag("tr")
    listItems.get(rowNo).getElementsByTag("td").get(colNo)
  }

  def tableHeading(doc:Document, colNo: Int) = {
    val propertiesUl = doc.getElementById(giftsTableId)
    val listItems = propertiesUl.getElementsByTag("tr")
    listItems.get(0).getElementsByTag("th").get(colNo).text
  }

  val ariaContainingElement = "div"

  def valuesWithChangeLink(rowNo: Int,
                           expectedGiftsValue: String,
                           expectedExemptionsValue: String,
                           expectedAmountAdded: String,
                           isChangeLink: Boolean = true) = {
    val displayRowNo = rowNo + 1
    s"show row $displayRowNo gifts value" in {
      getVisibleText(tableCell(doc, giftsTableId, 0, rowNo), ariaContainingElement) shouldBe expectedGiftsValue
    }
    s"show row $displayRowNo exemptions value" in {
      getVisibleText(tableCell(doc, giftsTableId, 1, rowNo), ariaContainingElement) shouldBe expectedExemptionsValue
    }
    s"show row $displayRowNo amount added to estate value" in {
      getVisibleText(tableCell(doc, giftsTableId, 2, rowNo), ariaContainingElement) shouldBe expectedAmountAdded
    }
    if (isChangeLink) {
      s"show row $displayRowNo change link with correct text and target" in {
        val cellContents = tableCell(doc, giftsTableId, 3, rowNo)
        val anchor = cellContents.getElementsByTag("a").first
        getVisibleText(anchor) shouldBe Messages("iht.change")
        anchor.attr("href") shouldBe iht.controllers.application.gifts.routes.GiftsDetailsController.onPageLoad(s"$rowNo").url
      }
    }
  }

  "seven years gift values view" must {
    behave like nonSubmittablePage()

    "contain gifts value heading" in {
      tableHeading(doc, 0) shouldBe Messages("page.iht.application.gifts.lastYears.tableTitle1")
    }

    "contain exemptions value heading" in {
      tableHeading(doc, 1) shouldBe Messages("page.iht.application.gifts.lastYears.tableTitle2")
    }

    "contain amount added to estate heading" in {
      tableHeading(doc, 2) shouldBe Messages("page.iht.application.gifts.lastYears.tableTitle3")
    }

    behave like valuesWithChangeLink(1, "£1,000.00", "£33.00", "£967.00")

    behave like valuesWithChangeLink(2, "£1,001.00", "£44.00", "£957.00")

    behave like valuesWithChangeLink(3, "£1,002.00", "£55.00", "£947.00")

    behave like valuesWithChangeLink(4, "£100.00", "£300.00", "£200.00", isChangeLink = false)

    //    behave like link("add-property",
//      iht.controllers.application.assets.properties.routes.PropertyDetailsOverviewController.onPageLoad().url,
//      Messages("iht.estateReport.assets.propertyAdd"))
//
//    "show ownership question" in {
//      elementShouldHaveText(doc, "home-in-uk-question", Messages("page.iht.application.assets.properties.question.question", deceasedName))
//    }
//
//    "show ownership question value" in {
//      elementShouldHaveText(doc, "home-in-uk-value", Messages("iht.yes"))
//    }
//
//    behave like link("home-in-uk-link",
//      iht.controllers.application.assets.properties.routes.PropertiesOwnedQuestionController.onPageLoad().url,
//      Messages("iht.change"))
//
//    behave like addressWithDeleteAndModify(0, formatAddressForDisplay(CommonBuilder.DefaultUkAddress))
//
//    behave like addressWithDeleteAndModify(1, formatAddressForDisplay(CommonBuilder.DefaultUkAddress2))
//
//    "show you haven't added message when there are no properties" in {
//      val view = properties_overview(List(),
//        Some(Properties(isOwned = Some(true))),
//        registrationDetails).toString()
//      val doc = asDocument(view)
//      doc.getElementById("properties-empty-table-row").text shouldBe
//        Messages("page.iht.application.assets.deceased-permanent-home.table.emptyRow.text")
//
//    }
  }
}
