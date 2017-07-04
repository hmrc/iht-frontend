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

import iht.FakeIhtApp
import iht.connector.CachingConnector
import iht.models.RegistrationDetails
import iht.testhelpers.{CommonBuilder, NinoBuilder}
import iht.utils.IhtFormValidator._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.data.{FieldMapping, Form, FormError}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IhtFormValidatorTest extends UnitSpec with MockitoSugar with FakeIhtApp {
  val coExecutorIDKey = "id"
  def ninoForCoExecutorMapping(rd:RegistrationDetails): FieldMapping[String] = {
    val mockCachingConnector = mock[CachingConnector]
    val ihtFormValidator = new IhtFormValidator {
      override def cachingConnector = mockCachingConnector
    }

    when(mockCachingConnector.getRegistrationDetails(any(), any())) thenReturn Future.successful(Some(rd))

    implicit val request = createFakeRequest()
    implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("1")))
    ihtFormValidator.ninoForCoExecutor("", "", "", coExecutorIDKey)
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

  "nino for coexecutor mapping" should {
    "respond with no error when nino not same as main executor nino or another executor nino" in {
      val nino = NinoBuilder.randomNino.toString()
      ninoForCoExecutorMapping(CommonBuilder.buildRegistrationDetails1).bind(Map("" -> nino)) shouldBe Right(nino)
    }

    "respond with no error when nino same as THIS executor nino but coexecutor ids are the same" in {
      val nino1 = NinoBuilder.randomNino.toString()
      val nino2 = NinoBuilder.randomNino.toString()
      val nino3 = NinoBuilder.randomNino.toString()

      val coExec1 = CommonBuilder.DefaultCoExecutor1 copy (nino = nino1, ukAddress = None, role = None, isAddressInUk = None)
      val coExec2 = CommonBuilder.DefaultCoExecutor2 copy (nino = NinoBuilder.addSpacesToNino(nino2), ukAddress = None, role = None, isAddressInUk = None)
      val coExec3 = CommonBuilder.DefaultCoExecutor3 copy (nino = nino3, ukAddress = None, role = None, isAddressInUk = None)

      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(CommonBuilder.DefaultNino))
      val rd = CommonBuilder.buildRegistrationDetails1 copy (
        applicantDetails = Some(ad),
        coExecutors = Seq(coExec1, coExec2, coExec3)
      )

      ninoForCoExecutorMapping(rd).bind(Map("" -> nino2, "id" -> "2")) shouldBe Right(nino2)
    }

    "respond with error when nino same as OTHER executor nino but coexecutor ids are the same" in {
      val nino1 = NinoBuilder.randomNino.toString()
      val nino2 = NinoBuilder.randomNino.toString()
      val nino3 = NinoBuilder.randomNino.toString()

      val coExec1 = CommonBuilder.DefaultCoExecutor1 copy (nino = nino1, ukAddress = None, role = None, isAddressInUk = None)
      val coExec2 = CommonBuilder.DefaultCoExecutor2 copy (nino = NinoBuilder.addSpacesToNino(nino2), ukAddress = None, role = None, isAddressInUk = None)
      val coExec3 = CommonBuilder.DefaultCoExecutor3 copy (nino = nino3, ukAddress = None, role = None, isAddressInUk = None)

      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(CommonBuilder.DefaultNino))
      val rd = CommonBuilder.buildRegistrationDetails1 copy (
        applicantDetails = Some(ad),
        coExecutors = Seq(coExec1, coExec2, coExec3)
      )

      ninoForCoExecutorMapping(rd)
        .bind(Map("" -> nino1, "id" -> "2")) shouldBe Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }

    "respond with error when nino same as another executor nino but coexecutor ids are different" in {
      val nino1 = NinoBuilder.randomNino.toString()
      val nino2 = NinoBuilder.randomNino.toString()
      val nino3 = NinoBuilder.randomNino.toString()

      val coExec1 = CommonBuilder.DefaultCoExecutor1 copy (nino = nino1, ukAddress = None, role = None, isAddressInUk = None)
      val coExec2 = CommonBuilder.DefaultCoExecutor2 copy (nino = NinoBuilder.addSpacesToNino(nino2), ukAddress = None, role = None, isAddressInUk = None)
      val coExec3 = CommonBuilder.DefaultCoExecutor3 copy (nino = nino3, ukAddress = None, role = None, isAddressInUk = None)

      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(CommonBuilder.DefaultNino))
      val rd = CommonBuilder.buildRegistrationDetails1 copy (
        applicantDetails = Some(ad),
        coExecutors = Seq(coExec1, coExec2, coExec3)
      )

      ninoForCoExecutorMapping(rd)
        .bind(Map("" -> CommonBuilder.DefaultNino, "id" -> "1")) shouldBe Left(Seq(FormError("", "error.nino.alreadyGiven")))

    }

    "respond with error when nino same as main executor nino" in {
      ninoForCoExecutorMapping(CommonBuilder.buildRegistrationDetails1)
        .bind(Map("" -> CommonBuilder.DefaultNino)) shouldBe Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }

    "respond with error when nino same as deceased nino" in {
      val deceasedNino = NinoBuilder.randomNino.toString()
      val rd = CommonBuilder.buildRegistrationDetails1 copy (
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails copy (nino = Some(deceasedNino)))
        )
      ninoForCoExecutorMapping(rd)
        .bind(Map("" -> deceasedNino)) shouldBe Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }

    "respond with error when nino same as other executor nino but with extra space" in {
      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(NinoBuilder.randomNino.toString()))
      val rd = CommonBuilder.buildRegistrationDetails1 copy (
        applicantDetails = Some(ad)
        )

      ninoForCoExecutorMapping(rd)
        .bind(Map("" -> NinoBuilder.addSpacesToNino(CommonBuilder.DefaultNino))) shouldBe
          Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }

    "respond with error when nino same as another executor nino but other one has an extra space" in {
      val nino1 = NinoBuilder.randomNino.toString()
      val nino2 = NinoBuilder.randomNino.toString()
      val nino3 = NinoBuilder.randomNino.toString()

      val coExec1 = CommonBuilder.DefaultCoExecutor1 copy (nino = nino1, ukAddress = None, role = None, isAddressInUk = None)
      val coExec2 = CommonBuilder.DefaultCoExecutor2 copy (nino = NinoBuilder.addSpacesToNino(nino2), ukAddress = None, role = None, isAddressInUk = None)
      val coExec3 = CommonBuilder.DefaultCoExecutor3 copy (nino = nino3, ukAddress = None, role = None, isAddressInUk = None)

      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(CommonBuilder.DefaultNino))
      val rd = CommonBuilder.buildRegistrationDetails1 copy (
        applicantDetails = Some(ad),
        coExecutors = Seq(coExec1, coExec2, coExec3)
        )

      ninoForCoExecutorMapping(rd)
        .bind(Map("" -> nino2)) shouldBe
        Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }

    "respond with error when nino same as another executor nino but other one is in upper case" in {
      val nino1 = NinoBuilder.randomNino.toString()
      val nino2 = NinoBuilder.randomNino.toString().toLowerCase
      val nino3 = NinoBuilder.randomNino.toString()

      val coExec1 = CommonBuilder.DefaultCoExecutor1 copy (nino = nino1, ukAddress = None, role = None, isAddressInUk = None)
      val coExec2 = CommonBuilder.DefaultCoExecutor2 copy (nino = nino2.toUpperCase, ukAddress = None, role = None, isAddressInUk = None)
      val coExec3 = CommonBuilder.DefaultCoExecutor3 copy (nino = nino3, ukAddress = None, role = None, isAddressInUk = None)

      val ad = CommonBuilder.buildApplicantDetails copy (nino = Some(CommonBuilder.DefaultNino))
      val rd = CommonBuilder.buildRegistrationDetails1 copy (
        applicantDetails = Some(ad),
        coExecutors = Seq(coExec1, coExec2, coExec3)
      )

      ninoForCoExecutorMapping(rd)
        .bind(Map("" -> nino2)) shouldBe
        Left(Seq(FormError("", "error.nino.alreadyGiven")))
    }
  }
}
