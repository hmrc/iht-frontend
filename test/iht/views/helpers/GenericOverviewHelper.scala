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

package iht.views.helpers

import iht.views.ViewTestHelper
import org.jsoup.nodes.Document
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object GenericOverviewHelper extends ViewTestHelper {


  def headerQuestionShouldBeUnanswered(doc: Document, elementId: String, message: String, url: String) = {
    val heading = doc.getElementById(s"$elementId-section").getElementsByTag("h2").first
    heading.text mustBe message

    val link = doc.getElementById(elementId)
    messagesShouldBePresent(link.text, messagesApi("site.link.giveAnswer"))
    link.attr("href") mustBe url
  }

  def headerShouldBeAnswered(doc: Document, elementId: String, messageKey: String) = {
    val heading = doc.getElementById(s"$elementId-heading").getElementsByTag("h2").first
    heading.text mustBe messagesApi(messageKey)

    assertNotRenderedByCssSelector(doc, s"#$elementId-heading #$elementId")
  }

  def rowShouldBeAnswered(doc: Document, elementId: String, message: String, value: String, linkMessageKey: String, url: String) = {
    val li = doc.getElementById(elementId)
    val divs = li.getElementsByTag("div")
    divs.get(0).text mustBe message
    divs.get(1).text mustBe value

    val link = li.getElementsByTag("a").get(0)
    messagesShouldBePresent(link.text, messagesApi(linkMessageKey))
    link.attr("href") mustBe url

  }

  def rowShouldBeUnAnswered(doc: Document, elementId: String, messageKey: String, linkMessageKey: String, url: String) = {
    val li = doc.getElementById(elementId)
    val divs = li.getElementsByTag("div")
    divs.get(0).text mustBe messagesApi(messageKey)
    divs.get(1).text mustBe ""

    val link = li.getElementsByTag("a").get(0)
    messagesShouldBePresent(link.text, messagesApi(linkMessageKey))
    link.attr("href") mustBe url

  }
}
