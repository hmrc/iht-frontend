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

import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.viewmodels.application.overview._
import iht.views.ViewTestHelper
import iht.views.html.application.overview.{estate_overview, overview_sidebar}
import org.joda.time.LocalDate
import org.jsoup.nodes.Element
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Call
import iht.testhelpers.TestHelper._

class EstateOverviewViewTest extends ViewTestHelper{

  val dummyOverviewRow = OverviewRow("", "", "", NotStarted, Call("", ""), "")
  val dummyTotalRow = OverviewRowWithoutLink("", "", "", "")
  val dummyOverviewRowWithoutLink = OverviewRowWithoutLink("", "", "", "", false)
  val dummyDeclarationSectionViewModel = DeclarationSectionViewModel(
    ihtReference = "ABC123", declarationSectionStatus = Declarable)

  val dummyAssetsAndGiftsSection = AssetsAndGiftsSectionViewModel(
    behaveAsIncreasingTheEstateSection = false,
    assetRow = dummyOverviewRow,
    giftRow = dummyOverviewRow,
    totalRow = dummyTotalRow
  )

  val dummyReducingEstateValueSection = ReducingEstateValueSectionViewModel(
    debtRow = None,
    exemptionRow = dummyOverviewRow,
    totalRow = dummyTotalRow
  )

  val dummyOtherDetailsSection = OtherDetailsSectionViewModel(
    debtRow = dummyOverviewRow,
    showClaimExemptionLink = false,
    ihtReference = "ABC123"
  )

  val dummyThresholdSection = ThresholdSectionViewModel(dummyOverviewRowWithoutLink, None, false, false)

  val dummyViewModel = EstateOverviewViewModel(
    ihtReference = "ABC123",
    deceasedName = CommonBuilder.DefaultName,
    submissionDeadline = "01 Apr 2016",
    assetsAndGiftsSection = dummyAssetsAndGiftsSection,
    reducingEstateValueSection = None,
    otherDetailsSection = None,
    thresholdSection = dummyThresholdSection,
    grandTotalRow = None,
    declarationSection = dummyDeclarationSectionViewModel,
    increasingThresholdRow = Some(dummyOverviewRow),
    submissionMonthsLeft = 5
  )

  "Estate overview view" must {

    "have no message keys in html" in {
      implicit val request = createFakeRequest()

      val view = estate_overview(dummyViewModel).toString
      noMessageKeysShouldBePresent(view)
    }

    "contain the correct guidance text" in {
      implicit val request = createFakeRequest()

      val view = estate_overview(dummyViewModel).toString
      val doc = asDocument(view)
      view should include(messagesApi("page.iht.application.estateOverview.declaration.allSectionsNotComplete.guidance.text2"))
    }

    "contain the assets and gifts section" in {
      implicit val request = createFakeRequest()

      val view = estate_overview(dummyViewModel).toString
      val doc = asDocument(view)
      assertRenderedById(doc, "assets-gifts-section")
    }

    "contain a sidebar with the deadline date" in {
      implicit val request = createFakeRequest()

      val view = estate_overview(dummyViewModel).toString
      view should include(messagesApi("page.iht.application.overview.timeScale.guidance"))
      view should include("01 Apr 2016")
    }

    "contain a sidebar with the 'Go to your Inheritance Tax estate reports' link" in {
      implicit val request = createFakeRequest()

      val view = estate_overview(dummyViewModel).toString
      view should include(messagesApi("iht.estateReport.goToEstateReports"))
    }

    "show the correct 'Go to your Inheritance Tax estate reports' link" in {
      implicit val request = createFakeRequest()
      val expectedUrl = "/inheritance-tax/estate-report"
      val view = overview_sidebar("", 5).toString
      val doc = asDocument(view)
      assertRenderedById(doc, "return-to-estate-report-link")
      val link: Element = doc.getElementById("return-to-estate-report-link")
      link.text() shouldBe messagesApi("iht.estateReport.goToEstateReports")
      link.attr("href") shouldBe expectedUrl
    }

    "contain the threshold section" in {
      implicit val request = createFakeRequest()

      val view = estate_overview(dummyViewModel).toString
      val doc = asDocument(view)
      assertRenderedById(doc, "threshold-section")
    }

    "contain the other details section when given one" in {
      implicit val request = createFakeRequest()

      val viewModel = dummyViewModel copy (otherDetailsSection = Some(dummyOtherDetailsSection))

      val view = estate_overview(viewModel).toString
      val doc = asDocument(view)
      assertRenderedById(doc, "other-details-section")
    }

    "not contain the other details section when not given one" in {
      implicit val request = createFakeRequest()

      val view = estate_overview(dummyViewModel).toString
      val doc = asDocument(view)
      assertNotRenderedById(doc, "other-details-section")
    }

    "contain the reducing the estate section when given one" in {
      implicit val request = createFakeRequest()

      val viewModel = dummyViewModel copy (reducingEstateValueSection = Some(dummyReducingEstateValueSection))

      val view = estate_overview(viewModel).toString
      val doc = asDocument(view)
      assertRenderedById(doc, "reducing-estate-value-section")
    }

    "not contain the reducing the estate section when not given one" in {
      implicit val request = createFakeRequest()

      val view = estate_overview(dummyViewModel).toString
      val doc = asDocument(view)
      assertNotRenderedById(doc, "reducing-estate-value-section")
    }

    "contain the grand total row when given one" in {
      implicit val request = createFakeRequest()

      val viewModel = dummyViewModel copy (
        reducingEstateValueSection = Some(dummyReducingEstateValueSection),
        grandTotalRow = Some(OverviewRowWithoutLink("grand-total", "", "", "")))
      val view = estate_overview(viewModel).toString
      val doc = asDocument(view)
      assertRenderedById(doc, "grand-total-row")
    }

    "not contain the grand total when not given one" in {
      implicit val request = createFakeRequest()

      val view = estate_overview(dummyViewModel).toString
      val doc = asDocument(view)
      assertNotRenderedById(doc, "grand-total")
    }

    "contain the Debts row if there are debts in the model and show as non negative value " +
      "if there are no exemptions" in {
      implicit val request = createFakeRequest()

      val appDetails = CommonBuilder.buildApplicationDetails
      val regDetails = CommonBuilder.buildRegistrationDetails1

      val applicationDetails = appDetails copy (allLiabilities = Some(CommonBuilder.buildEveryLiability))
      val estateOverviewViewModel = EstateOverviewViewModel(regDetails, applicationDetails, new LocalDate(2016, 4, 1))

      val view = estate_overview(estateOverviewViewModel)
      val doc = asDocument(view)

      assertRenderedById(doc, EstateDebtsID + "-row")
      assertRenderedById(doc, EstateDebtsID + "-value")
      assertEqualsValue(doc, "div#" + EstateDebtsID + "-value", "£500.00")

    }

    "contain the Debts row if there are debts in the model and show as negative value " +
      "if there are some exemptions values present" in {
      implicit val request = createFakeRequest()
      val appDetails = CommonBuilder.buildApplicationDetails
      val regDetails = CommonBuilder.buildRegistrationDetails1

      val exemptions = Some(CommonBuilder.buildAllExemptions.copy(partner = Some(CommonBuilder.buildPartnerExemption)))

      val applicationDetails = appDetails copy (allLiabilities = Some(CommonBuilder.buildEveryLiability),
                                                allExemptions = exemptions)
      val estateOverviewViewModel = EstateOverviewViewModel(regDetails, applicationDetails, new LocalDate(2016, 4, 1))

      val view = estate_overview(estateOverviewViewModel)
      val doc = asDocument(view)

      assertRenderedById(doc, EstateDebtsID + "-row")
      assertRenderedById(doc, EstateDebtsID + "-value")
      assertEqualsValue(doc, "div#" + EstateDebtsID + "-value", "-£500.00")

    }

    "contain the declaration section" in {
      implicit val request = createFakeRequest()

      val viewModel = dummyViewModel

      val view = estate_overview(viewModel).toString
      val doc = asDocument(view)

      assertEqualsValue(doc, "a#continue-to-declaration", messagesApi("iht.continue"))
      assertEqualsValue(doc, "p#declarable-guidance",
        messagesApi("page.iht.application.estateOverview.declaration.allSectionsComplete.guidance.text"))

    }

  }
}
