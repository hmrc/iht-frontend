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

package iht.views.filter

import iht.constants.Constants._
import iht.testhelpers.UseService
import iht.views.ViewTestHelper
import iht.views.html.filter.use_service
import play.api.test.Helpers._

class UseServiceViewTest extends ViewTestHelper with UseService {

  val fakeRequest = createFakeRequest(isAuthorised = false)
  val applicationMessages = messages
  lazy val useServiceView: use_service = app.injector.instanceOf[use_service]

  "use_service" must {

    "have no message keys in html" in {

      val result = useServiceView(under325000, false, "")(fakeRequest, applicationMessages)
      val view = asDocument(contentAsString(result)).toString
      noMessageKeysShouldBePresent(view)
    }

    "generate appropriate content for the title" in {
      val result = useServiceView(under325000, false, "iht.mustUseOnlineService")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first

      titleElement.text must include(messagesApi("iht.mustUseOnlineService"))
    }

    "generate appropriate content for the browser title" in {
      val result = useServiceView(under325000, false, "iht.mustUseOnlineService")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val browserTitleElement = doc.getElementsByTag("title").first

      browserTitleElement.text must include(messagesApi("iht.mustUseOnlineService"))
    }

    "generate appropriate content for under 325000" in {
      val result = useServiceView(under325000, false, "")(fakeRequest, applicationMessages)
      val content = contentAsString(result)

      content must include(messagesApi("page.iht.filter.useService.under325000.paragraph0"))
      content must include(messagesApi("page.iht.filter.useService.paragraphFinal"))
      content mustNot include(messagesApi("page.iht.filter.useService.between325000And1Million.section1.title"))
      content mustNot include(messagesApi("page.iht.filter.useService.between325000And1Million.section2.title"))
      content mustNot include(messagesApi("page.iht.filter.useService.between325000And1Million.section3.title"))
    }

    "generate appropriate content for between 325000 and 1 million" in {
      val result = useServiceView(between325000and1million, false, "")(fakeRequest, applicationMessages)
      val content = contentAsString(result)

      content must include(messagesApi("page.iht.filter.useService.between325000And1Million.section1.title"))
      content must include(messagesApi("page.iht.filter.useService.between325000And1Million.section2.title"))
      content must include(messagesApi("page.iht.filter.useService.between325000And1Million.section3.title"))
    }

    "display content about other ways to report the value of the estate when value is under 325000" in {
      val result = useServiceView(under325000, false, "")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val h2 = doc.getElementById("other-ways-to-report")
      h2.text() must be(messagesApi("page.iht.filter.useService.under325000.otherWaysToReportValue"))
    }

    "does not display content about other ways to report the value of the estate when value is between 325000 and 1 million" in {
      val result = useServiceView(between325000and1million, false, "")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val h2 = doc.getElementById("other-ways-to-reportOver")
      h2.text() mustBe pageIHTFilterUseServiceBetween325000And1MillionSection4P1
    }

    "contain a link with the button class with the text 'Continue' for values under 325000" in {
      val result = useServiceView(under325000, false, "")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val button = doc.select("a.button").first
    }

    "generate content for the final paragraph when given the under 325 parameter" in {
      val result = useServiceView(under325000, false, "")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val paragraph0 = doc.getElementById("paragraph-final")
      paragraph0.text() must be(messagesApi("page.iht.filter.useService.paragraphFinal"))
    }

    "contain a link with the button class with the text 'Continue to online service' " +
      "for values between 325000 and 1 million" in {
      val result = useServiceView(between325000and1million, false, "")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val button = doc.select("a.button").first

      button.text() mustBe pageIHTFilterUseServiceBetween325000And1MillionReport
    }

    "contain a link to the TNRB and RNRB guidance, and IHT400, when value is between 325 and 1 million" in {
      val result = useServiceView(between325000and1million, false, "")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val tnrb = doc.getElementById("tnrb-link")
      val rnrb = doc.getElementById("rnrb-link")
      val iht400 = doc.getElementById("IHT400-form")

      tnrb.attr("href") must be("https://www.gov.uk/guidance/inheritance-tax-transfer-of-threshold")
      rnrb.attr("href") must be("https://www.gov.uk/guidance/check-if-you-can-get-an-additional-inheritance-tax-threshold")
      iht400.attr("href") must be("https://www.gov.uk/government/publications/inheritance-tax-inheritance-tax-account-iht400")
    }

    "contain a link with the button class with href attribute pointing to the start pages" in {
      val result = useServiceView(under325000, false, "")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val button = doc.select("a.button").first

      button.attr("href") must be(iht.controllers.registration.routes.RegistrationChecklistController.onPageLoad().url)
    }

    "contain a 'Previous answers' section" in {
      val result = useServiceView(under325000, false, "")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "previous-answers")
    }

    "contain a 'Start again' link to go back to the domicile page" in {
      val result = useServiceView(under325000, false, "")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("start-again")
      link.text() must be(messagesApi("iht.startAgain"))
      link.attr("href") must be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous domicile question" in {
      val result = useServiceView(under325000, false, "")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("domicile-row")
      row.text() must include(messagesApi("page.iht.registration.deceasedPermanentHome.title"))
      row.text() must include(messagesApi("iht.countries.englandOrWales"))
    }

    "contain a 'Change' link to go back to the domicile page" in {
      val result = useServiceView(under325000, false, "")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("change-domicile")
      link.text() must include(messagesApi("iht.change"))
      link.attr("href") must be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous estimate question when given the under 32500 parameter" in {
      val result = useServiceView(under325000, false, "")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("estimate-row")
      row.text() must include(messagesApi("iht.roughEstimateEstateWorth"))
      row.text() must include(messagesApi("page.iht.filter.estimate.choice.under"))
    }

    "contain a row showing the user's answer to the previous estimate question when given the between parameter" in {
      val result = useServiceView(between325000and1million, false, "")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val rows = doc.getElementsByAttributeValue("id","estimate-row")
      rows.size() mustEqual 1
    }

    "contain a 'Change' link to go back to the estimate page" in {
      val result = useServiceView(under325000, false, "")(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("change-estimate")
      link.text() must include(messagesApi("iht.change"))
      link.attr("href") must be(iht.controllers.filter.routes.EstimateController.onPageLoadWithoutJointAssets().url)
    }
  }
}
