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
import iht.views.html.filter.any_assets
import play.api.data.Form
import play.api.data.Forms.{optional, single, text}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}

class AnyAssetsViewTest extends ViewTestHelper {

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest(isAuthorised = false)
  val fakeForm: Form[Option[String]] = Form(single("s" -> optional(text)))

  def getPageAsDoc(form: Form[Option[String]] = fakeForm, request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest) = {
    lazy val anyAssetsView: any_assets = app.injector.instanceOf[any_assets]

    val result = anyAssetsView(form, false)(messages, request)
    asDocument(contentAsString(result))
  }

  "Any Assets view" must {

    "have no message keys in html" in {
      val view = getPageAsDoc().toString
      noMessageKeysShouldBePresent(view)
    }

    "generate appropriate content for the title" in {
      val doc = getPageAsDoc()
      val titleElement = doc.getElementsByTag("h1").first

      titleElement.text must include(messagesApi("page.iht.filter.anyAssets.title"))
    }

    "generate appropriate content for the browser title" in {
      val doc = getPageAsDoc()
      val titleElement = doc.getElementsByTag("title").first

      titleElement.text must include(messagesApi("page.iht.filter.anyAssets.title"))
    }

    "contain an appropriate field set" in {
      val doc = getPageAsDoc()
      val fieldSet = doc.getElementsByTag("fieldset")
      val id = fieldSet.attr("id")
      id must be("any-assets-container")
    }

    "contain a 'Yes' radio button" in {
      val doc = getPageAsDoc()
      doc.getElementById("any-assets-any-assets-yes-label").text() must be(messagesApi("iht.yes"))
    }

    "contain a 'No' radio button" in {
      val doc = getPageAsDoc()
      doc.getElementById("any-assets-any-assets-no-label").text() must be(messagesApi("iht.no"))
    }

    "contain a continue button with the text 'Continue'" in {
      val doc = getPageAsDoc()
      val button = doc.select("input#continue").first

      button.attr("value") must be(messagesApi("iht.continue"))
    }

    "contain a form with the action attribute set to the Any Assets Controller onSubmit URL" in {
      val doc = getPageAsDoc()
      val formElement = doc.getElementsByTag("form").first

      formElement.attr("action") must be(iht.controllers.filter.routes.AnyAssetsController.onSubmitWithoutJointAssets().url)
    }

    "contain a 'Previous answers' section" in {
      val doc = getPageAsDoc()
      assertRenderedById(doc, "previous-answers")
    }

    "contain a 'Start again' link to go back to the domicile page" in {
      val doc = getPageAsDoc()
      val link = doc.getElementById("start-again")
      link.text() must be(messagesApi("iht.startAgain"))
      link.attr("href") must be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous question" in {
      val doc = getPageAsDoc()
      val row = doc.getElementById("domicile-row")
      row.text() must include(messagesApi("page.iht.registration.deceasedPermanentHome.title"))
      row.text() must include(messagesApi("iht.countries.englandOrWales"))
    }

    "contain a 'Change' link to go back to the estimate page" in {
      val doc = getPageAsDoc()
      val link = doc.getElementById("change-estimate")
      link.text() must include(messagesApi("iht.change"))
      link.attr("href") must be(iht.controllers.filter.routes.EstimateController.onPageLoadWithoutJointAssets().url)
    }

  }

}
