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

import iht.views.html.application.gift.guidance.gifts_given_away
import play.api.i18n.Messages.Implicits._

class GiftsGivenAwayViewTest extends GiftsGuidancePageBehaviour {

  override def guidance = guidance(
    Set(
      messagesApi("page.iht.application.gifts.guidance.giftsGivenAway.description1"),
      messagesApi("page.iht.application.gifts.guidance.giftsGivenAway.description2"),
      messagesApi("page.iht.application.gifts.guidance.giftsGivenAway.description3"),
      messagesApi("page.iht.application.gifts.guidance.giftsGivenAway.description4")
    )
  )

  override def view = gifts_given_away(
    ihtReference = "",
    backToLastQuestionUrl = Some(backToLastQuestionUrl),
    backToLastQuestionMessageKey = Some(backToLastQuestionMessageKey),
    backToLastQuestionMessageKeyAccessibility = None
  ).toString

  override def formTarget = Some(iht.controllers.application.gifts.guidance.routes.GiftsGivenAwayController.onSubmit())

  override def cancelComponent = None

  override val continueId: String = "continue"

  override val continueContent: String = "page.iht.application.gifts.guidance.button.continueToGifts"

  "Gifts Given Away View" must {
    behave like guidancePage()

    "contain the correct guidance section title" in {
      doc.getElementById("guidance-section-title").text shouldBe messagesApi("page.iht.application.gifts.guidance.giftsGivenAway.title")
    }

    behave like link("continue-to-previous",
      iht.controllers.application.gifts.guidance.routes.IncreasingAnnualLimitController.onPageLoad().url,
      messagesApi("site.previous") + " " + messagesApi("page.iht.application.gifts.guidance.increasingAnnualLimit.title"))
  }
}
