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

package iht.forms.application.gifts

import iht.FakeIhtApp
import iht.forms.ApplicationForms._
import iht.forms.FormTestHelper
import iht.models.application.gifts.{AllGifts, PreviousYearsGifts}

class GiftFormsTest extends FormTestHelper with FakeIhtApp {
  "giftsGivenAwayForm" must {
    behave like yesNoQuestion[AllGifts]("isGivenAway", giftsGivenAwayForm, _.isGivenAway, "error.giftsGivenAway.select")
  }

  "giftWithReservationFromBenefitForm" must {
    behave like yesNoQuestion[AllGifts]("reservation.isReservation",
      giftWithReservationFromBenefitForm, _.isReservation, "error.giftWithReservationFromBenefit.select")
  }

  "giftSevenYearsGivenInLast7YearsForm" must {
    behave like yesNoQuestion[AllGifts]("givenInPast.isGivenInLast7Years",
      giftSevenYearsGivenInLast7YearsForm, _.isGivenInLast7Years, "error.giftSevenYearsGivenInLast7Years.select")
  }

  "giftSevenYearsToTrustForm" must {
    behave like yesNoQuestion[AllGifts]("trust.isToTrust",
      giftSevenYearsToTrustForm, _.isToTrust, "error.giftSevenYearsToTrust.select")
  }

  "previousYearsGiftsForm.value" must {
    behave like currencyValue[PreviousYearsGifts](
      "value",
      previousYearsGiftsForm
    )
  }

  "previousYearsGiftsForm.exemptions" must {
    behave like currencyValue[PreviousYearsGifts](
      "exemptions",
      previousYearsGiftsForm
    )
  }

  "previousYearsGiftsForm" must {

    "display error if value < exemptions" in {
      val expectedErrors = error("exemptions", "error.giftsDetails.exceedsGivenAway")
      val data = Map("yearId" -> "1", "value" -> "10000", "exemptions" -> "11000", "startDate" -> "", "endDate" -> "")

      checkForError(previousYearsGiftsForm, data, expectedErrors)
    }
  }
}
