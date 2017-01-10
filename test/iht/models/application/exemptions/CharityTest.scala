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

package iht.models.application.exemptions

import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by vineet on 06/11/16.
  */
class CharityTest extends UnitSpec with MockitoSugar{

  "isComplete" must {

    "return true when Charity is complete" in {
      val charity = CommonBuilder.buildCharity.copy(id = Some("1"),
        name = Some("test"),
        number = Some("121212"),
        totalValue = Some(BigDecimal(1000)))

      charity.isComplete shouldBe true
    }

    "return false when all but one of fields is None" in {
      val charity = CommonBuilder.buildCharity.copy(
        id = Some("1"),
        name = Some("test"),
        number = Some("121212"),
        totalValue = None)

      charity.isComplete shouldBe false
    }

    "return false when all the fields are None" in {
        CommonBuilder.buildCharity.isComplete shouldBe false
    }
  }
}
