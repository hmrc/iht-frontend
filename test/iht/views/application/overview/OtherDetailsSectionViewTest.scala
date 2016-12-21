/*
 * Copyright 2016 HM Revenue & Customs
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

import iht.viewmodels.application.overview.{NotStarted, OtherDetailsSectionViewModel, OverviewRow}
import iht.views.HtmlSpec
import iht.views.html.application.overview.other_details_section
import iht.{FakeIhtApp, TestUtils}
import org.jsoup.select.Elements
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.play.test.UnitSpec

class OtherDetailsSectionViewTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with HtmlSpec with BeforeAndAfter {

  def dummyOverviewRow = OverviewRow("debts", "", "", NotStarted, Call("", ""), "")
  val dummyOtherDetailsSection = OtherDetailsSectionViewModel(dummyOverviewRow, false, "")

  "other details section" must {

    "have the correct title" in {
      val view = other_details_section(dummyOtherDetailsSection)
      val doc = asDocument(view)
      val assetsSection = doc.getElementById("other-details-section");
      val header = assetsSection.getElementsByTag("h2")
      header.text() shouldBe Messages("page.iht.application.estateOverview.otherDetailsNeeded")
    }

    "contain the Debts row" in {
      val view = other_details_section(dummyOtherDetailsSection)
      val doc = asDocument(view)
      assertRenderedById(doc, "debts-row")
    }

    "show the exemptions link when asked to" in {
      val viewModel = dummyOtherDetailsSection copy (showClaimExemptionLink = true, ihtReference = "123")
      val view = other_details_section(viewModel)
      val doc = asDocument(view)
      val link = doc.getElementById("exemptions-link")
      link.text shouldBe Messages("page.iht.application.estateOverview.claimExemptions.link")
      link.attr("href") shouldBe iht.controllers.application.exemptions.routes.ExemptionsGuidanceController.onPageLoad("123").url
    }

    "not show the exemptions link when asked not to" in {
      val view = other_details_section(dummyOtherDetailsSection)
      val doc = asDocument(view)
      assertNotRenderedById(doc, "exemptions-link")
    }
  }
}
