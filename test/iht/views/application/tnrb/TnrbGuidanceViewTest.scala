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

package iht.views.application.tnrb

import iht.views.ViewTestHelper
import iht.views.html.application.tnrb.tnrb_guidance
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import play.api.i18n.Messages.Implicits._

class TnrbGuidanceViewTest extends ViewTestHelper {

  "tnrb guidance page" must {

    "have no message keys in html" in {
      implicit val request = createFakeRequest()
      val view = tnrb_guidance("ihtReference", "url", "deceased name", "iht.estateReport.tnrb.increasingIHTThreshold",
        "iht.estateReport.tnrb.increasingThreshold",
        false).toString
      noMessageKeysShouldBePresent(view)
    }

    "show the correct title on page load" in {
      implicit val request = createFakeRequest()
      val view = tnrb_guidance("ihtReference", "url", "deceased name", "iht.estateReport.tnrb.increasingIHTThreshold",
        "iht.estateReport.tnrb.increasingThreshold",
        false).toString
      val doc = asDocument(view)
      val headers: Elements = doc.getElementsByTag("h1")
      headers.size() shouldBe 1
      headers.first().text() shouldBe messagesApi("iht.estateReport.tnrb.increasingIHTThreshold")
    }

    "show the correct title on system page load" in {
      implicit val request = createFakeRequest()
      val view = tnrb_guidance("ihtReference", "url", "deceased name", "page.iht.application.tnrb.guidance.system.title",
        "page.iht.application.tnrb.guidance.system.title",
        true).toString
      val doc = asDocument(view)
      val headers: Elements = doc.getElementsByTag("h1")
      headers.size() shouldBe 1
      headers.first().text() shouldBe messagesApi("page.iht.application.tnrb.guidance.system.title")
    }

    "show the correct browser title on page load" in {
      implicit val request = createFakeRequest()
      val view = tnrb_guidance("ihtReference", "url", "deceased name", "iht.estateReport.tnrb.increasingIHTThreshold",
        "iht.estateReport.tnrb.increasingThreshold",
        false).toString
      val doc = asDocument(view)
      assertEqualsValue(doc, "title",
        messagesApi("iht.estateReport.tnrb.increasingThreshold") + " " + messagesApi("site.title.govuk"))
    }

    "show the correct browser title on system page load" in {
      implicit val request = createFakeRequest()
      val view = tnrb_guidance("ihtReference", "url", "deceased name", "page.iht.application.tnrb.guidance.system.title",
        "page.iht.application.tnrb.guidance.system.title",
        true).toString
      val doc = asDocument(view)
      assertEqualsValue(doc, "title",
        messagesApi("page.iht.application.tnrb.guidance.system.title") + " " + messagesApi("site.title.govuk"))
    }

    "show the correct paragraphs on page load" in {
      implicit val request = createFakeRequest()
      val view = tnrb_guidance("ihtReference", "url", "deceased name", "iht.estateReport.tnrb.increasingIHTThreshold",
        "iht.estateReport.tnrb.increasingThreshold",
        false).toString
      view should include(messagesApi("page.iht.application.tnrb.guidance.p1", "deceased name"))
      view should include(messagesApi("page.iht.application.tnrb.guidance.p2"))
      view should include(messagesApi("page.iht.application.tnrb.guidance.p3"))
    }

    "show the correct paragraphs on system page load" in {
      implicit val request = createFakeRequest()
      val view = tnrb_guidance("ihtReference", "url", "deceased name", "page.iht.application.tnrb.guidance.system.title",
        "page.iht.application.tnrb.guidance.system.title",
        true).toString
      view should include(messagesApi("page.iht.application.tnrb.guidance.system.p1", "deceased name"))
    }

    "show the correct indent paragraph" in {
      implicit val request = createFakeRequest()
      val view = tnrb_guidance("ihtReference", "url", "deceased name", "iht.estateReport.tnrb.increasingIHTThreshold",
        "iht.estateReport.tnrb.increasingThreshold",
        false).toString
      val doc = asDocument(view)
      assertContainsMessage(doc, ".panel-indent p", "page.iht.application.tnrb.guidance.panelIndent.text")
    }

    "show a return to estate overview button which has specified iht reference" in {
      implicit val request = createFakeRequest()
      val view = tnrb_guidance("ihtReference", "url", "deceased name", "iht.estateReport.tnrb.increasingIHTThreshold",
        "iht.estateReport.tnrb.increasingThreshold",
        false).toString
      val doc = asDocument(view)
      assertRenderedById(doc, "continue-to-estate-overview-button")
      val button: Element = doc.getElementById("continue-to-estate-overview-button")
      button.text() shouldBe messagesApi("iht.estateReport.returnToEstateOverview")
      button.className() shouldBe "button"
      button.attr("href") shouldBe iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef("ihtReference").url
   }

    "show the correct continue to increasing threshold link" in {
      implicit val request = createFakeRequest()
      val expectedUrl = "url"
      val view = tnrb_guidance("ihtReference", expectedUrl, "deceased name", "iht.estateReport.tnrb.increasingIHTThreshold",
        "iht.estateReport.tnrb.increasingThreshold",
        false).toString
      val doc = asDocument(view)
      assertRenderedById(doc, "continue-to-increasing-threshold-link")
      val link: Element = doc.getElementById("continue-to-increasing-threshold-link")
      link.text() shouldBe messagesApi("page.iht.application.tnrb.guidance.continueLink.text")
      link.attr("href") shouldBe expectedUrl
    }

  }
}
