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
import org.apache.commons.lang3.StringEscapeUtils
import iht.testhelpers.ContentChecker
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.i18n.{Lang, MessagesApi}
import play.twirl.api.Html

trait HtmlSpec { self: FakeIhtApp =>

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val lang = Lang.defaultLang

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())
  def asDocument(string: String): Document = Jsoup.parse(string)

  def optionalAttr(doc : Document, cssSelector : String, attributeKey:String) : Option[String] = {
    val element = doc.select(cssSelector).first()

    if (element != null && element.hasAttr(attributeKey)) Some(element.attr(attributeKey)) else None
  }

  def assertEqualsMessage(doc : Document, cssSelector : String, expectedMessageKey: String) = {
    val elements = doc.select(cssSelector)

    if(elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    assertMessageKeyHasValue(expectedMessageKey)
    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(ContentChecker.stripLineBreaks(elements.first().html()) == Html(messagesApi(expectedMessageKey)).toString())
  }

  def assertEqualsValue(doc : Document, cssSelector : String, expectedValue: String) = {
    val elements = doc.select(cssSelector)

    if(elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(ContentChecker.stripLineBreaks(elements.first().html()) == expectedValue)
  }

  def assertMessageKeyHasValue(expectedMessageKey: String): Unit = {
    assert(expectedMessageKey != Html(messagesApi(expectedMessageKey)).toString(), s"$expectedMessageKey has no messages file value setup")
  }

  def assertContainsMessage(doc : Document, cssSelector : String, expectedMessageKey: String) = {
    assertContainsDynamicMessage(doc, cssSelector, expectedMessageKey)
  }

  def assertContainsDynamicMessage(doc : Document, cssSelector : String, expectedMessageKey: String, messageArgs: String*) = {
    val elements = doc.select(cssSelector)

    if(elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    assertMessageKeyHasValue(expectedMessageKey)
    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    val expectedString = Html(messagesApi(expectedMessageKey, messageArgs: _*)).toString()

    assert(elements.toArray(new Array[Element](elements.size())).exists { element =>
      ContentChecker.stripLineBreaks(element.html()).contains(StringEscapeUtils.escapeHtml4(expectedString))
    })
  }

  def assertRenderedByCssSelector(doc: Document, cssSelector: String) = {
    assert(!doc.select(cssSelector).isEmpty, "Element " + cssSelector + " was not rendered on the page.")
  }

  def assertNotRenderedByCssSelector(doc: Document, cssSelector: String) = {
    assert(doc.select(cssSelector).isEmpty, "\n\nElement " + cssSelector + " was rendered on the page.\n")
  }

  def assertLinkHasValue(doc: Document, id: String, linkValue: String) = {
    assert(doc.select(s"#$id").attr("href") === linkValue)
  }

  def assertRenderedById(doc: Document, id: String) = {
    assert(doc.getElementById(id) != null, "\n\nElement " + id + " was not rendered on the page.\n")
  }

  def assertNotRenderedById(doc: Document, id: String) = {
    assert(doc.getElementById(id) == null, "\n\nElement " + id + " was rendered when not expected.\n")
  }

  def assertContainsText(doc:Document, text: String) = assert(doc.toString.contains(text), "\n\ntext " + text + " was not rendered on the page.\n")

  def assertNotContainsText(doc:Document, text: String) = assert(!doc.toString.contains(text), "\n\ntext " + text + " was rendered on the page.\n")

}
