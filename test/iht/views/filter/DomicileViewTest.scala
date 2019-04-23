/*
 * Copyright 2019 HM Revenue & Customs
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
import iht.views.html.filter.domicile
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig
import play.api.test.Helpers.{contentAsString, _}

class DomicileViewTest extends ViewTestHelper {

  val applicationMessages = messages

  val fakeRequest = createFakeRequest(isAuthorised = false)
  val fakeForm =  Form(single("s"-> optional(text)))

  "Domicile view" must {

    "have no message keys in html" in {
      val result = domicile(fakeForm)(fakeRequest, applicationMessages, formPartialRetriever, appConfig)
      val view = asDocument(contentAsString(result)).toString
      noMessageKeysShouldBePresent(view)
    }

    "generate appropriate content for the title" in {
      val result = domicile(fakeForm)(fakeRequest, applicationMessages, formPartialRetriever, appConfig)
      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first

      titleElement.text must include(messagesApi("page.iht.registration.deceasedPermanentHome.title"))
    }

    "generate appropriate content for the browser title" in {
      val result = domicile(fakeForm)(fakeRequest, applicationMessages, formPartialRetriever, appConfig)
      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("title").first

      titleElement.text must include(messagesApi("page.iht.registration.deceasedPermanentHome.title"))
    }

    "contain an appropriate field set" in {
      val result = domicile(fakeForm)(fakeRequest, applicationMessages, formPartialRetriever, appConfig)
      val doc = asDocument(contentAsString(result))
      val fieldSet = doc.getElementsByTag("fieldset")
      val id = fieldSet.attr("id")
      id must be("domicile-container")
    }

    "contain an 'England or Wales' radio button" in {
      val result = domicile(fakeForm)(fakeRequest, applicationMessages, formPartialRetriever, appConfig)
      val doc = asDocument(contentAsString(result))

      doc.getElementById("domicile-england-or-wales-label").text() must be("England or Wales")
    }

    "contain a 'Scotland' radio button" in {
      val result = domicile(fakeForm)(fakeRequest, applicationMessages, formPartialRetriever, appConfig)
      val doc = asDocument(contentAsString(result))

      doc.getElementById("domicile-scotland-label").text() must be(messagesApi("iht.countries.scotland"))
    }

    "contain a 'Northern Ireland' radio button" in {
      val result = domicile(fakeForm)(fakeRequest, applicationMessages, formPartialRetriever, appConfig)
      val doc = asDocument(contentAsString(result))

      doc.getElementById("domicile-northern-ireland-label").text() must be(messagesApi("iht.countries.northernIreland"))
    }

    "contain an 'Other country' radio button" in {
      val result = domicile(fakeForm)(fakeRequest, applicationMessages, formPartialRetriever, appConfig)
      val doc = asDocument(contentAsString(result))

      doc.getElementById("domicile-other-label").text() must be(messagesApi("page.iht.filter.domicile.choice.other"))
    }

    "contain a continue button with the text 'Continue'" in {
      val result = domicile(fakeForm)(fakeRequest, applicationMessages, formPartialRetriever, appConfig)
      val doc = asDocument(contentAsString(result))
      val button = doc.select("input#continue").first

      button.attr("value") must be(messagesApi("iht.continue"))
    }

    "contain a form with the action attribute set to the DomicileController onSubmit URL" in {
      val result = domicile(fakeForm)(fakeRequest, applicationMessages, formPartialRetriever, appConfig)
      val doc = asDocument(contentAsString(result))
      val formElement = doc.getElementsByTag("form").first

      formElement.attr("action") must be(iht.controllers.filter.routes.DomicileController.onSubmit().url)
    }

    "contain a link to return to the 'What do you want to do' page" in {
      val result = domicile(fakeForm)(fakeRequest, applicationMessages, formPartialRetriever, appConfig)
      val doc = asDocument(contentAsString(result))

      val link = doc.getElementById("return-link")
      link.text() must be(messagesApi("page.iht.filter.domicile.return.link"))
      link.attr("href") must be(iht.controllers.filter.routes.FilterController.onPageLoad().url)
    }
  }
}
