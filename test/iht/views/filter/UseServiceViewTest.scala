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

package iht.views.filter

import iht.constants.Constants._
import iht.views.ViewTestHelper
import iht.views.html.filter.use_service
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._

/**
  * Created by adwelly on 25/10/2016.
  */
class UseServiceViewTest extends ViewTestHelper {

  val fakeRequest = createFakeRequest(isAuthorised = false)

  "use_service" must {

    "have no message keys in html" in {

      val result = use_service(under325000, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val view = asDocument(contentAsString(result)).toString
      noMessageKeysShouldBePresent(view)
    }

    "generate appropriate content for the title" in {
      val result = use_service(under325000, false, "iht.shouldUseOnlineService")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first

      titleElement.text should include(messagesApi("iht.shouldUseOnlineService"))
    }

    "generate appropriate content for the browser title" in {
      val result = use_service(under325000, false, "iht.shouldUseOnlineService")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val browserTitleElement = doc.getElementsByTag("title").first

      browserTitleElement.text should include(messagesApi("iht.shouldUseOnlineService"))
    }

    "generate appropriate content for under 325000" in {
      val result = use_service(under325000, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val content = contentAsString(result)

      content should include(messagesApi("page.iht.filter.useService.under325000.paragraph0"))
      content should include(messagesApi("page.iht.filter.useService.paragraphFinal"))
      content shouldNot include(messagesApi("page.iht.filter.useService.between325000And1Million.section1.title"))
      content shouldNot include(messagesApi("page.iht.filter.useService.between325000And1Million.section2.title"))
      content shouldNot include(messagesApi("page.iht.filter.useService.between325000And1Million.section3.title"))
    }

    "generate appropriate content for between 325000 and 1 million" in {
      val result = use_service(between325000and1million, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val content = contentAsString(result)

      content should include(messagesApi("page.iht.filter.useService.between325000And1Million.section1.title"))
      content should include(messagesApi("page.iht.filter.useService.between325000And1Million.section2.title"))
      content should include(messagesApi("page.iht.filter.useService.between325000And1Million.section3.title"))
    }

    "display content about other ways to report the value of the estate when value is under 325000" in {
      val result = use_service(under325000, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val h2 = doc.getElementById("other-ways-to-report")
      h2.text() should be(messagesApi("page.iht.filter.useService.under325000.otherWaysToReportValue"))
    }

    "display content about other ways to report the value of the estate when value is between 325000 and 1 million" in {
      val result = use_service(between325000and1million, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val h2 = doc.getElementById("other-ways-to-report")
      h2.text() should be(messagesApi("page.iht.filter.useService.under325000.otherWaysToReportValue"))
    }

    "contain a link with the button class with the text 'Continue' for values under 325000" in {
      val result = use_service(under325000, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val button = doc.select("a.button").first
    }

    "generate content for the final paragraph when given the under 325 parameter" in {
      val result = use_service(under325000, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val paragraph0 = doc.getElementById("paragraph-final")
      paragraph0.text() should be(messagesApi("page.iht.filter.useService.paragraphFinal"))
    }

    "contain a link with the button class with the text 'Report the value of the estate online' " +
      "for values between 325000 and 1 million" in {
      val result = use_service(between325000and1million, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val button = doc.select("a.button").first

      button.text() should be(messagesApi("page.iht.filter.useService.between325000And1Million.report"))
    }

    "contain a link to the TNRB and RNRB guidance, and IHT400, when value is between 325 and 1 million" in {
      val result = use_service(between325000and1million, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val tnrb = doc.getElementById("tnrb-link")
      val rnrb = doc.getElementById("rnrb-link")
      val iht400 = doc.getElementById("400-link")

      tnrb.attr("href") should be("https://www.gov.uk/guidance/inheritance-tax-transfer-of-threshold")
      rnrb.attr("href") should be("https://www.gov.uk/guidance/inheritance-tax-residence-nil-rate-band")
      iht400.attr("href") should be("https://www.gov.uk/government/publications/inheritance-tax-inheritance-tax-account-iht400")
    }

    "contain a link with the button class with href attribute pointing to the start pages" in {
      val result = use_service(under325000, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val button = doc.select("a.button").first

      button.attr("href") should be(iht.controllers.registration.routes.RegistrationChecklistController.onPageLoad().url)
    }

    "contain a 'Previous answers' section" in {
      val result = use_service(under325000, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "previous-answers")
    }

    "contain a 'Start again' link to go back to the domicile page" in {
      val result = use_service(under325000, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("start-again")
      link.text() should be(messagesApi("iht.startAgain"))
      link.attr("href") should be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous domicile question" in {
      val result = use_service(under325000, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("domicile-row")
      row.text() should include(messagesApi("page.iht.registration.deceasedPermanentHome.title"))
      row.text() should include(messagesApi("iht.countries.englandOrWales"))
    }

    "contain a 'Change' link to go back to the domicile page" in {
      val result = use_service(under325000, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("change-domicile")
      link.text() should include(messagesApi("iht.change"))
      link.attr("href") should be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous estimate question when given the under 32500 parameter" in {
      val result = use_service(under325000, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("estimate-row")
      row.text() should include(messagesApi("iht.roughEstimateEstateWorth"))
      row.text() should include(messagesApi("page.iht.filter.estimate.choice.under"))
    }

    "contain a row showing the user's answer to the previous estimate question when given the between parameter" in {
      val result = use_service(between325000and1million, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("estimate-row")
      row.text() should include(messagesApi("iht.roughEstimateEstateWorth"))
      row.text() should include(messagesApi("page.iht.filter.estimate.choice.between"))
    }

    "contain a 'Change' link to go back to the estimate page" in {
      val result = use_service(under325000, false, "")(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("change-estimate")
      link.text() should include(messagesApi("iht.change"))
      link.attr("href") should be(iht.controllers.filter.routes.EstimateController.onPageLoadWithoutJointAssets().url)
    }
  }
}
