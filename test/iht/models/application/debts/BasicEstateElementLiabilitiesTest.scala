/*
 * Copyright 2022 HM Revenue & Customs
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

package iht.models.application.debts

import iht.testhelpers.CommonBuilder
import org.scalatestplus.mockito.MockitoSugar
import common.CommonPlaySpec


class BasicEstateElementLiabilitiesTest extends CommonPlaySpec with MockitoSugar{

  "isComplete" must {

    "return Some(true) when ElementLiability is complete" in {
      val estateElementLiability = CommonBuilder.buildBasicEstateElementLiabilities.copy(
        isOwned = Some(true),
        value = Some(BigDecimal(10000)))

      estateElementLiability.isComplete shouldBe Some(true)
    }

    "return Some(true) when ElementLiability has isOwned as false" in {
      val estateElementLiability = CommonBuilder.buildBasicEstateElementLiabilities.copy(
        isOwned = Some(false), value = None)

      estateElementLiability.isComplete shouldBe Some(true)
    }

    "return Some(false) when one of the fields is None" in {
      val estateElementLiability = CommonBuilder.buildBasicEstateElementLiabilities.copy(
        isOwned = Some(true),
        value = None)

      estateElementLiability.isComplete shouldBe Some(false)
    }

    "return None when both the fields are None" in {
      val estateElementLiability = CommonBuilder.buildBasicEstateElementLiabilities.copy(None, None)
      estateElementLiability.isComplete shouldBe empty
    }
  }
}
