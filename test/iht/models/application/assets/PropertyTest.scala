/*
 * Copyright 2016 HM Revenue & Customs
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

import iht.testhelpers.{TestHelper, CommonBuilder}
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by vineet on 03/11/16.
  */
class PropertyTest extends UnitSpec with MockitoSugar{

  "isComplete" must {
    "return true if Property is complete" in {
      val property = CommonBuilder.buildProperty.copy(id = Some("1"),
        address = Some(CommonBuilder.DefaultUkAddress),
        propertyType = TestHelper.PropertyTypeDeceasedHome,
        typeOfOwnership = TestHelper.TypesOfOwnershipDeceasedOnly,
        tenure = TestHelper.TenureFreehold,
        value = Some(BigDecimal(1000)))

      property.isCompleted shouldBe true
    }

    "return false if Property is not complete" in {
      val property = CommonBuilder.buildProperty.copy(id = Some("1"),
        address = Some(CommonBuilder.DefaultUkAddress),
        propertyType = TestHelper.PropertyTypeDeceasedHome,
        typeOfOwnership = TestHelper.TypesOfOwnershipDeceasedOnly,
        tenure = TestHelper.TenureFreehold,
        value = None)

      property.isComplete shouldBe false
    }

  }
}
