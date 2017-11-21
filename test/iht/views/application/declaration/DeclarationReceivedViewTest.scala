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

package iht.views.application.declaration

import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.html.application.declaration.declaration_received
import play.api.i18n.Messages.Implicits._
import iht.utils._

class DeclarationReceivedViewTest extends ViewTestHelper {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)

  def declarationReceivedView() = {
    implicit val request = createFakeRequest()

    val view = declaration_received(regDetails).toString
    asDocument(view)
  }

  "DeclarationReceivedView Page" must {

    "have no message keys in html" in {
      val view = declarationReceivedView().toString
      noMessageKeysShouldBePresent(view)
    }

   "show correct title and browserTitle" in {
     val view = declarationReceivedView().toString
     titleShouldBeCorrect(view, messagesApi("page.iht.application.declaration_received.title", deceasedName))
     browserTitleShouldBeCorrect(view, messagesApi("page.iht.application.declaration_received.browserTitle"))
   }

    "show the correct pdf link in accompanying guidance" in {
      val view = declarationReceivedView().toString
      messagesShouldBePresent(view, messagesApi("page.iht.application.declaration_received.pdf.link"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.declaration_received.guidance.indent"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.declaration_received.guidance.retention"))
    }

    "show the correct text in 'Next steps' section" in {
      val view = declarationReceivedView().toString
      messagesShouldBePresent(view, messagesApi("iht.nextSteps"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.declaration_received.guidance.probate"))
    }

    "show the continue to Inheritance Tax estate reports link" in {
      val view = declarationReceivedView()
      val continue = view.getElementById("continue-to-probate-page")
      continue.attr("href") shouldBe iht.controllers.application.declaration.routes.ProbateApplicationFormDetailsController.onPageLoad().url
      continue.text() shouldBe messagesApi("page.iht.application.declaration_received.continue")
    }
  }

}
