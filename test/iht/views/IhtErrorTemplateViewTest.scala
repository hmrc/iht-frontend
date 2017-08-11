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

package iht.views

import iht.views.html.iht_error_template
import play.api.i18n.Messages.Implicits._

class IhtErrorTemplateViewTest extends ViewTestHelper {
  val title = "1"
  val heading = "2"
  val message = "3"
  def view: String = iht_error_template(title, heading, message)(createFakeRequest(),
                                                                 applicationMessages,
                                                                 formPartialRetriever).toString

  "Application error template" must {
    "have no message keys in html" in {
      noMessageKeysShouldBePresent(view)
    }

    "have the correct title" in {
      val doc = asDocument(view)
      val headers = doc.getElementsByTag("h1")
      headers.size shouldBe 2
      headers.get(1).text() shouldBe title
    }

    "have the correct heading" in {
      val doc = asDocument(view)
      val headers = doc.getElementsByTag("h1")
      headers.size shouldBe 2
      headers.first.text() shouldBe heading
    }

    "have the correct message" in {
      messagesShouldBePresent(view, message)
    }
  }
}
