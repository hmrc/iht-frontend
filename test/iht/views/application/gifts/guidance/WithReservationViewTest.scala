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

import iht.views.application.ApplicationPageBehaviour
import iht.views.html.application.gift.guidance.{what_is_a_gift, with_reservation}
import play.api.i18n.Messages.Implicits._

class WithReservationViewTest extends GiftsGuidancePageBehaviour {

  override def guidance = guidance(
    Set(
      messagesApi("page.iht.application.gifts.guidance.withReservation.description1"),
      messagesApi("page.iht.application.gifts.guidance.withReservation.description2"),
      messagesApi("page.iht.application.gifts.guidance.withReservation.description3"),
      messagesApi("page.iht.application.gifts.guidance.withReservation.description4")
    )
  )

  override def view = with_reservation(
    ihtReference = "",
    backToLastQuestionUrl = Some(backToLastQuestionUrl),
    backToLastQuestionMessageKey = Some(backToLastQuestionMessageKey),
    backToLastQuestionMessageKeyAccessibility = None
  ).toString

  override def formTarget = None

  override def cancelComponent = None

  "Kinds of Gifts View" must {
    behave like guidancePage()

    "contain the correct guidance section title" in {
      doc.getElementById("guidance-section-title").text shouldBe messagesApi("iht.estateReport.gifts.withReservation.title")
    }

    behave like link("continue-to-previous",
      iht.controllers.application.gifts.guidance.routes.KindsOfGiftsController.onPageLoad().url,
      messagesApi("site.previous") + " " + messagesApi("page.iht.application.gifts.guidance.kindOfGifts.title"))

    behave like link("continue-to-next",
      iht.controllers.application.gifts.guidance.routes.ClaimingExemptionsController.onPageLoad().url,
      messagesApi("iht.next") + " " + messagesApi("page.iht.application.gifts.guidance.claimingExemptions.title"))
  }
}
