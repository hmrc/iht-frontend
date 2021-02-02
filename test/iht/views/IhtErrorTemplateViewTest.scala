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

package iht.views

import iht.views.html.iht_error_template
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.config.AppConfig

class IhtErrorTemplateViewTest extends ViewTestHelper {
  val title = "Sorry, there is a problem with the service"
  val messages1 = "Try again later to sign in to the service at https://www.tax.service.gov.uk/inheritance-tax/estate-report (save this link)."
  val messages2 = "We saved your progress on the estate report."
  val messages3 = "If you see this message several times, you can choose to report the estate value using the IHT205 paper form instead."
  def view: String = iht_error_template()(createFakeRequest(), messages, formPartialRetriever, appConfig).toString

  "Application error template" must {
    "have no message keys in html" in {
      noMessageKeysShouldBePresent(view)
    }

    "have the correct title" in {
      val doc = asDocument(view)
      val headers = doc.getElementsByTag("h1")
      headers.size mustBe 1
      headers.first.text() mustBe title
    }

    "have the correct messages" in {
      val doc = asDocument(view)
      val message1 = doc.select("#content > article > p:nth-child(2)")
      val message2 = doc.select("#content > article > p:nth-child(3)")
      val message3 = doc.select("#content > article > p:nth-child(4)")
      message1.text() mustBe messages1
      message2.text() mustBe messages2
      message3.text() mustBe messages3
    }
  }
}
