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

package iht.views.application.status

import iht.views.ExitComponent
import iht.views.html.application.status.in_review_application
import play.api.i18n.Messages.Implicits._

class InReviewApplicationViewTest extends ApplicationStatusViewBehaviour {

  override def sidebarTitle: String = messagesApi("page.iht.application.overview.inreview.sidebartitle")

  def guidanceParagraphs = commonGuidanceParagraphs

  def pageTitle = messagesApi("page.iht.application.overview.inreview.title")

  def browserTitle = messagesApi("page.iht.application.overview.inreview.title")

  def view: String = in_review_application(ihtRef, deceasedName, probateDetails)(createFakeRequest(), applicationMessages).toString

  override val exitId: String = "return-link"

  override def exitComponent = None

  "In Review Application View" must {
    behave like applicationStatusPage()

    "show submit button with correct target and text" in {
      doc.getElementsByTag("form").attr("action") shouldBe iht.controllers.home.routes.IhtHomeController.onPageLoad().url
      val submitButton = doc.getElementById("return-input")
      submitButton.`val` shouldBe messagesApi("page.iht.application.overview.common.return")
    }
  }
}
