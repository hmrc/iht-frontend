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

import iht.constants.IhtProperties
import iht.views.html.iv.failurepages.failed_matching
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages.Implicits._

class FailedMatchingViewTest extends GenericNonSubmittablePageBehaviour {

  def guidanceParagraphs = Set(
    messagesApi("page.iht.iv.failure.failedMatching.p1")
  )

  def pageTitle = messagesApi("page.iht.iv.failure.failedMatching.title")

  def browserTitle = messagesApi("page.iht.iv.failure.failedMatching.title")

  def view: String = failed_matching()(createFakeRequest(), applicationMessages, formPartialRetriever).toString

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.routes.PrivateBetaLandingPageController.showLandingPage(),
      messagesApi("iht.iv.exit")
    )
  )

  "Failed Matching View" must {
    behave like nonSubmittablePage()

    "show the contact hmrc link with the correct target and text" in {
      implicit val request = createFakeRequest()
      val cancelButton = doc.getElementById("contact-hmrc")
      cancelButton.attr("href") shouldBe IhtProperties.linkContactHMRC
      cancelButton.text() shouldBe messagesApi("iht.iv.contactHMRC")
    }
  }
}
