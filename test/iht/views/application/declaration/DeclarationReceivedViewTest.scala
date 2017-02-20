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
  lazy val probateDetails = CommonBuilder.buildProbateDetails

  def declarationReceivedView() = {
    implicit val request = createFakeRequest()

    val view = declaration_received(Some(probateDetails), regDetails).toString
    asDocument(view)
  }

  "DeclarationReceivedView Page" must {
   "show correct title and browserTitle" in {
     val view = declarationReceivedView().toString
     titleShouldBeCorrect(view, messagesApi("page.iht.application.declaration_received.title"))
     browserTitleShouldBeCorrect(view, messagesApi("page.iht.application.declaration_received.title"))
   }

    "show the correct text and link in 'What happens next' section" in {

      val view = declarationReceivedView().toString
      messagesShouldBePresent(view, messagesApi("page.iht.application.declaration_received.subtitle"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.probateDetails.content2"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.probateDetails.content2.bullet1",
                              formattedProbateReference(probateDetails.probateReference)))
      messagesShouldBePresent(view, CommonHelper.escapePound(messagesApi("page.iht.application.probateDetails.content2.bullet2",
                              probateDetails.grossEstateforProbatePurposes)))
      messagesShouldBePresent(view,
                              CommonHelper.escapePound(messagesApi("page.iht.application.probateDetails.content2.bullet3",
                              probateDetails.netEstateForProbatePurposes)))

      messagesShouldBePresent(view, messagesApi("page.iht.application.probateDetails.guidance"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.probateDetails.guidance.bullet1"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.probateDetails.guidance.bullet2"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.probateDetails.guidance.bullet3"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.probateDetails.content4"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.probateDetails.content4.print"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.probateDetails.content5"))

      assertRenderedById(declarationReceivedView(), "download-and-print")
    }

    "show correct text in 'HMRC will review this estate report' section" in {
      val view = declarationReceivedView().toString
      messagesShouldBePresent(view, messagesApi("page.iht.application.declaration_received.subheading1"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.declaration_received.probate.paragraph1"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.declaration_received.paragraph2"))
    }

    "show correct link and texts in 'Keeping records for the future' section" in {
      val view = declarationReceivedView()

      messagesShouldBePresent(view.toString, messagesApi("page.iht.application.declaration_received.subheading1"))

      val pdfLink = view.getElementById("pdfLink")
      pdfLink.attr("href") shouldBe iht.controllers.application.pdf.routes.PDFController.onPostSubmissionPDF().url
      pdfLink.text() shouldBe messagesApi("page.iht.application.declaration_received.paragraph5.link")

      messagesShouldBePresent(view.toString, messagesApi("page.iht.application.declaration_received.paragraph5.part2"))
    }

    "show correct text in 'Getting your statutory certificate of discharge' section" in {
      val view = declarationReceivedView().toString
      messagesShouldBePresent(view, messagesApi("page.iht.application.declaration_received.subheading2"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.declaration_received.paragraph3"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.declaration_received.paragraph4"))
    }

    "show the continue to Inheritance Tax estate reports link" in {
      val view = declarationReceivedView()

      val continue = view.getElementById("continue-to-estate-report")
      continue.attr("href") shouldBe iht.controllers.home.routes.IhtHomeController.onPageLoad.url
      continue.text() shouldBe messagesApi("page.iht.application.declaration_received.continuelink")

    }
  }

}
