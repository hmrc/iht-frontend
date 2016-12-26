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

package iht.views.ihtHelpers

import iht.FakeIhtApp
import iht.utils.CommonHelper
import iht.viewmodels.application.overview._
import iht.views.HtmlSpec
import iht.views.html.ihtHelpers.overview_item
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

class OverviewItemViewTest extends UnitSpec with FakeIhtApp with HtmlSpec {
  "OverviewItem helper" must {
    "show the correct label" in {
      val overviewRow = OverviewRow("assets",
        Messages("iht.estateReport.assets.inEstate"),
        "",
        NotStarted,
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        "")

      val view = overview_item(overviewRow)
      val doc = asDocument(view)
      assertEqualsValue(doc, "div#assets-caption", Messages("iht.estateReport.assets.inEstate"))
    }

    "show the correct value" in {
      val overviewRow = OverviewRow("assets",
        Messages("iht.estateReport.assets.inEstate"),
        "£2,000",
        NotStarted,
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        "")

      val view = overview_item(overviewRow)
      val doc = asDocument(view)
      assertEqualsValue(doc, "div#assets-value", CommonHelper.escapePound("£2,000"))
    }

    "show the link with correct text and status label where Item has not been started" in {
      val overviewRow = OverviewRow("assets",
        Messages("iht.estateReport.assets.inEstate"),
        "£2,000",
        NotStarted,
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        "")

      val view = overview_item(overviewRow)
      val doc = asDocument(view)

      val link = doc.getElementById("assets-link-text")
      link.text shouldBe Messages("iht.start")
      link.attr("href") shouldBe iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad().url
      assertRenderedById(doc, "assets-status")
      assertContainsText(doc, Messages("iht.notStarted"))
    }

    "show the link with correct text and status label where Item has been started but not completed" in {
      val overviewRow = OverviewRow("assets",
        Messages("iht.estateReport.assets.inEstate"),
        "£2,000",
        PartiallyComplete,
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        "")

      val view = overview_item(overviewRow)
      val doc = asDocument(view)

      val link = doc.getElementById("assets-link-text")
      link.text shouldBe Messages("iht.giveMoreDetails")
      link.attr("href") shouldBe iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad().url
      assertRenderedById(doc, "assets-status")
      assertContainsText(doc, Messages("iht.inComplete"))
    }

    "show the link with correct text and status label where Item has been completed" in {
      val overviewRow = OverviewRow("assets",
        Messages("iht.estateReport.assets.inEstate"),
        "£2,000",
        Complete,
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        "")

      val view = overview_item(overviewRow)
      val doc = asDocument(view)

      val link = doc.getElementById("assets-link-text")
      link.text shouldBe Messages("iht.viewOrChange")
      link.attr("href") shouldBe iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad().url
      assertRenderedById(doc, "assets-status")
      assertContainsText(doc, Messages("iht.complete"))
    }


  }
}
