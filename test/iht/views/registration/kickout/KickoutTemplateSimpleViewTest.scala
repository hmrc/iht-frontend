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

package iht.views.registration.kickout

import iht.views.ViewTestHelper
import iht.views.html.registration.kickout.kickout_template_simple


class KickoutTemplateSimpleViewTest extends ViewTestHelper{

  val returnLinkUrl = iht.controllers.registration.applicant.routes.ExecutorOfEstateController.onPageLoad
  val Contents = messagesApi("page.iht.registration.notAnExecutor.kickout.p1")
  lazy val kickoutTemplateSimpleView: kickout_template_simple = app.injector.instanceOf[kickout_template_simple]

  def kickOutTemplateView() = {
    implicit val request = createFakeRequest()

    val view = kickoutTemplateSimpleView(returnLinkUrl, "Change your answer")(Contents).toString()
    asDocument(view)
  }

  "KickoutTemplateView View" must {
    "have the correct title" in {
      val view = kickOutTemplateView()

      titleShouldBeCorrect(view.toString, messagesApi("iht.notPossibleToUseService"))
      browserTitleShouldBeCorrectRegistration(view.toString, messagesApi("iht.notPossibleToUseService"))
      messagesShouldBePresent(view.toString, Contents)
    }

    "have the contents" in {
      val view = kickOutTemplateView
      view.toString must include(Contents)

    }

    "have details are correct button " in {
      val view = kickOutTemplateView

      val detailsAreCorrectButton = view.getElementById("finish")
      detailsAreCorrectButton.attr("value") mustBe messagesApi("iht.exitToGovUK")
    }

    "have return link with correct text" in {
      val view = kickOutTemplateView

      val changeYourAnswerLink = view.getElementById("return-button")
      changeYourAnswerLink.attr("href") mustBe returnLinkUrl.url
      changeYourAnswerLink.text mustBe "Change your answer"
    }
  }

}
