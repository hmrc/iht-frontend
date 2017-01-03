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

import iht.testhelpers.{CommonBuilder, NinoBuilder}
import iht.utils.IhtFormValidator._
import org.scalatest.mock.MockitoSugar
import play.api.data.FormError
import uk.gov.hmrc.play.test.UnitSpec

class IhtFormValidatorTest extends UnitSpec with MockitoSugar with iht.FakeIhtApp {
  "validatePrivatePensions" must {
    val vpp = IhtFormValidator.validatePrivatePensions("value", "shared", "radio")
    "displays error if no radio button value selected but value" in {
      val result = vpp.bind("", Map("value" -> "22", "shared" -> "", "radio" -> ""))
      result shouldBe Left(List(FormError("radio", "error.value.blank")))
    }

    "displays error if no radio button value selected but share value" in {
      val result = vpp.bind("", Map("value" -> "", "shared" -> "22", "radio" -> ""))
      result shouldBe Left(List(FormError("radio", "error.value.blank")))
    }

    "bind to true if true chosen via radio button" in {
      val result = vpp.bind("", Map("" -> "true", "value" -> "22", "shared" -> "22", "radio" -> ""))
      result shouldBe Right(Some(true))
    }

    "unbind" in {
      val result = vpp.unbind("waa", Some(true))
      result shouldBe Map("waa" -> "true")
    }
  }

  "validateInsurancePolicies" must {
    val vpp = IhtFormValidator.validateInsurancePolicies("value", "shared", "annuities", "other")
    "display error if no radio button value selected but value" in {
      val result = vpp.bind("", Map("value" -> "22", "shared" -> ""))
      result shouldBe Left(List(FormError("annuities", "error.value.blank")))
    }

    "display error if no radio button value selected but share value" in {
      val result = vpp.bind("", Map("value" -> "", "shared" -> "22"))
      result shouldBe Left(List(FormError("annuities", "error.value.blank")))
    }

    "bind to true if true chosen via radio button" in {
      val result = vpp.bind("", Map("" -> "true", "value" -> "", "shared" -> ""))
      result shouldBe Right(Some(true))
    }

    "display error if is annuities bought not ticked paid for so else no value chosen" in {
      val result = vpp.bind("", Map("" -> "true", "value" -> "", "shared" -> "", "annuities" -> "false"))
      result shouldBe Left(List(FormError("other", "error.value.blank")))
    }

    "unbind" in {
      val result = vpp.unbind("waa", Some(true))
      result shouldBe Map("waa" -> "true")
    }
  }

  "validateAssetsHeldInTrust" must {
    val vpp = IhtFormValidator.validateAssetsHeldInTrust("value", "morethanone")
    "displays error if no radio button value selected but value" in {
      val result = vpp.bind("", Map("value" -> "22"))
      result shouldBe Left(List(FormError("morethanone", "error.value.blank")))
    }

    "bind to true if true chosen via radio button" in {
      val result = vpp.bind("", Map("" -> "true", "value" -> "22", "morethanone" -> ""))
      result shouldBe Right(Some(true))
    }

    "unbind" in {
      val result = vpp.unbind("waa", Some(true))
      result shouldBe Map("waa" -> "true")
    }
  }

  "validateBasicEstateElementLiabilities" must {
    val vpp = IhtFormValidator.validateBasicEstateElementLiabilities("value")
    "displays error if no value" in {
      val result = vpp.bind("", Map("value" -> ""))
      result shouldBe Left(List(FormError("value", "error.value.blank")))
    }

    "bind to true if true chosen via radio button" in {
      val result = vpp.bind("", Map("" -> "true", "value" -> "22"))
      result shouldBe Right(Some(true))
    }

    "unbind" in {
      val result = vpp.unbind("waa", Some(true))
      result shouldBe Map("waa" -> "true")
    }
  }

  "validateGiftsDetails" should {
    val fv = IhtFormValidator.validateGiftsDetails("value", "ex")
    "display error if exemptions > 14K" in {
      val result = fv.bind("", Map("value" -> "16000", "ex" -> "15000"))
      result shouldBe Left(List(FormError("ex", "error.giftsDetails.exceedsLimit")))
    }

    "display error if value < exemptions" in {
      val result = fv.bind("", Map("value" -> "12000", "ex" -> "15000"))
      result shouldBe Left(List(FormError("ex", "error.giftsDetails.exceedsGivenAway")))
    }
  }

  "nino" should {
    val ninoMapping = nino("blank", "length", "format")

    "accept valid nino" in {
      val validNino = NinoBuilder.defaultNino
      ninoMapping.bind(Map("" -> validNino)) shouldBe Right(validNino)
    }

    "accept valid nino containing spaces" in {
      val ninoWithSpace = NinoBuilder.addSpacesToNino(CommonBuilder.DefaultNino)
      ninoMapping.bind(Map("" -> ninoWithSpace)) shouldBe Right(ninoWithSpace)
    }

    "accept nino with lower case letters" in {
      val ninoInLowerCase = NinoBuilder.defaultNino.toLowerCase
      ninoMapping.bind(Map("" -> ninoInLowerCase)) shouldBe Right(ninoInLowerCase)
    }

    "reject blank nino" in {
      ninoMapping.bind(Map("" -> "")) shouldBe Left(List(FormError("", "blank")))
    }

    "reject nino which is wrong length (too few characters)" in {
      val ninoWithLengthLessThanValid = NinoBuilder.defaultNino.substring(3)
      ninoMapping.bind(Map("" -> ninoWithLengthLessThanValid)) shouldBe Left(List(FormError("", "length")))
    }

    "reject nino which is wrong length (too many characters)" in {
      val ninoWithLengthMoreThanValid = NinoBuilder.defaultNino.concat("XXXX")
      ninoMapping.bind(Map("" -> ninoWithLengthMoreThanValid)) shouldBe Left(List(FormError("", "length")))
    }

    "reject ninos with invalid prefixes" in {
      val nino = NinoBuilder.defaultNino
      val ninoWithoutPrefix = nino.substring(2)
      
      Set("BG", "GB", "KN", "NK", "NT", "TN", "ZZ").foreach { prefix =>
        ninoMapping.bind(Map("" -> s"${prefix}$ninoWithoutPrefix")) shouldBe Left(List(FormError("", "format")))
      }
    }

    "reject nino with illegal suffix" in {
      val defaultNino = NinoBuilder.defaultNino
      val lastChar = defaultNino.charAt(defaultNino.length-1)

      val ninoWithIllegalSuffix = NinoBuilder.defaultNino.replace(lastChar,'F')
      ninoMapping.bind(Map("" -> ninoWithIllegalSuffix)) shouldBe Left(List(FormError("", "format")))
    }

    "reject nino which has more than one trailing character" in {
      val defaultNino = NinoBuilder.defaultNino
      val secondLastChar = defaultNino.charAt(defaultNino.length-2)
      val ninoWithMoreThanOneTrailingCharacter = defaultNino.replace(secondLastChar,'C')

      ninoMapping.bind(Map("" -> ninoWithMoreThanOneTrailingCharacter)) shouldBe Left(List(FormError("", "format")))
    }

    "reject nino containing spaces but too many characters" in {
      val ninoWithSpacesAndInvalidLength = NinoBuilder.addSpacesToNino(CommonBuilder.DefaultNino).concat("XXXX")
      ninoMapping.bind(Map("" -> ninoWithSpacesAndInvalidLength)) shouldBe Left(List(FormError("", "length")))
    }
  }
}
