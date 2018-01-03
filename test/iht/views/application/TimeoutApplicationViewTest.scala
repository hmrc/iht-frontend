/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.i18n.Messages.Implicits._
import iht.config.ApplicationConfig

class TimeoutApplicationViewTest extends ViewTestHelper {
  private lazy val viewAsDocument = {
    implicit val request = createFakeRequest()
    asDocument(timeout_application().toString)
  }

  "TimeoutApplication View" must {
    "have no message keys in html" in {
      noMessageKeysShouldBePresent(viewAsDocument.toString)
    }

    "have the correct title" in {
      titleShouldBeCorrect(viewAsDocument.toString, messagesApi("iht.signedOut"))
    }

    "have the correct first paragraph" in {
      viewAsDocument.getElementById("paragraph1").text shouldBe messagesApi("iht.timeout.p1", (ApplicationConfig.timeOutSeconds/60))
    }

    "have the correct second paragraph" in {
      viewAsDocument.getElementById("paragraph2").text shouldBe messagesApi("page.iht.application.timeout.p2")
    }
  }
}
