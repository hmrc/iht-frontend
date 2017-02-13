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

import iht.FakeIhtApp
import iht.views.HtmlSpec
import iht.views.html.filter.use_paper_form
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

class UsePaperFormViewTest  extends UnitSpec with FakeIhtApp with HtmlSpec {
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val fakeRequest = createFakeRequest(isAuthorised = false)

  def getPageAsDoc(country: String = "") = {
    val result = use_paper_form(country)(fakeRequest, app.injector.instanceOf[Messages])
    asDocument(contentAsString(result))
  }

  "Use Paper Form view" must {

    "have the correct title" in {
      val doc = getPageAsDoc()
      val titleElement = doc.getElementsByTag("h1").first()

      titleElement.text should include(Messages("iht.usePaperForm"))
    }

    "have the correct browser title" in {
      val doc = getPageAsDoc()
      val titleElement = doc.getElementsByTag("title").first()

      titleElement.text should include(Messages("iht.usePaperForm"))
    }

    "contain a Previous answers section" in {
      val doc = getPageAsDoc()
      assertRenderedById(doc, "previous-answers")
    }
  }

  "Use Paper Form view, when rendering for Scotland" must {

    "contain the correct content" in {
      val doc = getPageAsDoc(Messages("iht.countries.scotland"))
      doc.text() should include(Messages("page.iht.filter.paperform.scotland.p1"))
      doc.text() should include(Messages("page.iht.filter.paperform.scotland.p2.start"))
      doc.text() should include(Messages("page.iht.filter.paperform.scotland.p2.end"))
    }

    "contain a link to the Scottish Courts and Tribunals guidance" in {
      val doc = getPageAsDoc(Messages("iht.countries.scotland"))
      val link = doc.getElementById("scottish-courts-link")
      link.text should be(Messages("page.iht.filter.paperform.scotland.link.text"))
      link.attr("rel") should be("external")
      link.attr("href") should be("http://www.scotcourts.gov.uk/taking-action/dealing-with-a-deceased%27s-estate-in-scotland")
    }

    "contain a 'Start again' link to go back to the domicile page" in {
      val doc = getPageAsDoc(Messages("iht.countries.scotland"))
      val link = doc.getElementById("start-again")
      link.text() should be(Messages("iht.startAgain"))
      link.attr("href") should be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous question" in {
      val doc = getPageAsDoc(Messages("iht.countries.scotland"))
      val row = doc.getElementById("domicile-row")
      row.text() should include(Messages("iht.registration.deceased.permanentHome.where.question"))
      row.text() should include(Messages("iht.countries.scotland"))
    }

    "contain a 'Change' link to go back to the domicile page" in {
      val doc = getPageAsDoc(Messages("iht.countries.scotland"))
      val link = doc.getElementById("change-domicile")
      link.text() should be(Messages("iht.change"))
      link.attr("href") should be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }
  }

  "Use Paper Form view, when rendering for Northern Ireland" must {

    "contain the correct content" in {
      val doc = getPageAsDoc(Messages("iht.countries.northernIreland"))
      doc.text() should include(Messages("page.iht.filter.paperform.northern.ireland.p1"))
      doc.text() should include(Messages("page.iht.filter.paperform.northern.ireland.p2.sentence1.start"))
      doc.text() should include(Messages("iht.fullStop"))
      doc.text() should include(Messages("page.iht.filter.paperform.northern.ireland.p2.sentence2.start"))
      doc.text() should include(Messages("page.iht.filter.paperform.northern.ireland.p2.sentence2.end"))
      doc.text() should include(Messages("page.iht.filter.paperform.northern.ireland.p3"))
    }

    "contain a link to the IHT-400 paper form" in {
      val doc = getPageAsDoc(Messages("iht.countries.northernIreland"))
      val link = doc.getElementById("iht-400-link")
      link.text should be(Messages("page.iht.filter.paperform.iht400.link.text"))
      pending // TODO: Check destination URL
    }

    "contain a link to the IHT-205 paper form" in {
      val doc = getPageAsDoc(Messages("iht.countries.northernIreland"))
      val link = doc.getElementById("iht-205-link")
      link.text should be(Messages("page.iht.filter.paperform.northern.ireland.iht205.link.text"))
      pending // TODO: Check destination URL
    }

    "contain a link to NI Direct" in {
      val doc = getPageAsDoc(Messages("iht.countries.northernIreland"))
      val link = doc.getElementById("nidirect-link")
      link.text should be(Messages("page.iht.filter.paperform.northern.ireland.nidirect.link.text"))
      link.attr("rel") should be("external")
      link.attr("href") should be("https://www.nidirect.gov.uk/articles/applying-probate")
    }

    "contain a 'Start again' link to go back to the domicile page" in {
      val doc = getPageAsDoc(Messages("iht.countries.northernIreland"))
      val link = doc.getElementById("start-again")
      link.text() should be(Messages("iht.startAgain"))
      link.attr("href") should be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous question" in {
      val doc = getPageAsDoc(Messages("iht.countries.northernIreland"))
      val row = doc.getElementById("domicile-row")
      row.text() should include(Messages("iht.registration.deceased.permanentHome.where.question"))
      row.text() should include(Messages("iht.countries.northernIreland"))
    }

    "contain a 'Change' link to go back to the domicile page" in {
      val doc = getPageAsDoc(Messages("iht.countries.northernIreland"))
      val link = doc.getElementById("change-domicile")
      link.text() should be(Messages("iht.change"))
      link.attr("href") should be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }
  }

  "Use Paper Form view, when rendering for another country" must {

    "contain the correct content" in {
      val doc = getPageAsDoc(Messages("page.iht.filter.domicile.choice.other"))
      doc.text() should include(Messages("page.iht.filter.paperform.other.country.p1.sentence1.start"))
      doc.text() should include(Messages("iht.fullStop"))
      doc.text() should include(Messages("page.iht.filter.paperform.other.country.p1.sentence2.start"))
      doc.text() should include(Messages("iht.fullStop"))
      doc.text() should include(Messages("page.iht.filter.paperform.other.country.p2"))
    }

    "contain a link to the IHT-400 paper form" in {
      val doc = getPageAsDoc(Messages("page.iht.filter.domicile.choice.other"))
      val link = doc.getElementById("iht-400-link")
      link.text should be(Messages("page.iht.filter.paperform.iht400.link.text"))
      pending // TODO: Check destination URL
    }

    "contain a link to the IHT-401 paper form" in {
      val doc = getPageAsDoc(Messages("page.iht.filter.domicile.choice.other"))
      val link = doc.getElementById("iht-401-link")
      link.text should be(Messages("page.iht.filter.paperform.other.country.iht401.link.text"))
      pending // TODO: Check destination URL
    }

    "contain a 'Start again' link to go back to the domicile page" in {
      val doc = getPageAsDoc(Messages("page.iht.filter.domicile.choice.other"))
      val link = doc.getElementById("start-again")
      link.text() should be(Messages("iht.startAgain"))
      link.attr("href") should be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous question" in {
      val doc = getPageAsDoc(Messages("page.iht.filter.domicile.choice.other"))
      val row = doc.getElementById("domicile-row")
      row.text() should include(Messages("iht.registration.deceased.permanentHome.where.question"))
      row.text() should include(Messages("page.iht.filter.domicile.choice.other"))
    }

    "contain a 'Change' link to go back to the domicile page" in {
      val doc = getPageAsDoc(Messages("page.iht.filter.domicile.choice.other"))
      val link = doc.getElementById("change-domicile")
      link.text() should be(Messages("iht.change"))
      link.attr("href") should be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }
  }

}
