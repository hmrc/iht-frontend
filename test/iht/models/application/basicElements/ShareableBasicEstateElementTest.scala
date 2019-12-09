/*
 * Copyright 2019 HM Revenue & Customs
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

package iht.models.application.basicElements

import iht.testhelpers.CommonBuilder
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec


class ShareableBasicEstateElementTest extends UnitSpec with MockitoSugar{
  "isComplete" must {

    "return Some(true) if share yes no answered false" in {
      val shareableBasicEstateElement = ShareableBasicEstateElement(
        Some(BigDecimal(1)), Some(BigDecimal(1)), Some(false), Some(true)
      )
      shareableBasicEstateElement.isComplete shouldBe Some(true)
    }

    "return Some(true) if non share yes no answered false" in {
      val shareableBasicEstateElement = ShareableBasicEstateElement(
        Some(BigDecimal(1)), Some(BigDecimal(1)), Some(true), Some(false)
      )
      shareableBasicEstateElement.isComplete shouldBe Some(true)
    }

    "return Some(true) if ShareableBasicEstateElement is complete" in {
      val estateElement = CommonBuilder.buildShareableBasicElementExtended.copy(
        value = Some(BigDecimal(1000)),
        shareValue = Some(BigDecimal(1000)),
        isOwned = Some(true),
        isOwnedShare = Some(true))

      estateElement.isComplete shouldBe Some(true)
    }

    "return Some(false) if ShareableBasicEstateElement is not complete" in {
      val estateElement = CommonBuilder.buildShareableBasicElementExtended.copy(
        value = Some(BigDecimal(1000)),
        shareValue = Some(BigDecimal(1000)),
        isOwned = None,
        isOwnedShare = None)

      estateElement.isComplete shouldBe Some(false)
    }

    "return None if every field is None in ShareableBasicEstateElement" in {
      val estateElement = CommonBuilder.buildShareableBasicElementExtended

      estateElement.isComplete shouldBe empty
    }
  }

  "totalValue" must {
    "return correct total value" in {
      val shareableBasicElement = CommonBuilder.buildShareableBasicElement.copy(value = Some(BigDecimal(10000)),
        shareValue = Some(BigDecimal(20000)))

      shareableBasicElement.totalValue shouldBe Some(BigDecimal(30000))
    }

    "returns None, if value and sharedValue are None" in {
      val shareableBasicElement = CommonBuilder.buildShareableBasicElement

      shareableBasicElement.totalValue shouldBe empty
    }
  }

  "isValueEntered" must {
    "return true if both value and shareValue or any of it are entered " in {
      val shareableBasicElement = CommonBuilder.buildShareableBasicElement.copy(value = Some(BigDecimal(10000)),
        shareValue = None)

      shareableBasicElement.isValueEntered shouldBe true
    }

    "return false if value and sharedValue are not entered" in {
      val shareableBasicElement = CommonBuilder.buildShareableBasicElement

      shareableBasicElement.isValueEntered shouldBe false
    }
  }
}
