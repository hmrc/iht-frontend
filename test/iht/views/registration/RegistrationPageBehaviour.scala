/*
 * Copyright 2020 HM Revenue & Customs
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

package iht.views.registration

import iht.views.ViewTestHelper
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat.Appendable

trait RegistrationPageBehaviour[A] extends ViewTestHelper {
  def pageTitle: String
  def browserTitle: String

  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()
  def view: String = formToView(form).toString
  def doc: Document = asDocument(view)
  def form:Form[A] = ???
  def formToView:Form[A] => Appendable = ???

  def registrationPage(): Unit = {

    "have no message keys in html" in {
      noMessageKeysShouldBePresent(view)
    }

    "have the correct title" in {
      titleShouldBeCorrect(view, pageTitle)
    }

    "have the correct browser title" in {
      browserTitleShouldBeCorrect(view, browserTitle)
    }

    "have a Continue button" in {
      doc.getElementsByClass("button").first.attr("value") mustBe messagesApi("iht.continue")
    }
  }

  def registrationPageWithErrorSummaryBox(): Unit = {
    registrationPage()
    "display the 'There's a problem' box if there's an error" in {
      val newForm = form.withError(FormError("field","error message"))
      val document = asDocument(formToView(newForm).toString)
      document.getElementById("errors").children.first.text mustBe messagesApi("error.problem")
    }
  }

  def registrationPageInEditModeWithErrorSummaryBox(view: => Document, cancelUrl: => Call): Unit = {
    registrationPageWithErrorSummaryBox()

    "have a continue and cancel link in edit mode" in {
      val continueLink = view.getElementById("continue-button")
      continueLink.attr("value") mustBe messagesApi("iht.continue")

      val cancelLink = view.getElementById("cancel-button")
      cancelLink.attr("href") mustBe cancelUrl.url
      cancelLink.text() mustBe messagesApi("site.link.cancel")
    }
  }

  def addressPage(guidance: => Seq[String]): Unit = {

    "show the correct guidance" in {
      messagesShouldBePresent(view, guidance:_*)
    }

    "have a line 1 field" in {
      assertRenderedById(doc, "ukAddressLine1")
    }

    "have the correct label for line 1" in {
      labelShouldBe(doc, "ukAddressLine1-container", "iht.address.line1")
    }

    "have a line 2 field" in {
      assertRenderedById(doc, "ukAddressLine2")
    }

    "have the correct label for line 2" in {
      labelShouldBe(doc, "ukAddressLine2-container", "iht.address.line2")
    }

    "have a line 3 field" in {
      assertRenderedById(doc, "ukAddressLine3")
    }

    "have the correct label for line 3" in {
      labelShouldBe(doc, "ukAddressLine3-container", "iht.address.line3")
    }

    "have a line 4 field" in {
      assertRenderedById(doc, "ukAddressLine4")
    }

    "have the correct label for line 4" in {
      labelShouldBe(doc, "ukAddressLine4-container", "iht.address.line4")
    }
  }

  def addressPageUK(guidance: => Seq[String]): Unit = {
    addressPage(guidance)

    "have a post code field" in {
      assertRenderedById(doc, "postCode")
    }

    "have the correct label for post code" in {
      labelShouldBe(doc, "postCode-container", "iht.postcode")
    }

    "not have a country code field" in {
      assertNotRenderedById(doc, "countryCode")
    }
  }

  def addressPageAbroad(guidance: => Seq[String]): Unit = {
    addressPage( guidance )

    "have a country code field" in {
      assertRenderedById(doc, "countryCode")
    }

    "not have a post code field" in {
      assertNotRenderedById(doc, "postCode")
    }
  }
}
