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

import iht.constants.Constants._
import iht.FakeIhtApp
import iht.views.HtmlSpec
import iht.views.html.filter.{agent_view, use_service}
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by adwelly on 25/10/2016.
  */
class UseServiceTest extends UnitSpec with FakeIhtApp with HtmlSpec {
  val fakeRequest = createFakeRequest(isAuthorised = false)

  "use_service" must {
    "generate appropriate content for the title" in {
      val result = use_service(under325000)
      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first

      titleElement.text should include(Messages("iht.shouldUseOnlineService"))
    }

    "generate appropriate content for the browser title" in {
      val result = use_service(under325000)
      val doc = asDocument(contentAsString(result))
      val browserTitleElement = doc.getElementsByTag("title").first

      browserTitleElement.text should include(Messages("iht.shouldUseOnlineService"))
    }

    "generate content for under 325000 paragraph zero when given the under 325 paramater" in {
      val result = use_service(under325000)
      val doc = asDocument(contentAsString(result))
      val paragraph0 = doc.getElementById("paragraph0")
      paragraph0.text() should be(Messages("page.iht.filter.useService.under325000.paragraph0"))
    }

    "generate content for the between 325000 and 1 million paragraph zero when given the between parameter" in {
      val result = use_service(between325000and1million)
      val doc = asDocument(contentAsString(result))
      val paragraph0 = doc.getElementById("paragraph0")
      paragraph0.text() should be(Messages("page.iht.filter.useService.between325000And1Million.paragraph0"))
    }

    "generate content for the between 325000 and 1 million paragraph one when given the between parameter" in {
      val result = use_service(between325000and1million)
      val doc = asDocument(contentAsString(result))
      val paragraph0 = doc.getElementById("paragraph1")
      paragraph0.text() should be(Messages("page.iht.filter.useService.between325000And1Million.paragraph1"))
    }

    "generate content for the final paragraph when given the under 325 parameter" in {
      val result = use_service(under325000)
      val doc = asDocument(contentAsString(result))
      val paragraph0 = doc.getElementById("paragraph-final")
      paragraph0.text() should be(Messages("page.iht.filter.useService.paragraphFinal"))
    }

    "generate content for the final paragraph when given the between parameter" in {
      val result = use_service(under325000)
      val doc = asDocument(contentAsString(result))
      val paragraph0 = doc.getElementById("paragraph-final")
      paragraph0.text() should be(Messages("page.iht.filter.useService.paragraphFinal"))
    }

    "contain a link with the button class with the text 'Continue'" in {
      val result = use_service(under325000)
      val doc = asDocument(contentAsString(result))
      val button = doc.select("a.button").first

      button.text() should be(Messages("iht.continue"))
    }

    "contain a link with the button class with href attribute pointing to the start pages" in {
      val result = use_service(under325000)
      val doc = asDocument(contentAsString(result))
      val button = doc.select("a.button").first

      button.attr("href") should be(iht.controllers.registration.routes.RegistrationChecklistController.onPageLoad().url)
    }

    "contain a 'Previous answers' section" in {
      val result = use_service(under325000)
      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "previous-answers")
    }

    "contain a 'Start again' link to go back to the domicile page" in {
      val result = use_service(under325000)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("start-again")
      link.text() should be(Messages("iht.startAgain"))
      link.attr("href") should be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous domicile question" in {
      val result = use_service(under325000)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("domicile-row")
      row.text() should include(Messages("iht.registration.deceased.permanentHome.where.question"))
      row.text() should include(Messages("iht.countries.englandOrWales"))
    }

    "contain a 'Change' link to go back to the domicile page" in {
      val result = use_service(under325000)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("change-domicile")
      link.text() should be(Messages("iht.change"))
      link.attr("href") should be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous estimate question when given the under 32500 parameter" in {
      val result = use_service(under325000)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("estimate-row")
      row.text() should include(Messages("iht.roughEstimateEstateWorth"))
      row.text() should include(Messages("page.iht.filter.estimate.choice.under"))
    }

    "contain a row showing the user's answer to the previous estimate question when given the between parameter" in {
      val result = use_service(between325000and1million)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("estimate-row")
      row.text() should include(Messages("iht.roughEstimateEstateWorth"))
      row.text() should include(Messages("page.iht.filter.estimate.choice.between"))
    }

    "contain a 'Change' link to go back to the estimate page" in {
      val result = use_service(under325000)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("change-estimate")
      link.text() should be(Messages("iht.change"))
      link.attr("href") should be(iht.controllers.filter.routes.EstimateController.onPageLoad().url)
    }
  }
}
