/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.i18n.Messages.Implicits._

/**
  * Created by vineet on 15/11/16.
  */
class KickoutTemplateSimpleViewTest extends ViewTestHelper{

  val returnLinkUrl = iht.controllers.registration.applicant.routes.IsAnExecutorController.onPageLoad
  val Contents = messagesApi("page.iht.registration.notAnExecutor.kickout.p1")

  def kickOutTemplateView() = {
    implicit val request = createFakeRequest()

    val view = kickout_template_simple(returnLinkUrl, "Change your answer")(Contents).toString()
    asDocument(view)
  }

  "KickoutTemplateView View" must {
    "have the correct title" in {
      val view = kickOutTemplateView()

      titleShouldBeCorrect(view.toString, messagesApi("iht.notPossibleToUseService"))
      browserTitleShouldBeCorrect(view.toString, messagesApi("iht.notPossibleToUseService"))
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
