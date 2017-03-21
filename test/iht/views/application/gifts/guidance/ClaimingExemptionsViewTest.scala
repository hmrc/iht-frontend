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

package iht.views.application.gifts.guidance

import iht.controllers.application.gifts.guidance.routes
import iht.views.ViewTestHelper
import iht.views.html.application.gift.guidance.claiming_exemptions
import play.api.i18n.Messages.Implicits._

class ClaimingExemptionsViewTest extends ViewTestHelper {

  def claimingExemptionsView() = {
    implicit val request = createFakeRequest()

    val view = claiming_exemptions("ABC123",
                                   Some(iht.controllers.application.gifts.routes.GivenAwayController.onPageLoad.url),
                                    Some("site.backToLastQuestion.values.link"),
                                    Some("site.backToLastQuestion.values.link")).toString()
    asDocument(view)
  }

  "ClaimingExemptions view" must {

    "have no message keys in html" in {
      val view = claimingExemptionsView().toString
      noMessageKeysShouldBePresent(view)
    }

    "have correct title" in {
      val view = claimingExemptionsView().toString

      messagesShouldBePresent(view, messagesApi("page.iht.application.gifts.guidance.claimingExemptions.title"))
    }

    "have correct guidance paragraphs" in {
      val view = claimingExemptionsView().toString
      messagesShouldBePresent(view, messagesApi("page.iht.application.gifts.guidance.claimingExemptions.description1"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.gifts.guidance.claimingExemptions.description2"))
      messagesShouldBePresent(view, messagesApi("page.iht.application.gifts.guidance.claimingExemptions.description3"))
    }

    "have correct 'Gifts that can be claimed using the small gifts exemption' guidance" in {
      val view = claimingExemptionsView()
      assertEqualsValue(view, "summary#small-gifts-summary-heading",
                              messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.smallGifts.heading"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.smallGifts.description1"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.smallGifts.description2"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.smallGifts.description3"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.smallGifts.description4"))
    }

    "have correct 'Gifts that can be claimed using the marriage or civil partnership gift exemption' guidance" in {
      val view = claimingExemptionsView()
      assertEqualsValue(view, "summary#marriage-civil-partnership-gifts-summary-heading",
                   messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.marriageOrCivilPartner.heading"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.marriageOrCivilPartner.description1"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.marriageOrCivilPartner.description2"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.marriageOrCivilPartner.description3"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.marriageOrCivilPartner.description3.part1"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.marriageOrCivilPartner.description3.part2"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.marriageOrCivilPartner.description3.part3"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.marriageOrCivilPartner.description4"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.marriageOrCivilPartner.description4.part1"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.marriageOrCivilPartner.description4.part2"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.marriageOrCivilPartner.description5"))

    }

    "have correct 'Gifts that can be claimed using the gifts out of income exemption' guidance" in {
      val view = claimingExemptionsView()
      assertEqualsValue(view, "summary#gifts-out-of-income-exemption-summary-heading",
                   messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.income.heading"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.income.description1"))
      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.income.description2"))
      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.income.description3"))
      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.income.description4"))
      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.income.description5"))
      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.income.description6"))

    }

    "have correct 'Gifts that can be claimed using the annual gift exemption' guidance" in {
      val view = claimingExemptionsView()
      assertEqualsValue(view, "summary#annual-gift-exemption-summary-heading",
                   messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.annualGift.heading"))

      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.annualGift.description1"))
      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.annualGift.description2"))
      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.annualGift.description3"))
      messagesShouldBePresent(view.toString,
        messagesApi("page.iht.application.gifts.guidance.claimingExemptions.reveal.annualGift.description4"))
    }

    "have the correct navigation links" in {
      val view = claimingExemptionsView()
      val previousLink = view.getElementById("continue-to-previous")
      previousLink.attr("href") shouldBe routes.WithReservationController.onPageLoad().url

      val previousLinkLabel = view.getElementById("continue-to-previous")
      val previousLinkText = previousLinkLabel.getElementsByTag("span").get(0)
      previousLinkText.text shouldBe messagesApi("site.previous")

      val previousLinkTitle = previousLinkLabel.getElementsByTag("span").get(1)
      previousLinkTitle.text shouldBe messagesApi("iht.estateReport.gifts.withReservation.title")

      val nextLink = view.getElementById("continue-to-next")
      nextLink.attr("href") shouldBe routes.IncreasingAnnualLimitController.onPageLoad().url

      val nextLinkLabel = view.getElementById("continue-to-next")
      val nextLinkText = nextLinkLabel.getElementsByTag("span").get(0)
      nextLinkText.text shouldBe messagesApi("iht.next")

      val nextLinkTitle = nextLinkLabel.getElementsByTag("span").get(1)
      nextLinkTitle.text shouldBe messagesApi("page.iht.application.gifts.guidance.increasingAnnualLimit.title")

    }
  }

}
