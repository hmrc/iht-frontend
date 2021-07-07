/*
 * Copyright 2021 HM Revenue & Customs
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

package iht.views.ihtHelpers.custom

import iht.testhelpers.TestHelper._
import iht.viewmodels.application.overview._
import iht.views.ViewTestHelper
import iht.views.html.ihtHelpers.custom.overview_item
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.config.AppConfig

class OverviewItemViewTest extends ViewTestHelper {
  lazy val overviewItemView: overview_item = app.injector.instanceOf[overview_item]

  "OverviewItem helper" must {
    "have no message keys in html" in {
      val overviewRow = OverviewRow(EstateAssetsID,
        messagesApi("iht.estateReport.assets.inEstate"),
        "",
        NotStarted,
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        "")

      val view = overviewItemView(overviewRow)
      noMessageKeysShouldBePresent(view.toString)
    }

    "show the correct label and row id" in {
      val overviewRow = OverviewRow(EstateAssetsID,
        messagesApi("iht.estateReport.assets.inEstate"),
        "",
        NotStarted,
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        "")

      val view = overviewItemView(overviewRow)
      val doc = asDocument(view)
      assertEqualsValue(doc, s"#" + EstateAssetsID + "-caption span", messagesApi("iht.estateReport.assets.inEstate"))
      assertRenderedById(doc, EstateAssetsID + "-row")
    }

    "show the correct value" in {
      val overviewRow = OverviewRow(EstateAssetsID,
        messagesApi("iht.estateReport.assets.inEstate"),
        "£2,000",
        NotStarted,
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        "")

      val view = overviewItemView(overviewRow)
      val doc = asDocument(view)
      assertEqualsValue(doc, s"#" + EstateAssetsID + "-value", "£2,000")
    }

    "show the link with correct text and status label where Item has not been started" in {
      val overviewRow = OverviewRow(EstateAssetsID,
        messagesApi("iht.estateReport.assets.inEstate"),
        "£2,000",
        NotStarted,
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        "")

      val view = overviewItemView(overviewRow)
      val doc = asDocument(view)

      val link = doc.getElementById(EstateAssetsID)
      link.text mustBe messagesApi("iht.start")
      link.attr("href") mustBe iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad().url
      assertRenderedById(doc, EstateAssetsID + "-status")
      assertContainsText(doc, messagesApi("iht.notStarted"))
    }

    "show the link with correct text and status label where Item has been started but not completed" in {
      val overviewRow = OverviewRow(EstateAssetsID,
        messagesApi("iht.estateReport.assets.inEstate"),
        "£2,000",
        PartiallyComplete,
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        "")

      val view = overviewItemView(overviewRow)
      val doc = asDocument(view)

      val link = doc.getElementById(EstateAssetsID)
      link.text mustBe messagesApi("iht.giveMoreDetails")
      link.attr("href") mustBe iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad().url
      assertRenderedById(doc, EstateAssetsID + "-status")
      assertContainsText(doc, messagesApi("iht.inComplete"))
    }

    "show the link with correct text and status label where Item has been completed" in {
      val overviewRow = OverviewRow(EstateAssetsID,
        messagesApi("iht.estateReport.assets.inEstate"),
        "£2,000",
        Complete,
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        "")

      val view = overviewItemView(overviewRow)
      val doc = asDocument(view)

      val link = doc.getElementById(EstateAssetsID)
      link.text mustBe messagesApi("iht.viewOrChange")
      link.attr("href") mustBe iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad().url
      assertRenderedById(doc, EstateAssetsID + "-status")
      assertContainsText(doc, messagesApi("iht.complete"))
    }
  }

}
