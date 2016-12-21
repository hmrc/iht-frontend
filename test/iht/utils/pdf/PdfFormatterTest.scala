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

package iht.utils.pdf

import iht.FakeIhtApp
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by david-beer on 21/11/16.
  */
class PdfFormatterTest extends UnitSpec with FakeIhtApp with MockitoSugar {

  "disply value" must {

    "must return date in format of d MMMM yyyy" in {
      val result = PdfFormatter.getDateForDisplay("1975-10-24")

      result shouldBe "24 October 1975"
    }

    "must throw exception on invalid date" in {
      a[IllegalArgumentException] shouldBe thrownBy {
        PdfFormatter.getDateForDisplay("20 1019")
      }
    }

    "must return the year from specified date" in {
      val result = PdfFormatter.getYearFromDate("1990-06-05")
      result shouldBe 1990
    }

    "must return Australia fo AU" in {
      val result = PdfFormatter.countryName("AU")
      result shouldBe "Australia"
    }
  }

}
