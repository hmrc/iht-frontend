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

package iht.models.basicElements

import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by vineet on 03/11/16.
  */

class ShareableBasicEstateElementTest extends UnitSpec with MockitoSugar{

  "isComplete" must {

    "returns Some(true) if ShareableBasicEstateElement is complete" in {
      val estateElement = CommonBuilder.buildShareableBasicElementExtended.copy(
        value = Some(BigDecimal(1000)),
        shareValue = Some(BigDecimal(1000)),
        isOwned = Some(true),
        isOwnedShare = Some(true))

      estateElement.isComplete shouldBe Some(true)
    }

    "returns Some(false) if ShareableBasicEstateElement is not complete" in {
      val estateElement = CommonBuilder.buildShareableBasicElementExtended.copy(
        value = Some(BigDecimal(1000)),
        shareValue = Some(BigDecimal(1000)),
        isOwned = None,
        isOwnedShare = None)

      estateElement.isComplete shouldBe Some(false)
    }

    "returns None if every field is None in ShareableBasicEstateElement" in {
      val estateElement = CommonBuilder.buildShareableBasicElementExtended

      estateElement.isComplete shouldBe empty
    }
  }
}
