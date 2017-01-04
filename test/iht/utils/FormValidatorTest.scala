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

package iht.utils

import iht.constants.FieldMappings
import iht.testhelpers.{NinoBuilder, CommonBuilder, TestHelper}
import iht.utils.IhtFormValidator._
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import play.api.data.{FieldMapping, FormError}
import uk.gov.hmrc.play.test.UnitSpec

import scala.collection.immutable.ListMap

/**
 * Created by xavierzanatta on 3/20/15.
 */
class FormValidatorTest extends UnitSpec with MockitoSugar with iht.FakeIhtApp {

  val nino = CommonBuilder.DefaultNino

  "isNotFutureDate" must {
    "return false the date is later than current date" in {
      val testDate = LocalDate.now().plusDays(1)
      val result = IhtFormValidator.isNotFutureDate(testDate)
      result should be(false)
    }

    "return true the date is equal to current date" in {
      val testDate = LocalDate.now()
      val result = IhtFormValidator.isNotFutureDate(testDate)
      result should be(true)
    }

    "return true the date is earlier than current date" in {
      val testDate = LocalDate.now().minusDays(1)
      val result = IhtFormValidator.isNotFutureDate(testDate)
      result should be(true)
    }
  }

  "validateCountryCode" must {
    "reject an invalid country code" in {
      IhtFormValidator.validateCountryCode("UK") should be(false)
    }
    "accept a valid country code" in {
      IhtFormValidator.validateCountryCode("GB") should be(true)
    }
  }

  "existsInKeys" must {
    "return true if valid list map key" in {
      val result = IhtFormValidator.existsInKeys(TestHelper.MaritalStatusSingle, FieldMappings.maritalStatusMap)
      result should be(true)
    }

    "return false if invalid list map key" in {
      val result = IhtFormValidator.existsInKeys("7", FieldMappings.maritalStatusMap)
      result should be(false)
    }
  }

  "currency" should {
    "Report correctly for invalid numeric value length>10" in {
      optionalCurrencyWithoutFieldName.bind(Map("" -> "11111111111111111111")) shouldBe Left(List(FormError("", "error.currencyValue.length")))
    }

    "Report correctly for invalid numeric value length zero" in {
      mandatoryCurrencyWithParameter("").bind(Map("" -> "")) shouldBe Left(List(FormError("", "error.blank.")))
    }

    "Report correctly for invalid numeric value invalid number format - length" in {
      mandatoryCurrencyWithParameter("").bind(Map("" -> "123&^4411111111111111")) shouldBe Left(List(FormError("", "error.length.")))
    }

    "Report correctly for invalid numeric value invalid number format" in {
      mandatoryCurrencyWithParameter("").bind(Map("" -> "123&^4")) shouldBe Left(List(FormError("", "error.currency.")))
    }
 }

  "mandatoryPhoneNumberFormatter" should {
    "Return expected mapping validation for various inputs, valid and invalid" in {
      import play.api.data.FormError

      val formatter = mandatoryPhoneNumberFormatter("blank message", "invalid length", "invalid value")

      formatter.bind("a", Map("a" -> ""))  shouldBe Left(Seq(FormError("a", "blank message")))
      formatter.bind("a", Map("a" -> "1111111111111111111111111111"))  shouldBe Left(Seq(FormError("a", "invalid length")))
      formatter.bind("a", Map("a" -> "$5gggF"))  shouldBe Left(Seq(FormError("a", "invalid value")))
      formatter.bind("a", Map("a" -> "+44 020 1234 5678"))  shouldBe Right("0044 020 1234 5678")
      formatter.bind("a", Map("a" -> "(020) 1234 5678")) shouldBe Right("(020) 1234 5678")

      formatter.bind("a", Map("a" -> "(020) 1234 5678#1234")) shouldBe Right("(020) 1234 5678#1234")
      formatter.bind("a", Map("a" -> "(020) 1234 5678*6")) shouldBe Right("(020) 1234 5678*6")
      formatter.bind("a", Map("a" -> "(020) 1234 5678")) shouldBe Right("(020) 1234 5678")
      formatter.bind("a", Map("a" -> "02012345678")) shouldBe Right("02012345678")
      formatter.bind("a", Map("a" -> "02012345678 ext 1234")) shouldBe Right("02012345678 EXT 1234")
      formatter.bind("a", Map("a" -> "020123456+12 ext 1234")) shouldBe Left(Seq(FormError("a", "invalid value")))

    }
  }

  "phoneNumberOptionString" should {
    "Return expected mapping validation for various inputs, valid and invalid" in {
      import play.api.data.FormError
      val mapping: FieldMapping[Option[String]] = phoneNumberOptionString("blank message", "invalid length", "invalid value")

      mapping.bind(Map("" -> ""))  shouldBe Left(Seq(FormError("", "blank message")))
      mapping.bind(Map("" -> "1111111111111111111111111111"))  shouldBe Left(Seq(FormError("", "invalid length")))
      mapping.bind(Map("" -> "$5gggF"))  shouldBe Left(Seq(FormError("", "invalid value")))
      mapping.bind(Map("" -> "+44 020 1234 5678"))  shouldBe Right(Some("0044 020 1234 5678"))
      mapping.bind(Map("" -> "(020) 1234 5678")) shouldBe Right(Some("(020) 1234 5678"))

      mapping.bind(Map("" -> "(020) 1234 5678#1234")) shouldBe Right(Some("(020) 1234 5678#1234"))
      mapping.bind(Map("" -> "(020) 1234 5678*6")) shouldBe Right(Some("(020) 1234 5678*6"))
      mapping.bind(Map("" -> "(020) 1234 5678")) shouldBe Right(Some("(020) 1234 5678"))
      mapping.bind(Map("" -> "02012345678")) shouldBe Right(Some("02012345678"))
      mapping.bind(Map("" -> "02012345678 ext 1234")) shouldBe Right(Some("02012345678 EXT 1234"))
      mapping.bind(Map("" -> "020123456+12 ext 1234")) shouldBe Left(Seq(FormError("", "invalid value")))
    }
  }

  "ihtAddress" should {
    val allBlank = Map(
      "addr1key"->"",
      "addr2key"->"",
      "addr3key"->"",
      "addr4key"->"",
      "postcodekey"->"",
      "countrycodekey"->""
    )

    val first2Blank = Map(
      "addr1key"->"",
      "addr2key"->"",
      "postcodekey"->CommonBuilder.DefaultPostCode,
      "countrycodekey"->"GB"
    )

    val invalidLine2 = Map(
      "addr1key"->"addr1",
      "addr2key"->"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
      "addr3key"->"addr3",
      "addr4key"->"addr4",
      "postcodekey"->"pcode",
      "countrycodekey"->"GB"
    )

    val blankPostcode = Map(
      "addr1key"->"addr1",
      "addr2key"->"addr2",
      "addr3key"->"addr3",
      "addr4key"->"addr4",
      "postcodekey"->"",
      "countrycodekey"->"GB"
    )

    val invalidPostcode = Map(
      "addr1key"->"addr1",
      "addr2key"->"addr2",
      "addr3key"->"addr3",
      "addr4key"->"addr4",
      "postcodekey"->"CC!",
      "countrycodekey"->"GB"
    )

    val allowedBlankPostcode = Map(
      "addr1key"->"addr1",
      "addr2key"->"addr2",
      "addr3key"->"addr3",
      "addr4key"->"addr4",
      "postcodekey"->"",
      "countrycodekey"->"IL"
    )

    val formatter = ihtAddress("addr2key","addr3key","addr4key","postcodekey", "countrycodekey",
      "all-lines-blank","first-two-blank","invalid-line","blank-postcode","invalid-postcode", "blankcountrycode")

    "Return a formatter which responds suitably to all lines being blank" in {
      formatter.bind("", allBlank).left.get.contains(FormError("", "all-lines-blank")) shouldBe true
    }

    "Return a formatter which responds suitably to first two lines being blank" in {
      formatter.bind("", first2Blank).left.get.contains(FormError("", "all-lines-blank")) shouldBe true
    }
    "Return a formatter which responds suitably to invalid lines" in {
      formatter.bind("", invalidLine2).left.get.contains(FormError("addr2key", "invalid-line")) shouldBe true
    }
    "Return a formatter which responds suitably to blank postcode" in {
      formatter.bind("", blankPostcode).left.get.contains(FormError("postcodekey", "blank-postcode")) shouldBe true
    }
    "Return a formatter which responds suitably to invalid postcode" in {
      formatter.bind("", invalidPostcode).left.get.contains(FormError("postcodekey", "invalid-postcode")) shouldBe true
    }
    "Return a formatter which responds suitably to allowed country code" in {
      formatter.bind("", allowedBlankPostcode).left.get.contains(FormError("postcodekey", "blank-postcode")) shouldBe false
    }
  }

  "ihtRadio" should {
    val formatter = ihtRadio("no-selection", ListMap("a"->"a"))
    "Return a formatter which responds suitably to no item being selected" in {
      formatter.bind("radiokey", Map( "option1"->"option1" ))
        .left.get.contains(FormError("radiokey", "no-selection")) shouldBe true
    }
  }

  "radioOptionString" should {
    val formatter = radioOptionString("no-selection", ListMap("a"->"a"))
    "Return a formatter which responds suitably to no item being selected" in {
      formatter.bind("radiokey", Map( "option1"->"option1" ))
        .left.get.contains(FormError("radiokey", "no-selection")) shouldBe true
    }
  }

  "validateDate" should {
    "respond appropriately to invalid month and day" in {
      val fv = new FormValidator{}

      val result = fv.validateDate("2000", "17", "17", None, Some("blank"), Some("mdm"), Some("invalid"), Some("fd"))
      result shouldBe Some("mdm")
    }
  }

  "validateDateInvalidCharAndFutureDate" should {
    "respond appropriate to invalid char" in {
      val fv = new FormValidator{}

      val result = fv.validateDateInvalidCharAndFutureDate("@$", "12", "11", Some("ic"), Some("fd"))
      result shouldBe Some("ic")
    }
  }

  "isDayAndMonthLessThanMax" should {
    "respond appropriate when can't get int values from components" in {
      val fv = new FormValidator{}
      val result = fv.isDayAndMonthLessThanMax("$$", "@@")
      result shouldBe true
    }
  }
}
