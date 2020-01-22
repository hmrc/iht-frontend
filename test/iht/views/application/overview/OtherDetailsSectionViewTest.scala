/*
 * Copyright 2020 HM Revenue & Customs
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
import iht.views.ViewTestHelper
import iht.views.html.application.overview.other_details_section
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig
import play.api.mvc.Call

class OtherDetailsSectionViewTest extends ViewTestHelper {

  def dummyOverviewRow = OverviewRow("debts", "", "", NotStarted, Call("", ""), "")
  val dummyOtherDetailsSection = OtherDetailsSectionViewModel(dummyOverviewRow, false, "")

  "other details section" must {

    "have no message keys in html" in {
      implicit val request = createFakeRequest()
      val view = other_details_section(dummyOtherDetailsSection).toString
      noMessageKeysShouldBePresent(view)
    }

    "have the correct title" in {
      implicit val request = createFakeRequest()
      val view = other_details_section(dummyOtherDetailsSection)
      val doc = asDocument(view)
      val assetsSection = doc.getElementById("other-details-section")
      val header = assetsSection.getElementsByTag("h2")
      header.text() mustBe messagesApi("page.iht.application.estateOverview.otherDetailsNeeded")
    }

    "contain the Debts row" in {
      implicit val request = createFakeRequest()
      val view = other_details_section(dummyOtherDetailsSection)
      val doc = asDocument(view)
      assertRenderedById(doc, "debts-row")
    }

    "show the exemptions link when asked to" in {
      implicit val request = createFakeRequest()
      val viewModel = dummyOtherDetailsSection copy (showClaimExemptionLink = true, ihtReference = "123")
      val view = other_details_section(viewModel)
      val doc = asDocument(view)
      val link = doc.getElementById("exemptions-link")
      link.text mustBe messagesApi("page.iht.application.estateOverview.claimExemptions.link")
      link.attr("href") mustBe iht.controllers.application.exemptions.routes.ExemptionsGuidanceController.onPageLoad("123").url
    }

    "not show the exemptions link when asked not to" in {
      implicit val request = createFakeRequest()
      val view = other_details_section(dummyOtherDetailsSection)
      val doc = asDocument(view)
      assertNotRenderedById(doc, "exemptions-link")
    }
  }
}
