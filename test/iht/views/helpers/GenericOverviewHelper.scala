/*
 * Copyright 2016 HM Revenue & Customs
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
import play.api.i18n.Messages

object GenericOverviewHelper extends ViewTestHelper {

  def headerQuestionShouldBeUnanswered(doc: Document, elementId: String, messageKey: String, url: String) = {
    val heading = doc.getElementById(elementId).getElementsByTag("h2").first
    heading.text shouldBe Messages(messageKey)

    val link = doc.getElementById(s"$elementId-link")
    messagesShouldBePresent(link.text, "site.link.giveAnswer")
    link.attr("href") shouldBe url
  }

  def headerShouldBeAnswered(doc: Document, elementId: String, messageKey: String) = {
    val heading = doc.getElementById(elementId).getElementsByTag("h2").first
    heading.text shouldBe Messages(messageKey)

    assertNotRenderedById(doc, s"$elementId-link")
  }

  def rowShouldBeAnswered(doc: Document, elementId: String, messageKey: String, value: String, linkMessageKey: String, url: String) = {
    val li = doc.getElementById(elementId)
    val divs = li.getElementsByTag("div")
    divs.get(0).text shouldBe Messages(messageKey)
    divs.get(1).text shouldBe value

    val link = doc.getElementById(s"$elementId-link")
    messagesShouldBePresent(link.text, Messages(linkMessageKey))
    link.attr("href") shouldBe url

  }

  def rowShouldBeUnAnswered(doc: Document, elementId: String, messageKey: String, linkMessageKey: String, url: String) = {
    val li = doc.getElementById(elementId)
    val divs = li.getElementsByTag("div")
    divs.get(0).text shouldBe Messages(messageKey)
    divs.get(1).text shouldBe ""

    val link = doc.getElementById(s"$elementId-link")
    messagesShouldBePresent(link.text, Messages(linkMessageKey))
    link.attr("href") shouldBe url

  }
}
