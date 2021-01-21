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

import iht.views.html.sign_out
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig

class SignOutViewTest extends ViewTestHelper {
  def view: String = sign_out()(createFakeRequest(), messages, formPartialRetriever, appConfig).toString

  "Sign out view" must {
    "have no message keys in html" in {
      noMessageKeysShouldBePresent(view)
    }

    "have the correct title" in {
      val doc = asDocument(view)
      val headers = doc.getElementsByTag("h1")
      headers.size mustBe 1
      headers.get(0).text() mustBe messagesApi("iht.signedOut")
    }

    "have the correct message" in {
      messagesShouldBePresent(view, messagesApi("page.iht.sign-out.p1"))
    }
  }
}
