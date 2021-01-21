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

package iht.models.application.assets

import iht.testhelpers.CommonBuilder
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec


class PrivatePensionTest extends UnitSpec with MockitoSugar{

  "isComplete" must {
    "returns Some(true) when PrivatePension is complete" in {

      val completePP = CommonBuilder.buildPrivatePensionExtended.copy(isChanged = Some(false),
        value = Some(BigDecimal(1000)), isOwned = Some(true))

      completePP.isComplete shouldBe Some(true)
    }

    "returns Some(false) when PrivatePension is not complete" in {
      val inCompletePP = CommonBuilder.buildPrivatePensionExtended.copy(isChanged = None,
        value = Some(BigDecimal(1000)), isOwned = Some(true))

      inCompletePP.isComplete shouldBe Some(false)
    }

    "returns None when there is no PrivatePension" in {
      val pensionWithAllNoneValues = CommonBuilder.buildPrivatePensionExtended

      pensionWithAllNoneValues.isComplete shouldBe empty
    }
  }

}
