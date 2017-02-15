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

package iht.models.application.assets

import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class HouseholdTest extends UnitSpec with MockitoSugar {

  "totalValue" must {

    "returns correct total value of household" in {
      val shareableBasicElement = CommonBuilder.buildShareableBasicElement.copy(value = Some(BigDecimal(10000)),
        shareValue = Some(BigDecimal(20000)))

      shareableBasicElement.totalValue shouldBe Some(BigDecimal(30000))
    }

    "returns None, if values for listed and notListed are None" in {
      val shareableBasicElement = CommonBuilder.buildShareableBasicElement

      shareableBasicElement.totalValue shouldBe empty
    }
  }

  "isValueEntered" must {

    "returns true if both value and shareValue or any of it are entered " in {
      val shareableBasicElement = CommonBuilder.buildShareableBasicElement.copy(value = Some(BigDecimal(10000)),
        shareValue = None)

      shareableBasicElement.isValueEntered shouldBe true
    }

    "returns false if values for listed and notListed shares are not entered" in {
      val shareableBasicElement = CommonBuilder.buildShareableBasicElement

      shareableBasicElement.isValueEntered shouldBe false
    }
  }

  "isComplete" must {

    "returns Some(true) if Household is complete" in {
      val shareableBasicElement = CommonBuilder.buildShareableBasicElement.copy(isOwned = Some(true), value = Some(BigDecimal(10000)),
        isOwnedShare = Some(false), shareValue = None)

      shareableBasicElement.isComplete shouldBe Some(true)
    }

    "returns Some(false) if Household is not complete" in {
      val shareableBasicElement = CommonBuilder.buildShareableBasicElement.copy(isOwned = Some(true), value = Some(BigDecimal(10000)),
        isOwnedShare = Some(true), shareValue = None)

      shareableBasicElement.isComplete shouldBe Some(false)
    }

    "returns None if every field is None in Household" in {
      val shareableBasicElement = CommonBuilder.buildShareableBasicElement
      shareableBasicElement.isComplete shouldBe empty
    }
  }
}
