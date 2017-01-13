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

package iht.views

import iht.{FakeIhtApp, TestUtils}
import org.jsoup.nodes.Document
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

trait ViewTestHelper extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with HtmlSpec with BeforeAndAfter {

  def titleShouldBeCorrect(pageContent: String, expectedTitle: String) = {
    val doc = asDocument(pageContent)
    val headers = doc.getElementsByTag("h1")
    headers.size shouldBe 1
    headers.first.text() shouldBe expectedTitle
  }

  def browserTitleShouldBeCorrect(pageContent: String, expectedTitle: String) = {
    val doc = asDocument(pageContent)
    assertEqualsValue(doc, "title", buildApplicationTitle(expectedTitle))
  }

  def messagesShouldBePresent(content: String, expectedSentences: String*) = {
    for (sentence <- expectedSentences) content should include(sentence)
  }

  def buildApplicationTitle(title: String) = title + " " + Messages("site.title.govuk")

  def labelShouldBe(doc: Document, labelId: String, messageKey: String) = {
    val label = doc.getElementById(labelId)
    val mainLabel = label.getElementsByTag("span").first
    mainLabel.text shouldBe Messages(messageKey)
  }

  def labelHelpTextShouldBe(doc: Document, labelId: String, messageKey: String) = {
    val label = doc.getElementById(labelId)
    val helpText = label.getElementsByTag("span").get(1)
    helpText.text shouldBe Messages(messageKey)
  }
}
