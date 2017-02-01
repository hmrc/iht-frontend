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

package iht.views.registration

import iht.views.ViewTestHelper
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

trait RegistrationPageBehaviour extends ViewTestHelper {
  def pageTitle: String
  def browserTitle: String

  def fixture() = new {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = null
    val view: String = null
    val doc: Document = null
  }

  def fixtureWithError() = fixture()

  def registrationPage() = {
    "have the correct title" in {
      val f = fixture()
      titleShouldBeCorrect(f.view, pageTitle)
    }

    "have the correct browser title" in {
      val f = fixture()
      browserTitleShouldBeCorrect(f.view, browserTitle)
    }

    "have a Continue button" in {
      val f = fixture()
      f.doc.getElementsByClass("button").first.attr("value") shouldBe Messages("iht.continue")
    }
  }

  def registrationPageWithErrorSummaryBox() = {
    registrationPage()
    "display There's a problem box if there's an error" in {
      val f = fixtureWithError()
      f.doc.getElementById("errors").children.first.text shouldBe Messages("error.problem")
    }
  }
}
