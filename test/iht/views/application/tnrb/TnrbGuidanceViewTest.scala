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

package iht.views.application.tnrb

import iht.views.HtmlSpec
import iht.views.html.application.tnrb.tnrb_guidance
import iht.{FakeIhtApp, TestUtils}
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.test.UnitSpec

class TnrbGuidanceViewTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with HtmlSpec with BeforeAndAfter {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "tnrb guidance page" must {

    "show the correct title" in {
      implicit val request = createFakeRequest()
      val view = tnrb_guidance("ihtReference", "url").toString
      val doc = asDocument(view)
      val headers: Elements = doc.getElementsByTag("h1")
      headers.size() shouldBe 1
      headers.first().text() shouldBe Messages("iht.estateReport.tnrb.increasingIHTThreshold")
    }

    "show the correct browser title" in {
      implicit val request = createFakeRequest()
      val view = tnrb_guidance("ihtReference", "url").toString
      val doc = asDocument(view)
      assertEqualsValue(doc, "title",
        Messages("iht.estateReport.tnrb.increasingThreshold") + " " + Messages("site.title.govuk"))
    }

    "show the correct paragraphs" in {
      implicit val request = createFakeRequest()
      val view = tnrb_guidance("ihtReference", "url").toString
      view should include(Messages("page.iht.application.tnrb.guidance.p1"))
      view should include(Messages("page.iht.application.tnrb.guidance.p2"))
      view should include(Messages("page.iht.application.tnrb.guidance.p3"))
    }

    "show the correct indent paragraph" in {
      implicit val request = createFakeRequest()
      val view = tnrb_guidance("ihtReference", "url").toString
      val doc = asDocument(view)
      assertContainsMessage(doc, ".panel-indent p", "page.iht.application.tnrb.guidance.panelIndent.text")
    }

    "show a return to estate overview button which has specified iht reference" in {
      implicit val request = createFakeRequest()
      val view = tnrb_guidance("ihtReference", "url").toString
      val doc = asDocument(view)
      assertRenderedById(doc, "continue-to-estate-overview-button")
      val button: Element = doc.getElementById("continue-to-estate-overview-button")
      button.text() shouldBe Messages("iht.estateReport.returnToEstateOverview")
      button.className() shouldBe "button"
      button.attr("href") shouldBe iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef("ihtReference").url
   }

    "show the correct continue to increasing threshold link" in {
      implicit val request = createFakeRequest()
      val expectedUrl = "url"
      val view = tnrb_guidance("ihtReference", expectedUrl).toString
      val doc = asDocument(view)
      assertRenderedById(doc, "continue-to-increasing-threshold-link")
      val link: Element = doc.getElementById("continue-to-increasing-threshold-link")
      link.text() shouldBe Messages("page.iht.application.tnrb.guidance.continueLink.text")
      link.attr("href") shouldBe expectedUrl
    }

  }
}
