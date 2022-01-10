/*
 * Copyright 2022 HM Revenue & Customs
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

package iht.views.ihtHelpers.custom

import iht.FakeIhtApp
import iht.views.HtmlSpec
import iht.views.html.ihtHelpers.custom.generic_overview_status_link
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.MessagesControllerComponents

class GenericOverviewStatusLinkTest extends FakeIhtApp with HtmlSpec {

  val mockControllerComponents: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  override implicit val lang: Lang = Lang.defaultLang
  override val messagesApi: MessagesApi = mockControllerComponents.messagesApi
  implicit val messages: Messages = messagesApi.preferred(Seq(lang)).messages
  lazy val genericOverviewStatusLinkView: generic_overview_status_link = app.injector.instanceOf[generic_overview_status_link]

  "GenericOverviewStatusLink helper" must {

    "return 'Give answer' label when item has not been started" in {
      implicit val request = createFakeRequest()
      val result = genericOverviewStatusLinkView(isComplete = None)
      val doc = asDocument(result)

      assertContainsText(doc, messagesApi("site.link.giveAnswer"))
      assertNotContainsText(doc, messagesApi("iht.giveMoreDetails"))
      assertNotContainsText(doc, messagesApi("iht.change"))
    }

    "return 'Give more details' label when item has been started but not completed" in {
      implicit val request = createFakeRequest()
      val result = genericOverviewStatusLinkView(isComplete = Some(false))
      val doc = asDocument(result)

      assertContainsText(doc, messagesApi("iht.giveMoreDetails"))
      assertNotContainsText(doc, messagesApi("site.link.giveAnswer"))
      assertNotContainsText(doc, messagesApi("iht.change"))
    }

    "return 'View or change' label when item has completed" in {
      implicit val request = createFakeRequest()
      val result = genericOverviewStatusLinkView(isComplete = Some(true))
      val doc = asDocument(result)

      assertContainsText(doc, messagesApi("iht.change"))
      assertNotContainsText(doc, messagesApi("iht.giveMoreDetails"))
      assertNotContainsText(doc, messagesApi("site.link.giveAnswer"))
    }

  }
}
