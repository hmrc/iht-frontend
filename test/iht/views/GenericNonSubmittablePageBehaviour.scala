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

package iht.views

import org.jsoup.nodes.{Document, Element}
import play.api.mvc.Call
import iht.utils.CommonHelper._

case class ExitComponent(target: Call, content: String, hash: String = "")

trait GenericNonSubmittablePageBehaviour extends ViewTestHelper {
  def guidanceParagraphs: Set[String]

  def pageTitle:String

  def browserTitle:String

  def view: String

  def exitComponent: Option[ExitComponent]

  def doc: Document = asDocument(view)

  val exitId: String = "return-button"

  def nonSubmittablePage() = {
    "have no message keys in html" in {
      noMessageKeysShouldBePresent(view)
    }

    "have the correct title" in {
      titleShouldBeCorrect(view, pageTitle)
    }

    "have the correct browser title" in {
      browserTitleShouldBeCorrect(view, browserTitle)
    }

    if (guidanceParagraphs.nonEmpty) {
      "show the correct guidance paragraphs" in {
        for (paragraph <- guidanceParagraphs) messagesShouldBePresent(view, paragraph)
      }
    }

    if (exitComponent.isDefined) {
      "show the exit link with the correct target and text" in {
        exitComponent.foreach { attrib =>
          val anchor = doc.getElementById(exitId)
          anchor.attr("href") mustBe addFragmentIdentifierToUrl(attrib.target.url, attrib.hash)
          anchor.text() mustBe attrib.content
        }
      }
    }
  }

 def nonSubmittablePageRegistration() = {
    "have no message keys in html" in {
      noMessageKeysShouldBePresent(view)
    }

    "have the correct title" in {
      titleShouldBeCorrect(view, pageTitle)
    }

    "have the correct browser title" in {
      browserTitleShouldBeCorrectRegistration(view, browserTitle)
    }

    if (guidanceParagraphs.nonEmpty) {
      "show the correct guidance paragraphs" in {
        for (paragraph <- guidanceParagraphs) messagesShouldBePresent(view, paragraph)
      }
    }

    if (exitComponent.isDefined) {
      "show the exit link with the correct target and text" in {
        exitComponent.foreach { attrib =>
          val anchor = doc.getElementById(exitId)
          anchor.attr("href") mustBe addFragmentIdentifierToUrl(attrib.target.url, attrib.hash)
          anchor.text() mustBe attrib.content
        }
      }
    }
  }


  def link(anchorId: => String, href: => String, text: => String) = {
    def anchor = doc.getElementById(anchorId)
    s"have a link with id $anchorId and correct target" in {
      anchor.attr("href") mustBe href
    }
    s"have a link with id $anchorId and correct text" in {
      getVisibleText(anchor) mustBe text
    }
  }
}
