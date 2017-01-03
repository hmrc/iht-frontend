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

package iht.models.application.debts

import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by vineet on 06/11/16.
  */
class MortgageTest extends UnitSpec with MockitoSugar{

  "isComplete" must {

    "return Some(true) when Mortgage is complete" in {
      val mortgage = CommonBuilder.buildMortgage

      mortgage.isComplete shouldBe Some(true)
    }

    "return Some(true) when Mortgage has isOwned as false" in {
      val mortgage = CommonBuilder.buildMortgage.copy(
        isOwned = Some(false), value = None)

      mortgage.isComplete shouldBe Some(true)
    }

    "return Some(false) when one of the fields is None" in {
      val mortgage = CommonBuilder.buildMortgage.copy(
        isOwned = Some(true),
        value = None)

      mortgage.isComplete shouldBe Some(false)
    }

    "return None when value and isOwned fields are None" in {
      val mortgage = CommonBuilder.buildMortgage.copy(isOwned = None, value = None)
      mortgage.isComplete shouldBe empty
    }
  }
}
