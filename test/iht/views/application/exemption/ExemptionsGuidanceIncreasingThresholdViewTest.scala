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

package iht.views.application.exemption

import iht.views.ViewTestHelper
import iht.views.html.application.exemption.exemptions_guidance_increasing_threshold
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class ExemptionsGuidanceIncreasingThresholdViewTest extends ViewTestHelper {

  lazy val exemptionsGuidanceIncreasingThresholdView: exemptions_guidance_increasing_threshold = app.injector.instanceOf[exemptions_guidance_increasing_threshold]

  "exemptions guidance increasing threshold page" must {

    "have no message keys in html" in {
      implicit val request = createFakeRequest()
      val view = exemptionsGuidanceIncreasingThresholdView("ihtReference").toString
      noMessageKeysShouldBePresent(view)
    }

    "show the correct title" in {
      implicit val request = createFakeRequest()
      val view = exemptionsGuidanceIncreasingThresholdView("ihtReference").toString
      val doc = asDocument(view)
      val headers: Elements = doc.getElementsByTag("h1")
      headers.size() mustBe 1
      headers.first().text() mustBe messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.title")
    }

    "show the correct browser title" in {
      implicit val request = createFakeRequest()
      val view = exemptionsGuidanceIncreasingThresholdView("ihtReference").toString
      val doc = asDocument(view)
      assertEqualsValue(doc, "title",
        messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.title") + " " + messagesApi("site.title.govuk"))
    }

    "show the correct paragraphs" in {
      implicit val request = createFakeRequest()
      val view = exemptionsGuidanceIncreasingThresholdView("ihtReference").toString
      val doc = asDocument(view)
      doc.select("div#Section1 p").text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section1.p1.start"))
      doc.select("#p1Link").text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section1.p1.link"))
      doc.select("div#Section1 p").text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section1.p1.end"))
      doc.select("div#Section1 p").text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section1.p2"))
      doc.select("div#Section2 p").text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section2.p3"))
      doc.select("div#Section2 p").text must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section2.p4"))
      doc.select("div#Section2 p").text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section2.p5.start"))
      doc.select("#iht400Link").text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section2.p5.link"))
      doc.select("div#Section2 p").text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section2.p5.end"))
      doc.select("div#Section3 p").text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section3.p6.start"))
      doc.select("div#Section3 p").text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section3.p6.link"))
      doc.select("div#Section3 p").text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section3.p6.end"))
      doc.select("div#Section3 p").text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section3.p7.start"))
      doc.select("div#Section4 p").text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section4.p8"))
    }

    "show the correct sub headings" in {
      implicit val request = createFakeRequest()
      val view = exemptionsGuidanceIncreasingThresholdView("ihtReference").toString
      val doc = asDocument(view)
      val headers: Elements = doc.select("article h2")
      headers.size() mustBe 3
      headers.first().text() mustBe messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section2.exemptionHeader")
      headers.get(1).text() mustBe messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section3.raisedHeader")
      headers.last().text() mustBe messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section4.nextStepHeader")
    }

    "show the correct bullet points" in {
      implicit val request = createFakeRequest()
      val view = exemptionsGuidanceIncreasingThresholdView("ihtReference").toString
      val doc = asDocument(view)
      val bullets: Elements = doc.select("article li")
      bullets.size() mustBe 6
      doc.select("div#Section1 ul").first().text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section1.bullet1"))
      doc.select("div#Section1 ul").last().text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section1.bullet2"))
      doc.select("div#Section2 ul").first().text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section2.bullet3"))
      doc.select("div#Section2 ul").last().text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section2.bullet4"))
      doc.select("div#Section3 ul").first().text must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section3.bullet5"))
      doc.select("div#Section3 ul").last().text() must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section3.bullet6"))
    }

    "show a url linked to transfer of threshold page" in {
      implicit val request = createFakeRequest()
      val view = exemptionsGuidanceIncreasingThresholdView("ihtReference").toString
      val doc = asDocument(view)

      assertRenderedById(doc, "transferThresholdLink")
      val link = doc.getElementById("transferThresholdLink")
      link.text mustBe messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section3.p6.link")
      link.attr("href") mustBe "https://www.gov.uk/guidance/inheritance-tax-transfer-of-threshold"
    }

    "show a url linked to IHT400 page" in {
      implicit val request = createFakeRequest()
      val view = exemptionsGuidanceIncreasingThresholdView("ihtReference").toString
      val doc = asDocument(view)

      assertRenderedById(doc, "iht400Link")
      val link = doc.getElementById("iht400Link")
      link.text mustBe messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section2.p5.link")
      link.attr("href") mustBe "https://www.gov.uk/government/publications/inheritance-tax-inheritance-tax-account-iht400"
    }

    "shows a second url linked to IHT400 page" in {
      implicit val request = createFakeRequest()
      val view = exemptionsGuidanceIncreasingThresholdView("ihtReference").toString
      val doc = asDocument(view)

      assertRenderedById(doc, "iht400FormLink")
      val link = doc.getElementById("iht400FormLink")
      link.text mustBe messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.section2.p5.link")
      link.attr("href") mustBe "https://www.gov.uk/government/publications/inheritance-tax-inheritance-tax-account-iht400"
    }

    "show button with Continue as the title" in {
      implicit val request = createFakeRequest()
      val view = exemptionsGuidanceIncreasingThresholdView("ihtReference").toString
      val doc = asDocument(view)
      val button: Element = doc.getElementById("continue")

      assertRenderedById(doc, "continue")


      button.text() mustBe messagesApi("iht.continue")
      button.attr("name") mustBe "action"
    }
  }
}
