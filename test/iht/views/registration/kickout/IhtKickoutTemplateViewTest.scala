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

package iht.views.registration.kickout

import iht.views.ViewTestHelper
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig
import play.twirl.api.Html
import iht.views.html.registration.kickout.iht_kickout_template


class IhtKickoutTemplateViewTest extends ViewTestHelper{

  val title = "selected title"
  val summaryMessage = "kickout summary message"
  val returnLinkUrl = iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onPageLoad

  def ihtKickOutTemplateView() = {
    implicit val request = createFakeRequest()

    val view = iht_kickout_template(title,
                        summaryMessage,
                        returnLinkUrl,
                        false)(Html("")).toString()
    asDocument(view)
  }

  "IhtKickoutApplication View" must {

    "have no message keys in html" in {
      val view = ihtKickOutTemplateView().toString
      noMessageKeysShouldBePresent(view)
    }

    "have the correct title and summary message" in {
      val view = ihtKickOutTemplateView()

      titleShouldBeCorrect(view.toString, title)
      browserTitleShouldBeCorrect(view.toString, title)
      messagesShouldBePresent(view.toString, summaryMessage)
    }

    "have 'Next steps' heading" in {
      val view = ihtKickOutTemplateView
      val headers = view.getElementsByTag("h2")

      headers.first.text() mustBe messagesApi("iht.nextSteps")
    }

    "have details are correct button " in {
      val view = ihtKickOutTemplateView

      val detailsAreCorrectButton = view.getElementById("finish")
      detailsAreCorrectButton.attr("value") mustBe messagesApi("site.button.details.correct.exitToGovK")
    }

    "have return link with correct text" in {
      val view = ihtKickOutTemplateView

      val detailsAreCorrectButton = view.getElementById("return-button")
      detailsAreCorrectButton.attr("href") mustBe returnLinkUrl.url
      detailsAreCorrectButton.text mustBe messagesApi("iht.registration.kickout.message.returnToLast")
    }
  }

}
