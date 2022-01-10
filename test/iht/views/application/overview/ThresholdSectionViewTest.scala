/*
 * Copyright 2022 HM Revenue & Customs
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

import iht.viewmodels.application.overview.{NotStarted, OverviewRow, OverviewRowWithoutLink, ThresholdSectionViewModel}
import iht.views.ViewTestHelper
import iht.views.html.application.overview.threshold_section
import play.api.mvc.Call

class ThresholdSectionViewTest extends ViewTestHelper {

  def dummyOverviewRow = OverviewRow("", "", "", NotStarted, Call("", ""), "")
  def dummyThresholdRow = OverviewRowWithoutLink("threshold", "", "", "")

  val thresholdSection = ThresholdSectionViewModel(
    thresholdRow = dummyThresholdRow,
    increasingThresholdRow = None,
    showIncreaseThresholdLink = false,
    thresholdIncreased = false
  )
  lazy val thresholdSectionView: threshold_section = app.injector.instanceOf[threshold_section]

  "threshold section" must {

    "have no message keys in html" in {
      implicit val request = createFakeRequest()
      val view = thresholdSectionView(thresholdSection).toString
      noMessageKeysShouldBePresent(view)
    }

    "contain the threshold row" in {
      implicit val request = createFakeRequest()

      val view = thresholdSectionView(thresholdSection).toString
      val doc = asDocument(view)
      assertRenderedById(doc, "threshold-row")
    }

    "contain the increasing the threshold row when it is supplied" in {
      implicit val request = createFakeRequest()

      val viewModel = thresholdSection copy (
        increasingThresholdRow = Some(OverviewRow("increasing-threshold", "", "", NotStarted, Call("", ""), "")))

      val view = thresholdSectionView(viewModel).toString
      val doc = asDocument(view)
      assertRenderedById(doc, "increasing-threshold-row")
    }

    "not contain the TNRB link when not asked to" in {
      implicit val request = createFakeRequest()

      val view = thresholdSectionView(thresholdSection).toString
      val doc = asDocument(view)
      assertNotRenderedById(doc, "tnrb-link")
    }

    "contain the TNRB link when asked to" in {
      implicit val request = createFakeRequest()

      val viewModel = thresholdSection copy (showIncreaseThresholdLink = true)

      val view = thresholdSectionView(viewModel).toString
      val doc = asDocument(view)
      assertRenderedById(doc, "tnrb-link")
      val link = doc.getElementById("tnrb-link")
      link.text mustBe messagesApi("page.iht.application.estateOverview.increaseThreshold.link")
      link.attr("href") mustBe iht.controllers.application.tnrb.routes.TnrbGuidanceController.onPageLoad().url
    }

    "show the threshold row as a normal row when threshold has not been increased" in {
      implicit val request = createFakeRequest()
      val view = thresholdSectionView(thresholdSection).toString
      val doc = asDocument(view)
      val thresholdRow = doc.getElementById("threshold-row")
      thresholdRow.attr("data--iht-role") must not include "ledger"
    }

    "show the threshold row as a grand total when the threshold has been increased" in {
      implicit val request = createFakeRequest()

      val viewModel: ThresholdSectionViewModel = thresholdSection copy (thresholdIncreased = true)

      val view = thresholdSectionView(viewModel).toString
      val doc = asDocument(view)
      val thresholdRow = doc.getElementById("threshold-row")
      thresholdRow.attr("data--iht-role") must include("ledger")
    }
  }
}
