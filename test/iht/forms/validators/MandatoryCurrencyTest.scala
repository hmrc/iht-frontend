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

package iht.forms.validators

import iht.FakeIhtApp
import play.api.data.FormError
import uk.gov.hmrc.play.test.UnitSpec

class MandatoryCurrencyTest extends UnitSpec with FakeIhtApp {

  "MandatoryCurrency" must {
    val mandatoryCurrency = MandatoryCurrencyForOptions("length",
                                              "invalidChars",
                                              "incorrectPence",
                                              "hasSpaces",
                                              "blankValue",
                                              "hasCommaAtInvalidPosition")

    "give error message if there is no value given" in {
      mandatoryCurrency.bind(Map("" -> "")) shouldBe Left(List(FormError("", "blankValue")))
    }

    "give error message if more than 10 characters" in {
      mandatoryCurrency.bind(Map("" -> "12345678901")) shouldBe Left(List(FormError("", "length")))
    }

    "give error message if invalid characters - funny characters" in {
      mandatoryCurrency.bind(Map("" -> "$%^")) shouldBe Left(List(FormError("", "invalidChars")))
    }

    "give error message if invalid characters - letters" in {
      mandatoryCurrency.bind(Map("" -> "rcol")) shouldBe Left(List(FormError("", "invalidChars")))
    }

    "give error message if incorrect number of digits after point" in {
      mandatoryCurrency.bind(Map("" -> "444.444")) shouldBe Left(List(FormError("", "incorrectPence")))
      mandatoryCurrency.bind(Map("" -> "200.0,0")) shouldBe Left(List(FormError("", "incorrectPence")))
    }

    "give error message if too many decimal points" in {
     mandatoryCurrency.bind(Map("" -> "4.444.444")) shouldBe Left(List(FormError("", "incorrectPence")))
    }

    "give error message if there are spaces between the numbers" in {
      mandatoryCurrency.bind(Map("" -> "11 2 3")) shouldBe Left(List(FormError("", "hasSpaces")))
    }

    "not give the error when value field has valid length" in {
      mandatoryCurrency.bind(Map("" -> "123456.11")) shouldBe Right(Some(123456.11))
    }

    "not give the error when value field has valid value with one decimal" in {
      mandatoryCurrency.bind(Map("" -> "0.11")) shouldBe Right(Some(0.11))
    }

    "give no error if the value is .99" in {
      mandatoryCurrency.bind(Map("" -> ".99")) shouldBe Right(Some(0.99))
    }

    "give no error if the value is with 2 decimal points" in {
      mandatoryCurrency.bind(Map("" -> "1.99")) shouldBe Right(Some(1.99))
    }

    "give no error if the value has no digits after decimal, also append two additional zeros" in {
      mandatoryCurrency.bind(Map("" -> "100.")) shouldBe Right(Some(100.00))
    }

    "give no error if the value is valid" in {
      mandatoryCurrency.bind(Map("" -> "2000")) shouldBe Right(Some(2000))
    }

    "give no error if the value has comma" in {
      mandatoryCurrency.bind(Map("" -> "2,000.00")) shouldBe Right(Some(2000))
    }

    "give error if the value has comma at wrong position" in {
      mandatoryCurrency.bind(Map("" -> "220,00.00")) shouldBe Left(List(FormError("", "hasCommaAtInvalidPosition")))
      mandatoryCurrency.bind(Map("" -> "2,00.00")) shouldBe Left(List(FormError("", "hasCommaAtInvalidPosition")))
    }

  }
}
