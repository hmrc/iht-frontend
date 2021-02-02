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

package iht.forms.validators

import iht.FakeIhtApp
import play.api.data.FormError

class OptionalCurrencyTest extends FakeIhtApp {

  "OptionalCurrency" must {
    val optionalCurrency = OptionalCurrency("length", "invalidChars", "incorrectPence", "hasSpaces", "hasCommaAtInvalidPosition")
    "give error message if more than 10 characters" in {
      optionalCurrency.bind(Map("" -> "12345678901")) mustBe Left(List(FormError("", "length")))
    }

    "give error message if invalid characters - funny characters" in {
      optionalCurrency.bind(Map("" -> "$%^")) mustBe Left(List(FormError("", "invalidChars")))
    }

    "give error message if invalid characters - letters" in {
      optionalCurrency.bind(Map("" -> "rcol")) mustBe Left(List(FormError("", "invalidChars")))
    }

    "give error message if incorrect number of digits after point" in {
      optionalCurrency.bind(Map("" -> "444.444")) mustBe Left(List(FormError("", "incorrectPence")))
    }

    "give error message if too many decimal points" in {
     optionalCurrency.bind(Map("" -> "4.444.444")) mustBe Left(List(FormError("", "incorrectPence")))
    }

    "give error message if there are spaces between the numbers" in {
      optionalCurrency.bind(Map("" -> "11 2 3")) mustBe Left(List(FormError("", "hasSpaces")))
    }

    "not give the error when value field has valid length" in {
      optionalCurrency.bind(Map("" -> "123456.11")) mustBe Right(Some(123456.11))
    }

    "not give the error when value field has valid value with one decimal" in {
      optionalCurrency.bind(Map("" -> "0.11")) mustBe Right(Some(0.11))
    }

    "give no error if the value is .99" in {
      optionalCurrency.bind(Map("" -> ".99")) mustBe Right(Some(0.99))
    }

    "give no error if the value has no digits after decimal, also append two additional zeros" in {
      optionalCurrency.bind(Map("" -> "100.")) mustBe Right(Some(100.00))
    }

    "give no error if the value is with 2 decimal points" in {
      optionalCurrency.bind(Map("" -> "1.99")) mustBe Right(Some(1.99))
    }

    "give no error if the value is valid" in {
      optionalCurrency.bind(Map("" -> "2000")) mustBe Right(Some(2000))
    }

    "give no error if the value has comma" in {
      optionalCurrency.bind(Map("" -> "2,000.00")) mustBe Right(Some(2000))
    }

    "give error if the value has comma at wrong position" in {
      optionalCurrency.bind(Map("" -> "220,00.00")) mustBe Left(List(FormError("", "hasCommaAtInvalidPosition")))
      optionalCurrency.bind(Map("" -> "2,00.00")) mustBe Left(List(FormError("", "hasCommaAtInvalidPosition")))
    }

    "give no error if a space character has been entered instead of a number" in {
      optionalCurrency.bind(Map("" -> " ")) mustBe Right(None)
    }

  }
}
