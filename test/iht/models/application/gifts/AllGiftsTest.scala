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

package iht.models.application.gifts

import iht.testhelpers.CommonBuilder
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec


class AllGiftsTest extends UnitSpec with MockitoSugar{

  "isGiftsSectionCompletedWithNoValue" must {

    "returns true if user selects No for all the gift question" in {
      val gifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(false),
        isReservation  = Some(false), isToTrust = Some(false), isGivenInLast7Years = Some(false))

      gifts.isGiftsSectionCompletedWithNoValue shouldBe true
    }

    "returns false if user does not select No for all the gift question" in {
      val gifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(false),
        isReservation  = Some(true), isToTrust = Some(false), isGivenInLast7Years = Some(false))

      gifts.isGiftsSectionCompletedWithNoValue shouldBe false
    }
  }

  "isStarted" must {
    "return true if isGivenAway field has some value" in {
      val gifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(false))
      gifts.isStarted shouldBe true
    }

    "return false if isGivenAway question has not been answered" in {
      val gifts = CommonBuilder.buildAllGifts
      gifts.isStarted shouldBe false
    }
  }

}
