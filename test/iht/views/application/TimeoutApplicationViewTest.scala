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

package iht.views.application

import iht.views.ViewTestHelper
import iht.views.html.application.timeout_application
import play.api.i18n.Messages

class TimeoutApplicationViewTest extends ViewTestHelper {
  private lazy val viewAsDocument = {
    implicit val request = createFakeRequest()
    asDocument(timeout_application().toString)
  }

  "TimeoutApplication View" must {
    "have the correct title and browser title" in {
      titleShouldBeCorrect(viewAsDocument.toString, Messages("iht.signedOut"))
    }

    "have the correct first paragraph" in {
      viewAsDocument.getElementById("paragraph1").text shouldBe Messages("iht.timeout.p1")
    }

    "have the correct second paragraph" in {
      viewAsDocument.getElementById("paragraph2").text shouldBe Messages("page.iht.application.timeout.p2")
    }
  }
}
