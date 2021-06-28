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

package iht.views.application

import iht.views.ViewTestHelper
import iht.views.html.application.iht_kickout_final_application



class IhtKickoutFinalApplicationViewTest extends ViewTestHelper{

  lazy val ihtRef = "ABCABC"
  lazy val ihtKickoutFinalApplicationView: iht_kickout_final_application = app.injector.instanceOf[iht_kickout_final_application]

 def ihtKickOutFinalApplicationView = {
    implicit val request = createFakeRequest()
    val view = ihtKickoutFinalApplicationView(ihtRef).toString()
    asDocument(view)
  }

  "IhtKickoutFinalApplication View" must {

    "have no message keys in html" in {
      noMessageKeysShouldBePresent(ihtKickOutFinalApplicationView.toString)
    }

    "have the correct title and browser title" in {
      val view = ihtKickOutFinalApplicationView.toString

      titleShouldBeCorrect(view.toString, messagesApi("page.iht.application.kickout.final.browserTitle"))
      browserTitleShouldBeCorrect(view, messagesApi("page.iht.application.kickout.final.browserTitle"))
    }

    "have correct guidance" in {
      val view = ihtKickOutFinalApplicationView.toString

      messagesShouldBePresent(view, messagesApi("page.iht.application.kickout.final.getCopy.title"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.kickout.final.getCopy.guidance1"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.kickout.final.getCopy.guidance2.youShould"))
      ihtKickOutFinalApplicationView.getElementsByClass("panel").first.text() mustBe
        (messagesApi("page.iht.application.kickout.final.getCopy.guidance2.youShould") + " " +
          messagesApi("page.iht.application.kickout.final.getCopy.guidance2.saveAndPrint") +
          ".")
      messagesShouldBePresent(view, messagesApi("page.iht.application.kickout.final.getCopy.guidance3"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.kickout.final.guidance.onFinish"))
    }

    "have save and print link with correct text " in {
      val view = ihtKickOutFinalApplicationView
      val returnButton = view.getElementById("save-and-print")
      returnButton.text() mustBe messagesApi("page.iht.application.kickout.final.getCopy.guidance2.saveAndPrint")
      returnButton.attr("href") mustBe
        iht.controllers.application.pdf.routes.PDFController.onPreSubmissionPDF.url
    }

    "have return link with correct text " in {
      val view = ihtKickOutFinalApplicationView

      val returnButton = view.getElementById("return-button")
      returnButton.text() mustBe messagesApi("iht.estateReport.returnToEstateOverview")
      returnButton.attr("href") mustBe
        iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef).url
    }

    "have finish and delete button " in {
      val view = ihtKickOutFinalApplicationView

      val finishButton = view.getElementById("finish")
      finishButton.attr("value") mustBe messagesApi("iht.finishAndDeleteThisEstateReport")
    }
  }

}
