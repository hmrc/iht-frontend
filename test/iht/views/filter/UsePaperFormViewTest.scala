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
import iht.views.html.filter.use_paper_form
import play.api.test.Helpers.{contentAsString, _}

class UsePaperFormViewTest extends ViewTestHelper {

  val fakeRequest = createFakeRequest(isAuthorised = false)
  lazy val iht400FormUrl= "https://www.gov.uk/government/publications/inheritance-tax-inheritance-tax-account-iht400"
  lazy val iht205FormUrl= "https://www.gov.uk/government/publications/inheritance-tax-return-of-estate-information-iht205-2011"
  lazy val iht401FormUrl= "https://www.gov.uk/government/publications/inheritance-tax-domicile-outside-the-united-kingdom-iht401"
  lazy val usePaperFormView: use_paper_form = app.injector.instanceOf[use_paper_form]

  def getPageAsDoc(countryMessageKey: String = "") = {
    val result = usePaperFormView(countryMessageKey)(fakeRequest, messages)
    asDocument(contentAsString(result))
  }

  "Use Paper Form view" must {

    "have no message keys in html" in {
      val view = getPageAsDoc().toString
      noMessageKeysShouldBePresent(view)
    }

    "have the correct title" in {
      val doc = getPageAsDoc()
      val titleElement = doc.getElementsByTag("h1").first()

      titleElement.text must include(messagesApi("iht.usePaperForm"))
    }

    "have the correct browser title" in {
      val doc = getPageAsDoc()
      val titleElement = doc.getElementsByTag("title").first()

      titleElement.text must include(messagesApi("iht.usePaperForm"))
    }

    "contain a Previous answers section" in {
      val doc = getPageAsDoc()
      assertRenderedById(doc, "previous-answers")
    }
  }

  "Use Paper Form view, when rendering for Scotland" must {

    "contain the correct content" in {
      val doc = getPageAsDoc("iht.countries.scotland")
      doc.text() must include(messagesApi("page.iht.filter.paperform.scotland.p1"))
      doc.text() must include(messagesApi("page.iht.filter.paperform.scotland.p2.start"))
      doc.text() must include(messagesApi("page.iht.filter.paperform.scotland.p2.end"))
    }

    "contain a link to the Scottish Courts and Tribunals guidance" in {
      val doc = getPageAsDoc("iht.countries.scotland")
      val link = doc.getElementById("scottish-courts-link")
      link.text must be(messagesApi("page.iht.filter.paperform.scotland.link.text"))
      link.attr("href") must be(appConfig.linkScottishCourtAndTribunal)
    }

    "contain a 'Start again' link to go back to the domicile page" in {
      val doc = getPageAsDoc("iht.countries.scotland")
      val link = doc.getElementById("start-again")
      link.text() must be(messagesApi("iht.startAgain"))
      link.attr("href") must be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous question" in {
      val doc = getPageAsDoc("iht.countries.scotland")
      val row = doc.getElementById("domicile-row")
      row.text() must include(messagesApi("page.iht.registration.deceasedPermanentHome.title"))
      row.text() must include(messagesApi("iht.countries.scotland"))
    }

    "contain a 'Change' link to go back to the domicile page" in {
      val doc = getPageAsDoc("iht.countries.scotland")
      val link = doc.getElementById("change-domicile")
      link.text() must include(messagesApi("iht.change"))
      link.attr("href") must be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain 'Exit to GOV.UK' button to exit from the service" in {
      val doc = getPageAsDoc("iht.countries.scotland")
      val link = doc.getElementById("exit")
      link.text() must be(messagesApi("iht.exitToGovUK"))
      link.attr("href") must be(appConfig.linkExitToGovUKIHTForms)
    }
  }

  "Use Paper Form view, when rendering for Northern Ireland" must {

    "contain the correct content" in {
      val doc = getPageAsDoc("iht.countries.northernIreland")
      doc.text() must include(messagesApi("page.iht.filter.paperform.northern.ireland.p1"))
      doc.text() must include(messagesApi("page.iht.filter.paperform.northern.ireland.p2.sentence1.start"))
      doc.text() must include(messagesApi("page.iht.filter.paperform.northern.ireland.p2.sentence2.start"))
      doc.text() must include(messagesApi("page.iht.filter.paperform.northern.ireland.p2.sentence2.end"))
      doc.text() must include(messagesApi("page.iht.filter.paperform.northern.ireland.p3"))
    }

    "contain a link to the IHT-400 paper form" in {
      val doc = getPageAsDoc("iht.countries.northernIreland")
      val link = doc.getElementById("iht-400-link")
      link.text must be(messagesApi("page.iht.filter.paperform.iht400.link.text"))
      link.attr("href") must be (iht400FormUrl)
    }

    "contain a link to the IHT-205 paper form" in {
      val doc = getPageAsDoc("iht.countries.northernIreland")
      val link = doc.getElementById("iht-205-link")
      link.text must be(messagesApi("page.iht.filter.paperform.northern.ireland.iht205.link.text"))
      link.attr("href") must be (iht205FormUrl)
    }

    "contain a link to NI Direct" in {
      val doc = getPageAsDoc("iht.countries.northernIreland")
      val link = doc.getElementById("nidirect-link")
      link.text must be(messagesApi("page.iht.filter.paperform.northern.ireland.nidirect.link.text"))
      link.attr("href") must be("https://www.nidirect.gov.uk/articles/applying-probate")
    }

    "contain a 'Start again' link to go back to the domicile page" in {
      val doc = getPageAsDoc("iht.countries.northernIreland")
      val link = doc.getElementById("start-again")
      link.text() must be(messagesApi("iht.startAgain"))
      link.attr("href") must be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous question" in {
      val doc = getPageAsDoc("iht.countries.northernIreland")
      val row = doc.getElementById("domicile-row")
      row.text() must include(messagesApi("page.iht.registration.deceasedPermanentHome.title"))
      row.text() must include(messagesApi("iht.countries.northernIreland"))
    }

    "contain a 'Change' link to go back to the domicile page" in {
      val doc = getPageAsDoc("iht.countries.northernIreland")
      val link = doc.getElementById("change-domicile")
      link.text() must include(messagesApi("iht.change"))
      link.attr("href") must be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain 'Exit to GOV.UK' button to exit from the service" in {
      val doc = getPageAsDoc("iht.countries.northernIreland")
      val link = doc.getElementById("exit")
      link.text() must be(messagesApi("iht.exitToGovUK"))
      link.attr("href") must be(appConfig.linkExitToGovUKIHTForms)
    }
  }

  "Use Paper Form view, when rendering for another country" must {

    "contain the correct content" in {
      val doc = getPageAsDoc("page.iht.filter.domicile.choice.other")
      doc.text() must include(messagesApi("page.iht.filter.paperform.other.country.p1.sentence1.start"))
      doc.text() must include(messagesApi("page.iht.filter.paperform.other.country.p1.sentence2.start"))
      doc.text() must include(messagesApi("page.iht.filter.paperform.other.country.p2"))
    }

    "contain a link to the IHT-400 paper form" in {
      val doc = getPageAsDoc("page.iht.filter.domicile.choice.other")
      val link = doc.getElementById("iht-400-link")
      link.text must be(messagesApi("page.iht.filter.paperform.iht400.link.text"))
      link.attr("href") must be (iht400FormUrl)
    }

    "contain a link to the IHT-401 paper form" in {
      val doc = getPageAsDoc("page.iht.filter.domicile.choice.other")
      val link = doc.getElementById("iht-401-link")
      link.text must be(messagesApi("page.iht.filter.paperform.other.country.iht401.link.text"))
      link.attr("href") must be (iht401FormUrl)
    }

    "contain a 'Start again' link to go back to the domicile page" in {
      val doc = getPageAsDoc("page.iht.filter.domicile.choice.other")
      val link = doc.getElementById("start-again")
      link.text() must be(messagesApi("iht.startAgain"))
      link.attr("href") must be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous question" in {
      val doc = getPageAsDoc("page.iht.filter.domicile.choice.other")
      val row = doc.getElementById("domicile-row")
      row.text() must include(messagesApi("page.iht.registration.deceasedPermanentHome.title"))
      row.text() must include(messagesApi("page.iht.filter.domicile.choice.other"))
    }

    "contain a 'Change' link to go back to the domicile page" in {
      val doc = getPageAsDoc("page.iht.filter.domicile.choice.other")
      val link = doc.getElementById("change-domicile")
      link.text() must include(messagesApi("iht.change"))
      link.attr("href") must be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain 'Exit to GOV.UK' button to exit from the service" in {
      val doc = getPageAsDoc("page.iht.filter.domicile.choice.other")
      val link = doc.getElementById("exit")
      link.text() must be(messagesApi("iht.exitToGovUK"))
      link.attr("href") must be(appConfig.linkExitToGovUKIHTForms)
    }
  }

}
