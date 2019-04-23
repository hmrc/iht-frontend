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

package iht.utils

import iht.FakeIhtApp
import iht.config.AppConfig
import iht.connector.CachingConnector
import iht.models.RegistrationDetails
import iht.testhelpers.{CommonBuilder, NinoBuilder}
import iht.utils.IhtFormValidator._
import org.scalatest.mockito.MockitoSugar
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, FormError}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId

import scala.concurrent.ExecutionContext.Implicits.global

class IhtFormValidatorTest extends FakeIhtApp with MockitoSugar {
  val coExecutorIDKey = "id"
  val validName = "Axxlsk"
  val mapping: FieldMapping[String] = name(10, "blank", "length", "chars")

  implicit val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  def ninoForCoExecutorMapping(rd: RegistrationDetails): FieldMapping[String] = {
    val mockCachingConnector = mock[CachingConnector]
    val ihtFormValidator = new IhtFormValidator {}

    implicit val request = createFakeRequest()
    implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("1")))
    ihtFormValidator.ninoForCoExecutor("", "", "", coExecutorIDKey, Some(rd))
  }

  def ninoForDeceasedMapping(rd: RegistrationDetails): FieldMapping[String] = {
    val mockCachingConnector = mock[CachingConnector]
    val ihtFormValidator = new IhtFormValidator {}

    implicit val request = createFakeRequest()
    implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("1")))
    ihtFormValidator.ninoForDeceased("", "", "", Some(rd))
  }

  "validateBasicEstateElementLiabilities" must {
    val vpp = IhtFormValidator.validateBasicEstateElementLiabilities("value")
    "displays error if no value" in {
      val result = vpp.bind("", Map("value" -> ""))
      result mustBe Left(List(FormError("value", "error.value.blank")))
    }

    "bind to true if true chosen via radio button" in {
      val result = vpp.bind("", Map("" -> "true", "value" -> "22"))
      result mustBe Right(Some(true))
    }

    "unbind" in {
      val result = vpp.unbind("waa", Some(true))
      result mustBe Map("waa" -> "true")
    }
  }

  "validateGiftsDetails" should {
    val fv = IhtFormValidator.validateGiftsDetails("value", "ex")

    "display error if value < exemptions" in {
      val result = fv.bind("", Map("value" -> "12000", "ex" -> "15000"))
      result mustBe Left(List(FormError("ex", "error.giftsDetails.exceedsGivenAway")))
    }
  }

  "nino" should {
    val ninoMapping = nino("blank", "length", "format")

    "accept valid nino" in {
      val validNino = NinoBuilder.defaultNino
      ninoMapping.bind(Map("" -> validNino)) mustBe Right(validNino)
    }

    "accept valid nino containing spaces" in {
      val ninoWithSpace = NinoBuilder.addSpacesToNino(CommonBuilder.DefaultNino)
      ninoMapping.bind(Map("" -> ninoWithSpace)) mustBe Right(ninoWithSpace)
    }

    "accept nino with lower case letters" in {
      val ninoInLowerCase = NinoBuilder.defaultNino.toLowerCase
      ninoMapping.bind(Map("" -> ninoInLowerCase)) mustBe Right(ninoInLowerCase)
    }

    "reject blank nino" in {
      ninoMapping.bind(Map("" -> "")) mustBe Left(List(FormError("", "blank")))
    }

    "reject nino which is wrong length (too few characters)" in {
      val ninoWithLengthLessThanValid = NinoBuilder.defaultNino.substring(3)
      ninoMapping.bind(Map("" -> ninoWithLengthLessThanValid)) mustBe Left(List(FormError("", "length")))
    }

    "reject nino which is wrong length (too many characters)" in {
      val ninoWithLengthMoreThanValid = NinoBuilder.defaultNino.concat("XXXX")
      ninoMapping.bind(Map("" -> ninoWithLengthMoreThanValid)) mustBe Left(List(FormError("", "length")))
    }

    "reject ninos with invalid prefixes" in {
      val nino = NinoBuilder.defaultNino
      val ninoWithoutPrefix = nino.substring(2)

      Set("BG", "GB", "KN", "NK", "NT", "TN", "ZZ").foreach { prefix =>
        ninoMapping.bind(Map("" -> s"${prefix}$ninoWithoutPrefix")) mustBe Left(List(FormError("", "format")))
      }
    }

    "reject nino with illegal suffix" in {
      val defaultNino = NinoBuilder.defaultNino
      val lastChar = defaultNino.charAt(defaultNino.length - 1)

      val ninoWithIllegalSuffix = NinoBuilder.defaultNino.replace(lastChar, 'F')
      ninoMapping.bind(Map("" -> ninoWithIllegalSuffix)) mustBe Left(List(FormError("", "format")))
    }

    "reject nino which has more than one trailing character" in {
      val defaultNino = NinoBuilder.defaultNino
      val secondLastChar = defaultNino.charAt(defaultNino.length - 2)
      val ninoWithMoreThanOneTrailingCharacter = defaultNino.replace(secondLastChar, 'C')

      ninoMapping.bind(Map("" -> ninoWithMoreThanOneTrailingCharacter)) mustBe Left(List(FormError("", "format")))
    }

    "reject nino containing spaces but too many characters" in {
      val ninoWithSpacesAndInvalidLength = NinoBuilder.addSpacesToNino(CommonBuilder.DefaultNino).concat("XXXX")
      ninoMapping.bind(Map("" -> ninoWithSpacesAndInvalidLength)) mustBe Left(List(FormError("", "length")))
    }
  }

  "nino for coexecutor mapping" should {
    "respond with no error when nino not same as main executor nino or another executor nino" in {
      val nino = NinoBuilder.randomNino.toString()
      ninoForCoExecutorMapping(CommonBuilder.buildRegistrationDetails1).bind(Map("" -> nino)) mustBe Right(nino)
    }

    "respond with no error when nino same as THIS executor nino but coexecutor ids are the same" in {
      val nino1 = NinoBuilder.randomNino.toString()
      val nino2 = NinoBuilder.randomNino.toString()
      val nino3 = NinoBuilder.randomNino.toString()

      val coExec1 = CommonBuilder.DefaultCoExecutor1 copy(nino = nino1, ukAddress = None, role = None, isAddressInUk = None)
      val coExec2 = CommonBuilder.DefaultCoExecutor2 copy(nino = NinoBuilder.addSpacesToNino(nino2), ukAddress = None, role = None, isAddressInUk = None)
      val coExec3 = CommonBuilder.DefaultCoExecutor3 copy(nino = nino3, ukAddress = None, role = None, isAddressInUk = None)

      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(CommonBuilder.DefaultNino))
      val rd = CommonBuilder.buildRegistrationDetails1 copy(
        applicantDetails = Some(ad),
        coExecutors = Seq(coExec1, coExec2, coExec3)
      )

      ninoForCoExecutorMapping(rd).bind(Map("" -> nino2, "id" -> "2")) mustBe Right(nino2)
    }

    "respond with error when nino same as OTHER executor nino but coexecutor ids are the same" in {
      val nino1 = NinoBuilder.randomNino.toString()
      val nino2 = NinoBuilder.randomNino.toString()
      val nino3 = NinoBuilder.randomNino.toString()

      val coExec1 = CommonBuilder.DefaultCoExecutor1 copy(nino = nino1, ukAddress = None, role = None, isAddressInUk = None)
      val coExec2 = CommonBuilder.DefaultCoExecutor2 copy(nino = NinoBuilder.addSpacesToNino(nino2), ukAddress = None, role = None, isAddressInUk = None)
      val coExec3 = CommonBuilder.DefaultCoExecutor3 copy(nino = nino3, ukAddress = None, role = None, isAddressInUk = None)

      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(CommonBuilder.DefaultNino))
      val rd = CommonBuilder.buildRegistrationDetails1 copy(
        applicantDetails = Some(ad),
        coExecutors = Seq(coExec1, coExec2, coExec3)
      )

      ninoForCoExecutorMapping(rd)
        .bind(Map("" -> nino1, "id" -> "2")) mustBe Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }

    "respond with error when nino same as another executor nino but coexecutor ids are different" in {
      val nino1 = NinoBuilder.randomNino.toString()
      val nino2 = NinoBuilder.randomNino.toString()
      val nino3 = NinoBuilder.randomNino.toString()

      val coExec1 = CommonBuilder.DefaultCoExecutor1 copy(nino = nino1, ukAddress = None, role = None, isAddressInUk = None)
      val coExec2 = CommonBuilder.DefaultCoExecutor2 copy(nino = NinoBuilder.addSpacesToNino(nino2), ukAddress = None, role = None, isAddressInUk = None)
      val coExec3 = CommonBuilder.DefaultCoExecutor3 copy(nino = nino3, ukAddress = None, role = None, isAddressInUk = None)

      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(CommonBuilder.DefaultNino))
      val rd = CommonBuilder.buildRegistrationDetails1 copy(
        applicantDetails = Some(ad),
        coExecutors = Seq(coExec1, coExec2, coExec3)
      )

      ninoForCoExecutorMapping(rd)
        .bind(Map("" -> CommonBuilder.DefaultNino, "id" -> "1")) mustBe Left(Seq(FormError("", "error.nino.alreadyGiven")))

    }

    "respond with error when nino same as main executor nino" in {
      ninoForCoExecutorMapping(CommonBuilder.buildRegistrationDetails1)
        .bind(Map("" -> CommonBuilder.DefaultNino)) mustBe Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }

    "respond with error when nino same as deceased nino" in {
      val deceasedNino = NinoBuilder.randomNino.toString()
      val rd = CommonBuilder.buildRegistrationDetails1 copy (
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails copy (nino = Some(deceasedNino)))
        )
      ninoForCoExecutorMapping(rd)
        .bind(Map("" -> deceasedNino)) mustBe Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }

    "respond with error when nino same as other executor nino but with extra space" in {
      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(NinoBuilder.randomNino.toString()))
      val rd = CommonBuilder.buildRegistrationDetails1 copy (
        applicantDetails = Some(ad)
        )

      ninoForCoExecutorMapping(rd)
        .bind(Map("" -> NinoBuilder.addSpacesToNino(CommonBuilder.DefaultNino))) mustBe
        Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }

    "respond with error when nino same as another executor nino but other one has an extra space" in {
      val nino1 = NinoBuilder.randomNino.toString()
      val nino2 = NinoBuilder.randomNino.toString()
      val nino3 = NinoBuilder.randomNino.toString()

      val coExec1 = CommonBuilder.DefaultCoExecutor1 copy(nino = nino1, ukAddress = None, role = None, isAddressInUk = None)
      val coExec2 = CommonBuilder.DefaultCoExecutor2 copy(nino = NinoBuilder.addSpacesToNino(nino2), ukAddress = None, role = None, isAddressInUk = None)
      val coExec3 = CommonBuilder.DefaultCoExecutor3 copy(nino = nino3, ukAddress = None, role = None, isAddressInUk = None)

      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(CommonBuilder.DefaultNino))
      val rd = CommonBuilder.buildRegistrationDetails1 copy(
        applicantDetails = Some(ad),
        coExecutors = Seq(coExec1, coExec2, coExec3)
      )

      ninoForCoExecutorMapping(rd)
        .bind(Map("" -> nino2)) mustBe
        Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }

    "respond with error when nino same as another executor nino but other one is in upper case" in {
      val nino1 = NinoBuilder.randomNino.toString()
      val nino2 = NinoBuilder.randomNino.toString().toLowerCase
      val nino3 = NinoBuilder.randomNino.toString()

      val coExec1 = CommonBuilder.DefaultCoExecutor1 copy(nino = nino1, ukAddress = None, role = None, isAddressInUk = None)
      val coExec2 = CommonBuilder.DefaultCoExecutor2 copy(nino = nino2.toUpperCase, ukAddress = None, role = None, isAddressInUk = None)
      val coExec3 = CommonBuilder.DefaultCoExecutor3 copy(nino = nino3, ukAddress = None, role = None, isAddressInUk = None)

      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(CommonBuilder.DefaultNino))
      val rd = CommonBuilder.buildRegistrationDetails1 copy(
        applicantDetails = Some(ad),
        coExecutors = Seq(coExec1, coExec2, coExec3)
      )

      ninoForCoExecutorMapping(rd)
        .bind(Map("" -> nino2)) mustBe
        Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }
  }

  // scalastyle:off magic.number
  "check for name error" should {
    "respond with error when no value entered" in {
      checkForNameError("", 10, "blank", "length", "chars", Some("")) mustBe Some(FormError("", "blank"))
    }

    "respond with error when too long" in {
      checkForNameError("", 10, "blank", "length", "chars", Some("a" * 11)) mustBe Some(FormError("", "length"))
    }
  }

  "validatePartnerName" should {
    "return errors for blank values" in {
      val formatter: Formatter[Option[String]] = validatePartnerName("lastName")
      val result = formatter.bind("firstName", Map("firstName" -> "", "lastName" -> ""))
      result mustBe Left(Seq(FormError("firstName", "error.firstName.give"),
        FormError("lastName", "error.lastName.give")))
    }

    "return errors for length" in {
      val formatter: Formatter[Option[String]] = validatePartnerName("lastName")
      val result = formatter.bind("firstName", Map("firstName" -> "a" * 50, "lastName" -> "a" * 50))
      result mustBe Left(Seq(FormError("firstName", "error.firstName.giveUsingXCharsOrLess"),
        FormError("lastName", "error.lastName.giveUsingXCharsOrLess")))
    }

    "return no errors for valid entry" in {
      val formatter: Formatter[Option[String]] = validatePartnerName("lastName")
      val result = formatter.bind("firstName", Map("firstName" -> validName, "lastName" -> validName))
      result mustBe Right(Some(validName))
    }
  }

  "name" should {
    "return error for blank value" in {
      mapping.bind(Map("" -> "")) mustBe Left(List(FormError("", "blank")))
    }

    "return error for long value" in {
      mapping.bind(Map("" -> "a" * 50)) mustBe Left(List(FormError("", "length")))
    }

    "return no error for valid value" in {
      mapping.bind(Map("" -> validName)) mustBe Right(validName)
    }
  }
  // scalastyle:on magic.number

  "nino for deceased" should {

    "respond with no error when nino not same as main executor nino or an executor nino" in {
      val nino = NinoBuilder.randomNino.toString()
      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(CommonBuilder.DefaultNino))

      ninoForDeceasedMapping(CommonBuilder.buildRegistrationDetails1 copy (
        applicantDetails = Some(ad))).bind(Map("" -> nino)) mustBe Right(nino)
    }

    "respond with error when nino entered for nino matches previously entered nino for applicant" in {
      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(CommonBuilder.DefaultNino))
      val rd = CommonBuilder.buildRegistrationDetails1 copy (applicantDetails = Some(ad))

      ninoForDeceasedMapping(rd)
        .bind(Map("" -> NinoBuilder.addSpacesToNino(CommonBuilder.DefaultNino))) mustBe
        Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }

    "respond with error when nino entered for deceased matches previously entered nino for a coexecutor" in {
      val coExec1 = CommonBuilder.DefaultCoExecutor1 copy(nino = Some(CommonBuilder.DefaultNino),
        ukAddress = None, role = None, isAddressInUk = None)
      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(NinoBuilder.randomNino.toString()))
      val rd = CommonBuilder.buildRegistrationDetails1 copy(applicantDetails = Some(ad), coExecutors = Seq(coExec1))

      ninoForDeceasedMapping(rd)
        .bind(Map("" -> CommonBuilder.DefaultNino)) mustBe Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }

    "respond with error when nino entered for deceased matches previously entered nino for a 2nd coexecutor" in {
      val coExec1 = CommonBuilder.DefaultCoExecutor1 copy(nino = Some(NinoBuilder.randomNino.toString()),
        ukAddress = None, role = None, isAddressInUk = None)
      val coExec2 = CommonBuilder.DefaultCoExecutor1 copy(nino = Some(CommonBuilder.DefaultNino),
        ukAddress = None, role = None, isAddressInUk = None)
      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(NinoBuilder.randomNino.toString()))
      val rd = CommonBuilder.buildRegistrationDetails1 copy(applicantDetails = Some(ad), coExecutors = Seq(coExec1, coExec2))

      ninoForDeceasedMapping(rd)
        .bind(Map("" -> CommonBuilder.DefaultNino)) mustBe Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }

  }
}
