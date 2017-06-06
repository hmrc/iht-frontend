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

import iht.views.html.application.gift.guidance.kinds_of_gifts
import play.api.i18n.Messages.Implicits._

class KindsOfGiftsViewTest extends GiftsGuidancePageBehaviour {
  override def guidance = guidance(
    Set(
      messagesApi("page.iht.application.gifts.guidance.kindOfGifts.description1"),
      messagesApi("page.iht.application.gifts.guidance.kindOfGifts.description2"),
      messagesApi("page.iht.application.gifts.guidance.kindOfGifts.description3"),
      messagesApi("iht.estateReport.assets.money.lowerCaseInitial"),
      messagesApi("iht.estateReport.gifts.stocksAndSharesListed"),
      messagesApi("page.iht.application.gifts.guidance.kindOfGifts.description3.part3"),
      messagesApi("page.iht.application.gifts.guidance.kindOfGifts.description3.part4"),
      messagesApi("page.iht.application.gifts.guidance.kindOfGifts.description4"),
      messagesApi("page.iht.application.gifts.guidance.kindOfGifts.description5")
    )
  )

  override def view = kinds_of_gifts(
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
      doc.getElementById("guidance-section-title").text shouldBe messagesApi("page.iht.application.gifts.guidance.kindOfGifts.title")
    }

    behave like link("continue-to-previous",
      iht.controllers.application.gifts.guidance.routes.WhatIsAGiftController.onPageLoad().url,
      messagesApi("site.previous") + " " + messagesApi("page.iht.application.gifts.guidance.whatsAGift.title"))

    behave like link("continue-to-next",
      iht.controllers.application.gifts.guidance.routes.WithReservationController.onPageLoad().url,
      messagesApi("iht.next") + " " + messagesApi("iht.estateReport.gifts.withReservation.titleWithoutName"))
  }
}
