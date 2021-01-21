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

package iht.models.application.basicElements

import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class EstateElementTest extends UnitSpec with MockitoSugar{
  "EstateElement" must {
    "return true if it has a value" in {
      val estateElement = new EstateElement {
        override val value = Some(BigDecimal(55.4))
      }
      estateElement.isValueEntered shouldBe true
    }
    "return false if it has no value" in {
      val estateElement = new EstateElement {
        override val value = None
      }
      estateElement.isValueEntered shouldBe false
    }
  }
}
