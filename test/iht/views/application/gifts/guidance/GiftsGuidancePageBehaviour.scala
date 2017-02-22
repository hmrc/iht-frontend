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

import iht.testhelpers.CommonBuilder
import iht.views.application.ApplicationPageBehaviour

trait GiftsGuidancePageBehaviour extends ApplicationPageBehaviour {

  val backToLastQuestionUrl: String = CommonBuilder.DefaultCall1.url
  val backToLastQuestionMessageKey: String = "test"

  override def pageTitle = messagesApi("page.iht.application.gifts.guidance.title")

  override def browserTitle = messagesApi("page.iht.application.gifts.guidance.title")

  def guidancePage() = {
    applicationPage()

    link("return-to-last-question",
      backToLastQuestionUrl,
      backToLastQuestionMessageKey)

    link("guidance1",
      iht.controllers.application.gifts.guidance.routes.WhatIsAGiftController.onPageLoad().url,
      messagesApi("page.iht.application.gifts.guidance.whatsAGift.title"))

    link("guidance2",
      iht.controllers.application.gifts.guidance.routes.KindsOfGiftsController.onPageLoad().url,
      messagesApi("page.iht.application.gifts.guidance.kindOfGifts.title"))

    link("guidance3",
      iht.controllers.application.gifts.guidance.routes.WithReservationController.onPageLoad().url,
      messagesApi("iht.estateReport.gifts.withReservation.title"))

    link("guidance4",
      iht.controllers.application.gifts.guidance.routes.ClaimingExemptionsController.onPageLoad().url,
      messagesApi("page.iht.application.gifts.guidance.claimingExemptions.title"))

    link("guidance5",
      iht.controllers.application.gifts.guidance.routes.IncreasingAnnualLimitController.onPageLoad().url,
      messagesApi("page.iht.application.gifts.guidance.increasingAnnualLimit.title"))

    link("guidance6",
      iht.controllers.application.gifts.guidance.routes.GiftsGivenAwayController.onPageLoad().url,
      messagesApi("page.iht.application.gifts.guidance.giftsGivenAway.title"))
  }
}
