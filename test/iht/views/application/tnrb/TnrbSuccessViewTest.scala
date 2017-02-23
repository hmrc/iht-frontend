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

package iht.views.application.tnrb

import iht.controllers.application.tnrb.routes
import iht.utils.CommonHelper
import iht.views.ViewTestHelper
import iht.views.html.application.tnrb.tnrb_success
import org.jsoup.nodes.Element
import play.api.i18n.Messages.Implicits._

class TnrbSuccessViewTest extends ViewTestHelper {

  lazy val deceasedName = "Test test"
  lazy val preDeceasedName = "Test1 test1"
  lazy val ihtReference = "ABC12344"

  def tnrbSuccessView() = {
    implicit val request = createFakeRequest()

    val view = tnrb_success(deceasedName, preDeceasedName, ihtReference).toString
    asDocument(view)
  }

  "TnrbSuccess View" must {

    "show correct title and browserTitle" in {
      val view = tnrbSuccessView.toString
      titleShouldBeCorrect(view, messagesApi("page.iht.application.tnrbEligibilty.increasedTnrbThreshold.title"))
      browserTitleShouldBeCorrect(view, messagesApi("page.iht.application.tnrbEligibilty.increasedTnrbThreshold.browserTitle"))
    }

    "show the correct paragraphs" in {
      val view = tnrbSuccessView.toString

      messagesShouldBePresent(view,
        messagesApi("page.iht.application.tnrbEligibilty.increasedTnrbThreshold.paragraph1", deceasedName, preDeceasedName))

      messagesShouldBePresent(view, messagesApi("page.iht.application.tnrbEligibilty.increasedTnrbThreshold.paragraph2"))
    }

    "show a return button to estate overview button with correct text" in {
      val view = tnrbSuccessView.toString
      val doc = asDocument(view)


      val button: Element = doc.getElementById("continue-to-estate-overview")
      button.text() shouldBe messagesApi("iht.estateReport.returnToEstateOverview")
      button.className() shouldBe "button"
      button.attr("href") shouldBe
        iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtReference).url
   }

    "show the return link to Tnrb Overview page" in {
      val view = tnrbSuccessView.toString
      val doc = asDocument(view)

      val link = doc.getElementById("return-button")
      link.text() shouldBe messagesApi("page.iht.application.tnrbEligibilty.returnToTnrbEligibilty")
      link.attr("href") shouldBe routes.TnrbOverviewController.onPageLoad().url
    }

  }
}
