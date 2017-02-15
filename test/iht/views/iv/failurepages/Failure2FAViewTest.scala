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

package iht.views.iv.failurepages

import iht.testhelpers.CommonBuilder
import iht.views.html.iv.failurepages.failure_2fa
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages
import play.api.mvc.Call

class Failure2FAViewTest extends GenericNonSubmittablePageBehaviour {
  def guidanceParagraphs = Set(
    Messages("page.iht.iv.failure.2fa.p1"),
    Messages("page.iht.iv.failure.2fa.p2")
  )

  def pageTitle = Messages("page.iht.iv.failure.2fa.title")

  def browserTitle = Messages("page.iht.iv.failure.2fa.title")

  def view: String = failure_2fa(CommonBuilder.DefaultCall1.url).toString

  override def exitComponent = Some(
    ExitComponent(
      Call("GET", "https://www.gov.uk/inheritance-tax"),
      Messages("page.iht.iv.failure.2fa.exitLink")
    )
  )

  "Failure 2FA View" must {
    behave like nonSubmittablePage()

    "show the verify link with the correct target and text" in {
      val cancelButton = doc.getElementById("verify-link")
      cancelButton.attr("href") shouldBe CommonBuilder.DefaultCall1.url
      cancelButton.text() shouldBe Messages("page.iht.iv.failure.2fa.verifyLink")
    }
  }
}
