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

package iht.models.application.debts

import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class MortgageEstateElementTest extends UnitSpec with MockitoSugar{

  "totalValue" must {

    "return none if mortgages list is empty" in {
      val mortgage = MortgageEstateElement( isOwned = Some(true),
        mortgageList = Nil)
      mortgage.totalValue shouldBe None
    }

    "return none if mortgages list has elements but no values" in {
      val mortgage = MortgageEstateElement(isOwned = Some(true),
        mortgageList = List(
          Mortgage(
            id = "1",
            value = None,
            isOwned = Some(true)
        )))
      mortgage.totalValue shouldBe None
    }

    "return sum of the mortgages values" in {
      val mortgage = MortgageEstateElement(isOwned = Some(true),
        mortgageList = List(
          Mortgage(id = "1", value = Some(10000), isOwned = Some(true)),
            Mortgage(id = "2", value = Some(20000), isOwned = Some(true)
          )))
      mortgage.totalValue shouldBe Some(30000)
    }
  }
}
