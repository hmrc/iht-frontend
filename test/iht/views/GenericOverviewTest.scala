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

import iht.FakeIhtApp
import iht.config.AppConfig
import iht.testhelpers.CommonBuilder
import iht.utils.OverviewHelper.{Link, Question, Section}
import iht.views.html.application.generic_overview
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{Call, MessagesControllerComponents}

class GenericOverviewTest extends FakeIhtApp with HtmlSpec {

  implicit val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]
  override implicit val lang: Lang = Lang.defaultLang
  implicit val messages: Messages = app.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(Seq(lang)).messages

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val call1 = CommonBuilder.DefaultCall1
  lazy val call2 = CommonBuilder.DefaultCall2
  lazy val call3 = Call("GET", "Call3")

  "GenericOverview" must {

    "display the first guidance paragraph" in {
      implicit val request = createFakeRequest()
      val result = generic_overview(registrationDetails = regDetails,
        seqRows = Nil,
        messagesFileSectionLine1 = "site.button.confirm",
        messagesFileSectionLine2 = "",
        returnToCall = Some(call1),
        returnToMessagesKey = "")
      val doc = asDocument(result)
      assertEqualsValue(doc,"#assets-guidance1", messagesApi("site.button.confirm"))
    }

    "have the second paragraph" which {
      "is NOT displayed when message key doesn't exist" in {
        implicit val request = createFakeRequest()
        val result = generic_overview(registrationDetails = regDetails,
          seqRows = Nil,
          messagesFileSectionLine1 = "",
          messagesFileSectionLine2 = "",
          returnToCall = Some(call1),
          returnToMessagesKey = "")
        val doc = asDocument(result)
        assertNotRenderedById(doc, "assets-guidance2")
      }
      "is displayed when message key exists" in {
        implicit val request = createFakeRequest()
        val result = generic_overview(registrationDetails = regDetails,
          seqRows = Nil,
          messagesFileSectionLine1 = "",
          messagesFileSectionLine2 = "site.button.confirm",
          returnToCall = Some(call1),
          returnToMessagesKey = "")
        val doc = asDocument(result)
        assertRenderedById(doc, "assets-guidance2")
      }
    }

    "display a section" which{

      def doc = {
        implicit val request = createFakeRequest()
        val view = generic_overview(registrationDetails = regDetails,
          seqRows = Seq(Section(id = "section-id",
            title = Some("section-title"),
            link = Link(linkText = "link-text", linkTextAccessibility = "link-accessibility", linkUrl = call1),
            details = Seq(Question(id = "question-id",
              title = "question-title",
              link = Link(linkText = "question-link-text", linkTextAccessibility = "question-link-accessibility", linkUrl = call2),
              value = "question-value",
              status = "question-status")))),
          messagesFileSectionLine1 = "",
          messagesFileSectionLine2 = "",
          returnToCall = Some(call3),
          returnToMessagesKey = "")
        asDocument(view.toString)
      }

      "with correct id"in{
        assertRenderedById(doc, "section-id")
      }
      "with correct title"in{
        assertContainsText(doc, "section-title")
      }

    }
  }
}
