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

package iht.views.registration

import iht.views.ViewTestHelper
import iht.views.html.registration.timeout_registration
import play.api.i18n.Messages.Implicits._
import iht.config.ApplicationConfig

class TimeoutRegistrationViewTest extends ViewTestHelper {
  private lazy val viewAsDocument = {
    implicit val request = createFakeRequest()
    asDocument(timeout_registration().toString)
  }

  "TimeoutRegistration View" must {

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
      viewAsDocument.getElementById("paragraph2").text shouldBe messagesApi("page.iht.registration.timeout.p2")
    }
  }
}
