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

package iht.views.application.gifts

import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.html.application.gift.gifts_sidebar
import play.api.i18n.MessagesApi
import play.api.i18n.Messages.Implicits._


class GiftsSidebarViewTest extends ViewTestHelper {

  def doc = {
    implicit val request = createFakeRequest()
    val view = gifts_sidebar(
      relevantSectionGuidanceUrl = CommonBuilder.DefaultCall1,
      messageKeyUrl = "test",
      messageKeyAccessibility = ""
    ).toString

    asDocument(view)
  }

  "gifts sidebar view" must {

    "have no message keys in html" in {
      val view = doc.toString
      noMessageKeysShouldBePresent(view)
    }

    "have the correct title" in {
        val headers = doc.getElementsByTag("h2")
        headers.size shouldBe 1
        headers.first.text() shouldBe messagesApi("site.getHelp")
    }

      behave like link( doc, "whatIsAGift", CommonBuilder.DefaultCall1.url, "test")
  }
}
