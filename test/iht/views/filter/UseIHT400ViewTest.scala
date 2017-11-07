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

package iht.views.filter

import iht.constants.IhtProperties
import iht.views.ViewTestHelper
import iht.views.html.filter.use_iht400
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers.{contentAsString, _}

/**
 * Created by jennygj on 25/10/16.
 */
class UseIHT400ViewTest extends ViewTestHelper {

  val fakeRequest = createFakeRequest(isAuthorised = false)
  val iht400PaperFormLink = "https://www.gov.uk/government/publications/inheritance-tax-inheritance-tax-account-iht400"

  "Use Paper Form view, when rendering for an estate exceeding £1 million" must {

    "have no message keys in html" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val view = asDocument(contentAsString(result)).toString
      noMessageKeysShouldBePresent(view)
    }

    "display the correct title" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val title = doc.getElementsByTag("h1").first
      title.text should be(messagesApi("iht.useIHT400PaperForm"))
    }

    "display the correct browser title" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val browserTitle = doc.getElementsByTag("title").first
      browserTitle.text should include(messagesApi("iht.useIHT400PaperForm"))
    }

    "contain content advising why you must use a paper IHT-400 form" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val content = contentAsString(result)
      content should include(messagesApi("page.iht.filter.paperform.million.p1"))
      content should include(messagesApi("page.iht.filter.paperform.million.p2"))
    }

    "contain content with link to IHT-400 paper form" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val linkElement = doc.getElementById("form-link")
      linkElement.text should be(messagesApi("page.iht.filter.paperform.iht400.link.text"))
    }

    "contain link to IHT-400 paper form" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val linkElement = doc.getElementById("form-link")
      linkElement.attr("href") should be(iht400PaperFormLink)
    }

    "contain a link with the button class with the text 'Exit to IHT-400 paper form'" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val button = doc.select("a.button").first

      button.text() should be(messagesApi("iht.continue"))
    }

    "contain a link with the button class with href attribute pointing to ???" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val button = doc.select("a.button").first

      button.attr("href") should be(iht.controllers.routes.DeadlinesController.onPageLoadRegistration().url)
    }

    "contain a 'Previous answers' section" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "previous-answers")
    }

    "contain a 'Start again' link to go back to the domicile page" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("start-again")
      link.text() should be(messagesApi("iht.startAgain"))
      link.attr("href") should be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous domicile question" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("domicile-row")
      row.text() should include(messagesApi("page.iht.registration.deceasedPermanentHome.title"))
      row.text() should include(messagesApi("iht.countries.englandOrWales"))
    }

    "contain a 'Change' link to go back to the domicile page" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("change-domicile")
      link.text() should include(messagesApi("iht.change"))
      link.attr("href") should be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous estimate question when given the under 32500 parameter" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("estimate-row")
      row.text() should include(messagesApi("iht.roughEstimateEstateWorth"))
      row.text() should include(messagesApi("page.iht.filter.estimate.choice.over"))
    }

    "contain a row showing the user's answer to the previous estimate question when given the between parameter" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("estimate-row")
      row.text() should include(messagesApi("iht.roughEstimateEstateWorth"))
      row.text() should include(messagesApi("page.iht.filter.estimate.choice.over"))
    }

    "contain a 'Change' link to go back to the estimate page" in {
      val result = use_iht400()(fakeRequest, applicationMessages, formPartialRetriever)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("change-estimate")
      link.text() should include(messagesApi("iht.change"))
      link.attr("href") should be(iht.controllers.filter.routes.EstimateController.onPageLoadWithoutJointAssets().url)
    }

  }

}
