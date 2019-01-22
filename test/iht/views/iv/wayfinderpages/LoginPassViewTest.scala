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

package iht.views.iv.wayfinderpages

import iht.views.ViewTestHelper
import iht.views.helpers.MessagesHelper
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.i18n.Messages.Implicits._

class LoginPassViewTest extends ViewTestHelper with MessagesHelper {

  implicit lazy val fakeRequest = FakeRequest()
  lazy val view = iht.views.html.iv.wayfinderpages.login_pass()
  lazy val doc = Jsoup.parse(view.body)

  "Login Pass" ignore {

    "have the correct title" in {
      doc.title() mustBe WayfinderLoginTitle
    }

    "First paragraph" must {
      "have correct first paragraph text" in {
        doc.select("p").eq(2).text mustBe WayfinderLoginP1
      }

      "have correct first paragraph link" in {
        doc.select("p a").eq(3).text mustBe WayfinderLoginP1Link
      }

      "have correct first paragraph url" in {
        doc.select("p a").eq(3).attr("href") mustBe WayfinderLoginP1Url
      }
    }

    "Second paragraph" must {
      "have correct second paragraph" in {
        doc.select("p").eq(3).text mustBe WayfinderLoginP2
      }

    }

    "Continue button" must {
      "continue to next page url" in {
        doc.select("div div a").eq(4).attr("href") mustBe WayfinderStartUrl
      }

      "continue to next page link" in {
        doc.select("div div a").eq(4).text mustBe WayfinderStartLink
      }

      "have a class of button" in {
        doc.select("div div a").eq(4).attr("class") mustBe "button"
      }
    }

    "Leave link" must {
      "have a way of leaving page url" in {
        doc.select("div a").eq(8).attr("href") mustBe WayfinderLeaveUrl
      }

      "have a way of leaving page link" in {
        doc.select("div a").eq(8).text mustBe WayfinderLeaveLink
      }
    }
  }
}
