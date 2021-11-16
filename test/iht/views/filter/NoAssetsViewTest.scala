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

package iht.views.filter

import iht.views.ViewTestHelper
import iht.views.html.filter.no_assets
import play.api.test.Helpers.{contentAsString, _}

class NoAssetsViewTest extends ViewTestHelper {

  val fakeRequest = createFakeRequest(isAuthorised = false)
  lazy val noAssetsView: no_assets = app.injector.instanceOf[no_assets]
  val iht400PaperFormLink = "https://www.gov.uk/government/publications/inheritance-tax-inheritance-tax-account-iht400"


  "No Assets view" must {

    "have no message keys in html" in {
      val result = noAssetsView()(fakeRequest, messages)
      val view = asDocument(contentAsString(result)).toString
      noMessageKeysShouldBePresent(view)
    }

    "display the correct title" in {
      val result = noAssetsView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val title = doc.getElementsByTag("h1").first
      title.text must be(messagesApi("page.iht.filter.noAssets.title"))
    }

    "display the correct browser title" in {
      val result = noAssetsView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val browserTitle = doc.getElementsByTag("title").first
      browserTitle.text must be(messagesApi("page.iht.filter.noAssets.title") + " - GOV.UK")
    }

    "contain content advising why you must use IHT400 form" in {
      val result = noAssetsView()(fakeRequest, messages)
      val content = contentAsString(result)
      content must include(messagesApi("page.iht.filter.noAssets.label.b"))
      content must include(messagesApi("page.iht.filter.noAssets.label.c"))
    }

    "contain link to IHT-400 paper form" in {
      val result = noAssetsView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val linkElement = doc.getElementById("form-link")
      linkElement.attr("href") must be(iht400PaperFormLink)
    }

    "contain a 'Previous answers' section" in {
      val result = noAssetsView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "previous-answers")
    }

    "contain a 'Start again' link to go back to the domicile page" in {
      val result = noAssetsView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("start-again")
      link.text() must be(messagesApi("iht.startAgain"))
      link.attr("href") must be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous domicile question" in {
      val result = noAssetsView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("domicile-row")
      row.text() must include(messagesApi("page.iht.registration.deceasedPermanentHome.title"))
      row.text() must include(messagesApi("iht.countries.englandOrWales"))
    }

    "contain a 'Change' link to go back to the domicile page" in {
      val result = noAssetsView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("change-domicile")
      link.text() must include(messagesApi("iht.change"))
      link.attr("href") must be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous estimate question when given the under 32500 parameter" in {
      val result = noAssetsView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("estimate-row")
      row.text() must include(messagesApi("iht.roughEstimateEstateWorth"))
      row.text() must include(messagesApi("page.iht.filter.estimate.choice.under"))
    }

    "contain a row showing the user's answer to the previous estimate question when given the between parameter" in {
      val result = noAssetsView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("estimate-row")
      row.text() must include(messagesApi("iht.roughEstimateEstateWorth"))
      row.text() must include(messagesApi("page.iht.filter.estimate.choice.under"))
    }

    "contain a 'Change' link to go back to the estimate page" in {
      val result = noAssetsView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("change-estimate")
      link.text() must include(messagesApi("iht.change"))
      link.attr("href") must be(iht.controllers.filter.routes.EstimateController.onPageLoadWithoutJointAssets().url)
    }

  }

}
