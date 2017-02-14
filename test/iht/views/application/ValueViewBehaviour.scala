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

package iht.views.application

import iht.utils.CommonHelper
import play.api.i18n.Messages
import play.api.mvc.Call

trait ValueViewBehaviour[A] extends ApplicationPageBehaviour[A] {
  def pageTitle: String

  def browserTitle: String

  def guidanceParagraphs: Set[String]

  def formTarget: Call

  def cancelTarget: Option[Call] = None

  def cancelContent: Option[String] = None

  /**
    * Assumes that the Call for the continue button has been set up as CommonBuilder.DefaultCall1.
    */
  def valueView() = {
    applicationPageWithErrorSummaryBox()

    "show the correct guidance paragraphs" in {
      for (paragraph <- guidanceParagraphs) messagesShouldBePresent(view, paragraph)
    }

    "have a value input field" in {
      Option(doc.getElementById("value")).isDefined shouldBe true
    }

    "show the Save/Continue button with the correct target" in {
      doc.getElementsByTag("form").attr("action") shouldBe formTarget.url
    }

    "show the return link with the correct target and text if applicable" in {
      cancelTarget.foreach { target =>
        val cancelButton = doc.getElementById("return-button")
        cancelButton.attr("href") shouldBe target.url
        cancelButton.text() shouldBe CommonHelper.getOrException(cancelContent)
      }
    }
  }
}
