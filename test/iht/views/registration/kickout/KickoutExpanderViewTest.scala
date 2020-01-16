/*
 * Copyright 2020 HM Revenue & Customs
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
import iht.views.html.registration.kickout.kickout_expander
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig


class KickoutExpanderViewTest extends ViewTestHelper{

  val summaryMessage = "kickout summary message"
  val returnLinkUrl = iht.controllers.registration.applicant.routes.ApplyingForProbateController.onPageLoad
  val seqOfContents = Seq("lineOne", "lineTwo")

  def kickOutTemplateView() = {
    implicit val request = createFakeRequest()

    val view = kickout_expander(summaryMessage,
      returnLinkUrl, messagesApi("iht.changeYourAnswer"))(seqOfContents).toString()
    asDocument(view)
  }

  "KickoutTemplateView View" must {
    "have the correct title and summary message" in {
      val view = kickOutTemplateView()

      titleShouldBeCorrect(view.toString, messagesApi("iht.notPossibleToUseService"))
      browserTitleShouldBeCorrect(view.toString, messagesApi("iht.notPossibleToUseService"))
      messagesShouldBePresent(view.toString, summaryMessage)
    }

    "have the sequence of contents" in {
      val view = kickOutTemplateView
      for (content <- seqOfContents) view.toString must include(content)

    }

    "have hidden text which has a summary with text: Check if the estate is excepted have hidden text" in {
      val view = kickOutTemplateView
      val hiddenTextSummary = view.select("details#checkExcepted > summary > span")
      hiddenTextSummary.text mustBe messagesApi("page.iht.registration.notApplyingForProbate.kickout.checkExcepted")
    }

    "contain 6 paragraphs" in {
      val view = kickOutTemplateView
      val hiddenTextBody = view.select("details.form-group")

      hiddenTextBody.select("p").size mustEqual 6
    }

    "contain two bulleted lists" in {
      val view = kickOutTemplateView
      val hiddenTextBody = view.select("details.form-group")

      hiddenTextBody.select("ul").size mustEqual 2
    }

    "have a link to the IHT400 form" in {
      val view = kickOutTemplateView
      val formLink = view.getElementById("iht400")

      formLink.attr("href") mustBe "https://www.gov.uk/government/publications/inheritance-tax-inheritance-tax-account-iht400"
    }


    "have Exit to Gov.UK button" in {
      val view = kickOutTemplateView

      val detailsAreCorrectButton = view.getElementById("finish")
      detailsAreCorrectButton.attr("value") mustBe messagesApi("iht.exitToGovUK")
    }

    "have return link with correct text" in {
      val view = kickOutTemplateView

      val detailsAreCorrectButton = view.getElementById("return-button")
      detailsAreCorrectButton.attr("href") mustBe returnLinkUrl.url
      detailsAreCorrectButton.text mustBe messagesApi("iht.changeYourAnswer")
    }
  }

}
