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
import iht.views.html.filter.estimate
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

class EstimateViewTest extends UnitSpec with FakeIhtApp with HtmlSpec {
  val fakeRequest = createFakeRequest(isAuthorised = false)
  val fakeForm =  Form(single("s"-> optional(text)))

  def getPageAsDoc(form: Form[Option[String]] = fakeForm, request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest) = {
    val result = estimate(form)(request)
    asDocument(contentAsString(result))
  }

  "Estimate view" must {

    "generate appropriate content for the title" in {
      val doc = getPageAsDoc()
      val titleElement = doc.getElementsByTag("h1").first

      titleElement.text should include(Messages("iht.roughEstimateEstateWorth"))
    }

    "generate appropriate content for the browser title" in {
      val doc = getPageAsDoc()
      val titleElement = doc.getElementsByTag("title").first

      titleElement.text should include(Messages("iht.roughEstimateEstateWorth"))
    }

    "contain an appropriate field set" in {
      val doc = getPageAsDoc()
      val fieldSet = doc.getElementsByTag("fieldset")
      val id = fieldSet.attr("id")
      id should be("estimate-container")
    }

    "contain an 'Under £325,000' radio button" in {
      val doc = getPageAsDoc()
      doc.getElementById("estimate-under-325000-label").text() should be(Messages("page.iht.filter.estimate.choice.under"))
    }

    "contain a 'Between £325,000 and £1 million' radio button" in {
      val doc = getPageAsDoc()
      doc.getElementById("estimate-between-325000-and-1million-label").text() should be(Messages("page.iht.filter.estimate.choice.between"))
    }

    "contain a 'More than £1 million' radio button" in {
      val doc = getPageAsDoc()
      doc.getElementById("estimate-more-than-1million-label").text() should be(Messages("page.iht.filter.estimate.choice.over"))
    }

    "contain a continue button with the text 'Continue'" in {
      val doc = getPageAsDoc()
      val button = doc.select("input#continue").first

      button.attr("value") should be(Messages("iht.continue"))
    }

    "contain a form with the action attribute set to the Estimate Controller onSubmit URL" in {
      val doc = getPageAsDoc()
      val formElement = doc.getElementsByTag("form").first

      formElement.attr("action") should be(iht.controllers.filter.routes.EstimateController.onSubmit().url)
    }

    "contain a 'Previous ansewrs' section" in {
      val doc = getPageAsDoc()
      assertRenderedById(doc, "previous-answers")
    }

    "contain a 'Start again' link to go back to the domicile page" in {
      val doc = getPageAsDoc()
      val link = doc.getElementById("start-again")
      link.text() should be(Messages("iht.startAgain"))
      link.attr("href") should be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous question" in {
      val doc = getPageAsDoc()
      val row = doc.getElementById("domicile-row")
      row.text() should include(Messages("iht.registration.deceased.permanentHome.where.question"))
      row.text() should include(Messages("iht.countries.englandOrWales"))
    }

    "contain a 'Change' link to go back to the domicile page" in {
      val doc = getPageAsDoc()
      val link = doc.getElementById("change-domicile")
      link.text() should be(Messages("iht.change"))
      link.attr("href") should be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }
  }
}
