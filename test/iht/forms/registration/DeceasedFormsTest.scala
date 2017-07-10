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

package iht.forms.registration

import iht.FakeIhtApp
import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.IhtProperties._
import iht.forms.FormTestHelper
import iht.forms.registration.DeceasedForms._
import iht.models.{DeceasedDateOfDeath, DeceasedDetails, UkAddress}
import iht.testhelpers.{CommonBuilder, NinoBuilder}
import iht.utils.IhtFormValidator
import org.joda.time.LocalDate
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, FormError, Forms}
import play.api.i18n.Messages
import play.api.mvc.Request
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.SessionId

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class DeceasedFormsTest extends FormTestHelper with FakeIhtApp {
  def dateOfDeath(day: String, month: String, year: String) =
    Map("dateOfDeath.day" -> day, "dateOfDeath.month" -> month, "dateOfDeath.year" -> year)

  lazy val completeDateOfDeath = dateOfDeath("01", "01", "2015")

  def permanentHome(value: String) = Map("domicile" -> value)

  def deceasedAddressQuestion(value: String) = Map("isAddressInUk" -> value)

  def aboutDeceased(firstName: String, lastName: String, nino: String, day: String,
                    month: String, year: String, maritalStatus: String) =
    Map("firstName" -> firstName,
      "lastName" -> lastName,
      "nino" -> nino,
      "dateOfBirth.day" -> day,
      "dateOfBirth.month" -> month,
      "dateOfBirth.year" -> year,
      "maritalStatus" -> maritalStatus)

  val firstName = CommonBuilder.firstNameGenerator
  val surname = CommonBuilder.surnameGenerator

  lazy val completeAboutDeceased: Map[String, String] = aboutDeceased(
    firstName, surname, CommonBuilder.DefaultNino, "01", "01", "1980", statusSingle)

  def deceasedAddress(line1: String, line2: String, line3: String, line4: String, postCode: String, countryCode: String) =
    Map("ukAddress.addressLine1" -> line1,
      "ukAddress.ukAddressLine2" -> line2,
      "ukAddress.addressLine3" -> line3,
      "ukAddress.addressLine4" -> line4,
      "ukAddress.postCode" -> postCode,
      "ukAddress.countryCode" -> countryCode)

  lazy val completeUkAddress = deceasedAddress("Line 1", "Line 2", "Line 3", "Line 4", "AA111AA", "GB") - "ukAddress.countryCode"

  lazy val completeAddressAbroad = deceasedAddress("Line 1", "Line 2", "Line 3", "Line 4", "AA111AA", "AU") - "ukAddress.postCode"

  def ec = mock[ExecutionContext]
  def hc = mock[HeaderCarrier]
  def mockCachingConnector = mock[CachingConnector]
  def mockIhtConnector = mock[IhtConnector]

  //region Deceased Date of Death tests


  "Deceased Date of Death form" must {

    "not give an error for a valid date" in {
      deceasedDateOfDeathForm.bind(completeDateOfDeath).get shouldBe DeceasedDateOfDeath(new LocalDate(2015, 1, 1))
    }

    "give an error when the day is blank" in {
      val data = dateOfDeath("", "01", "2014")
      val expectedErrors = error("dateOfDeath", "error.dateOfDeath.giveFull")

      checkForError(deceasedDateOfDeathForm, data, expectedErrors)
    }

    "give an error when the day is not supplied" in {
      val data = completeDateOfDeath - "dateOfDeath.day"
      val expectedErrors = error("dateOfDeath.day", "error.required")

      checkForError(deceasedDateOfDeathForm, data, expectedErrors)
    }

    "give an error when the day is invalid" in {
      val data = dateOfDeath("INVALID", "01", "2014")
      val expectedErrors = error("dateOfDeath", "error.dateOfDeath.giveCorrectDateUsingOnlyNumbers")

      checkForError(deceasedDateOfDeathForm, data, expectedErrors)
    }

    "give an error when the day is too high for the month" in {
      val data = dateOfDeath("29", "02", "2013")
      val expectedErrors = error("dateOfDeath", "error.dateOfDeath.giveCorrectDayForMonth")

      checkForError(deceasedDateOfDeathForm, data, expectedErrors)
    }

    "give an error when the month is blank" in {
      val data = dateOfDeath("01", "", "2014")
      val expectedErrors = error("dateOfDeath", "error.dateOfDeath.giveFull")

      checkForError(deceasedDateOfDeathForm, data, expectedErrors)
    }

    "give an error when the month is not supplied" in {
      val data = completeDateOfDeath - "dateOfDeath.month"
      val expectedErrors = error("dateOfDeath.month", "error.required")

      checkForError(deceasedDateOfDeathForm, data, expectedErrors)
    }

    "give an error when the month is invalid" in {
      val data = dateOfDeath("01", "INVALID", "2014")
      val expectedErrors = error("dateOfDeath", "error.dateOfDeath.giveCorrectDateUsingOnlyNumbers")

      checkForError(deceasedDateOfDeathForm, data, expectedErrors)
    }

    "give an error when the month is too high" in {
      val data = dateOfDeath("01", "13", "2013")
      val expectedErrors = error("dateOfDeath", "error.dateOfDeath.giveCorrectMonth")

      checkForError(deceasedDateOfDeathForm, data, expectedErrors)
    }

    "give an error when the year is blank" in {
      val data = dateOfDeath("01", "01", "")
      val expectedErrors = error("dateOfDeath", "error.dateOfDeath.giveFull")

      checkForError(deceasedDateOfDeathForm, data, expectedErrors)
    }

    "give an error when the year is not supplied" in {
      val data = completeDateOfDeath - "dateOfDeath.year"
      val expectedErrors = error("dateOfDeath.year", "error.required")

      checkForError(deceasedDateOfDeathForm, data, expectedErrors)
    }

    "give an error when the year is invalid" in {
      val data = dateOfDeath("01", "01", "INVALID")
      val expectedErrors = error("dateOfDeath", "error.dateOfDeath.giveCorrectDateUsingOnlyNumbers")

      checkForError(deceasedDateOfDeathForm, data, expectedErrors)
    }

    "give an error when the year is supplied as a two-digit number" in {
      val data = dateOfDeath("01", "01", "14")
      val expectedErrors = error("dateOfDeath", "error.dateOfDeath.giveCorrectYear")

      checkForError(deceasedDateOfDeathForm, data, expectedErrors)
    }

    "give only one error when two fields are invalid" in {
      val data = dateOfDeath("32", "XX", "14")
      val expectedErrors = error("dateOfDeath", "error.dateOfDeath.giveCorrectDateUsingOnlyNumbers")

      checkForError(deceasedDateOfDeathForm, data, expectedErrors)
    }

    "give an error when no data is supplied" in {
      val expectedErrors = error("dateOfDeath.day", "error.required") ++
        error("dateOfDeath.month", "error.required") ++
        error("dateOfDeath.year", "error.required")

      checkForError(deceasedDateOfDeathForm, emptyForm, expectedErrors)
    }
  }

  //endregion

  //region Deceased Permanent Home tests

  "Deceased Permanent Home form" must {

    "not give an error for a valid answer" in {
      val data = permanentHome(domicileEnglandOrWales)
      deceasedPermanentHomeForm(messages).bind(data).get shouldBe DeceasedDetails(domicile = Some(domicileEnglandOrWales))
    }

    "give an error when a blank value is supplied" in {
      val data = permanentHome("")
      val expectedErrors = error("domicile", "error.invalid")

      checkForError(deceasedPermanentHomeForm(messages), data, expectedErrors)
    }

    "give an error when invalid data is supplied" in {
      val data = permanentHome("INVALID")
      val expectedErrors = error("domicile", "error.invalid")

      checkForError(deceasedPermanentHomeForm(messages), data, expectedErrors)
    }

    "give an error when no data is supplied" in {
      val expectedErrors = error("domicile", "error.deceasedPermanentHome.selectLocation")

      checkForError(deceasedPermanentHomeForm(messages), emptyForm, expectedErrors)
    }
  }

  //endregion

  //region About Deceased tests

  def deceasedForms = {
    val mockIhtFormValidator = new IhtFormValidator {

      override def cachingConnector: CachingConnector = mock[CachingConnector]
      override def ninoForDeceased(blankMessageKey: String, lengthMessageKey: String, formatMessageKey: String)(
        implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): FieldMapping[String] = {
        val formatter = new Formatter[String] {
          override val format: Option[(String, Seq[Any])] = None

          override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = Right("")

          override def unbind(key: String, value: String): Map[String, String] = Map.empty
        }
        val fieldMapping: FieldMapping[String] = Forms.of(formatter)
        fieldMapping
      }
    }

    new DeceasedForms {
      override def ihtFormValidator: IhtFormValidator = mockIhtFormValidator
    }
  }

  def formsWithIhtFormValidatorMockedToFail = {
    val formatter = mock[Formatter[String]]
    when(formatter.bind(any(), any())).thenReturn(Left(Seq(FormError("nino", "error.nino.alreadyGiven"))))
    val fieldMapping: FieldMapping[String] = Forms.of(formatter)
    val mockIhtFormValidator = mock[IhtFormValidator]
    when(mockIhtFormValidator.ninoForDeceased(any(), any(), any())(any(), any(), any()))
      .thenReturn(fieldMapping)
    val deceasedForms = new DeceasedForms {
      override def ihtFormValidator: IhtFormValidator = mockIhtFormValidator
    }
    deceasedForms
  }

  def formsWithIhtFormValidatorMockedToSucceed(nino:String): DeceasedForms = {
    val formatter = mock[Formatter[String]]
    when(formatter.bind(any(), any())).thenReturn(Right(nino))
    val fieldMapping: FieldMapping[String] = Forms.of(formatter)
    val mockIhtFormValidator = mock[IhtFormValidator]
    when(mockIhtFormValidator.ninoForDeceased(any(), any(), any())(any(), any(), any()))
      .thenReturn(fieldMapping)
    val deceasedForms = new DeceasedForms {
      override def ihtFormValidator: IhtFormValidator = mockIhtFormValidator
    }
    deceasedForms
  }

  "About Deceased form" must {

    "not give an error for valid data" in {
      val nino = NinoBuilder.randomNino.toString

      def bindFormEdit(map: Map[String, String]) = {
        implicit val request = createFakeRequest()
        implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("1")))
        implicit val msg: Messages = messages
        formsWithIhtFormValidatorMockedToSucceed(nino).aboutDeceasedForm().bind(map)
      }

      bindFormEdit(completeAboutDeceased).get shouldBe
        DeceasedDetails(firstName = Some(firstName),
          middleName = None,
          lastName = Some(surname),
          nino = Some(nino),
          ukAddress = None,
          dateOfBirth = Some(new LocalDate(1980, 1, 1)),
          maritalStatus = Some(statusSingle),
          domicile = None,
          isAddressInUK = None)
    }

    "give an error when the first name is blank" in {
      val data = completeAboutDeceased + ("firstName" -> "")
      val expectedErrors = error("firstName", "error.firstName.give")

      checkForError(deceasedForms.aboutDeceasedForm()(messages, createFakeRequest(true), hc, ec), data, expectedErrors)
    }

    "give an error when the first name is not supplied" in {
      val data = completeAboutDeceased - "firstName"
      val expectedErrors = error("firstName", "error.required")

      checkForError(deceasedForms.aboutDeceasedForm()(messages, createFakeRequest(true), hc, ec), data, expectedErrors)
    }

    "give an error when the first name is too long" in {
      val data = completeAboutDeceased + ("firstName" -> "A value that's longer than the 40 characters allowed in this field")
      val expectedErrors = error("firstName", "error.firstName.giveUsingXCharsOrLess")

      checkForError(deceasedForms.aboutDeceasedForm()(messages, createFakeRequest(true), hc, ec), data, expectedErrors)
    }

    "give an error when the last name is blank" in {
      val data = completeAboutDeceased + ("lastName" -> "")
      val expectedErrors = error("lastName", "error.lastName.give")

      checkForError(deceasedForms.aboutDeceasedForm()(messages, createFakeRequest(true), hc, ec), data, expectedErrors)
    }

    "give an error when the last name is not supplied" in {
      val data = completeAboutDeceased - "lastName"
      val expectedErrors = error("lastName", "error.required")

      checkForError(deceasedForms.aboutDeceasedForm()(messages, createFakeRequest(true), hc, ec), data, expectedErrors)
    }

    "give an error when the last name is too long" in {
      val data = completeAboutDeceased + ("lastName" -> "A value that's longer than the 40 characters allowed in this field")
      val expectedErrors = error("lastName", "error.lastName.giveUsingXCharsOrLess")

      checkForError(deceasedForms.aboutDeceasedForm()(messages, createFakeRequest(true), hc, ec), data, expectedErrors)
    }

    "give an error when a blank marital status is supplied" in {
      val data = completeAboutDeceased + ("maritalStatus" -> "")
      val expectedErrors = error("maritalStatus", "error.invalid")

      checkForError(deceasedForms.aboutDeceasedForm()(messages, createFakeRequest(true), hc, ec), data, expectedErrors)
    }

    "give an error when an invalid marital status is supplied" in {
      val data = completeAboutDeceased + ("maritalStatus" -> "INVALID")
      val expectedErrors = error("maritalStatus", "error.invalid")

      checkForError(deceasedForms.aboutDeceasedForm()(messages, createFakeRequest(true), hc, ec), data, expectedErrors)
    }

    "give an error when no marital status is supplied" in {
      val data = completeAboutDeceased - "maritalStatus"
      val expectedErrors = error("maritalStatus", "error.deceasedMaritalStatus.select")

      checkForError(deceasedForms.aboutDeceasedForm()(messages, createFakeRequest(true), hc, ec), data, expectedErrors)
    }

    "give multiple errors when several fields are invalid" in {
      val data = completeAboutDeceased + ("firstName" -> "", "lastName" -> "")
      val expectedErrors = error("firstName", "error.firstName.give") ++
        error("lastName", "error.lastName.give")
      val form = deceasedForms.aboutDeceasedForm()(messages, createFakeRequest(true), hc, ec)

      checkForError(form, data, expectedErrors)
    }

    "give one date error when several date fields are invalid" in {
      val data = completeAboutDeceased + ("dateOfBirth.day" -> "32", "dateOfBirth.month" -> "13", "dateOfBirth.year" -> "12", "dateOfBirth.day" -> "99")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveFull")

      checkForError(deceasedForms.aboutDeceasedForm()(messages, createFakeRequest(true), hc, ec), data, expectedErrors)
    }

    "indicate validation error when nino for coexecutor validation fails" in {
      def checkForError(data: Map[String, String], expectedErrors: Seq[FormError]): Unit = {
        implicit val request = createFakeRequest()
        implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("1")))
        implicit val msg = messages
        super.checkForError(formsWithIhtFormValidatorMockedToFail
          .aboutDeceasedForm(), data, expectedErrors)
      }
      val data = completeAboutDeceased
      val expectedErrors = error("nino", "error.nino.alreadyGiven")

      checkForError(data, expectedErrors)
    }
  }

  //endregion

  //region Deceased Address Question tests

  "Deceased Address Question form" must {

    "not give an error when answered Yes" in {
      val data = deceasedAddressQuestion("true")
      deceasedAddressQuestionForm.bind(data).get shouldBe DeceasedDetails(isAddressInUK = Some(true))
    }

    "not give an error when answered No" in {
      val data = deceasedAddressQuestion("false")
      deceasedAddressQuestionForm.bind(data).get shouldBe DeceasedDetails(isAddressInUK = Some(false))
    }

    "give an error when the question is not answered" in {
      val expectedErrors = error("isAddressInUk", "error.address.wasInUK.give")

      checkForError(deceasedAddressQuestionForm, emptyForm, expectedErrors)
    }

    "give an error when an invalid answer is given" in {
      val data = deceasedAddressQuestion("INVALID")
      val expectedErrors = error("isAddressInUk", "error.invalid")

      checkForError(deceasedAddressQuestionForm, data, expectedErrors)
    }
  }

  //endregion

  //region Deceased Address in the UK tests

  "Deceased Address Details (in the UK) form" must {

    "not give an error for valid data" in {
      deceasedAddressDetailsUKForm.bind(completeUkAddress).get shouldBe
        DeceasedDetails(ukAddress = Some(UkAddress("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), "AA111AA", "GB")))
    }

    "not give an error when lines 2 and 3 are omitted" in {
      val data = completeUkAddress - "ukAddress.addressLine3" - "ukAddress.addressLine4"

      deceasedAddressDetailsUKForm.bind(data).get shouldBe
        DeceasedDetails(ukAddress = Some(UkAddress("Line 1", "Line 2", None, None, "AA111AA", "GB")))
    }

    "give an error when line 1 is blank" in {
      val data = completeUkAddress + ("ukAddress.addressLine1" -> "")
      val expectedErrors = error("ukAddress.addressLine1", "error.address.giveInLine1And2")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give an error when line 1 is omitted" in {
      val data = completeUkAddress - "ukAddress.addressLine1"
      val expectedErrors = error("ukAddress.addressLine1", "error.address.giveInLine1And2")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give an error when line 1 is too long" in {
      val data = completeUkAddress + ("ukAddress.addressLine1" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddress.addressLine1", "error.address.giveUsing35CharsOrLess")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give an error when line 2 is blank" in {
      val data = completeUkAddress + ("ukAddress.ukAddressLine2" -> "")
      val expectedErrors = error("ukAddress.ukAddressLine2", "error.address.giveInLine1And2")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give an error when line 2 is omitted" in {
      val data = completeUkAddress - "ukAddress.ukAddressLine2"
      val expectedErrors = error("ukAddress.ukAddressLine2", "error.address.giveInLine1And2") ++
        error("ukAddress.ukAddressLine2", "error.required")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give an error when line 2 is too long" in {
      val data = completeUkAddress + ("ukAddress.ukAddressLine2" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddress.ukAddressLine2", "error.address.giveUsing35CharsOrLess")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give an error when line 3 is too long" in {
      val data = completeUkAddress + ("ukAddress.addressLine3" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddress.addressLine3", "error.address.giveUsing35CharsOrLess")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give an error when line 4 is too long" in {
      val data = completeUkAddress + ("ukAddress.addressLine4" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddress.addressLine4", "error.address.giveUsing35CharsOrLess")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give an error when the postcode is blank" in {
      val data = completeUkAddress + ("ukAddress.postCode" -> "")
      val expectedErrors = error("ukAddress.postCode", "error.address.givePostcode")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give an error when the postcode is omitted" in {
      val data = completeUkAddress - "ukAddress.postCode"
      val expectedErrors = error("ukAddress.postCode", "error.address.givePostcode") ++
        error("ukAddress.postCode", "error.required")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give an error when the postcode is too long" in {
      val data = completeUkAddress + ("ukAddress.postCode" -> "AA11 11AAA")
      val expectedErrors = error("ukAddress.postCode", "error.address.givePostcodeUsingNumbersAndLetters")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give an error when the postcode is invalid" in {
      val data = completeUkAddress + ("ukAddress.postCode" -> "INVALID")
      val expectedErrors = error("ukAddress.postCode", "error.address.givePostcodeUsingNumbersAndLetters")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give multiple errors when no data is supplied" in {
      val expectedErrors = error("ukAddress.addressLine1", "error.address.give") ++
        error("ukAddress.ukAddressLine2", "") ++
        error("ukAddress.postCode", "error.address.givePostcode") ++
        error("ukAddress.ukAddressLine2", "error.required") ++
        error("ukAddress.postCode", "error.required")

      checkForError(deceasedAddressDetailsUKForm, emptyForm, expectedErrors)
    }
  }

  //endregion

  //region Deceased Address (abroad) tests

  "Deceased Address Details (abroad) form" must {

    "not give an error for valid data" in {
      deceasedAddressDetailsOutsideUKForm.bind(completeAddressAbroad).get shouldBe
        DeceasedDetails(ukAddress = Some(UkAddress("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), "", "AU")))
    }

    "not give an error when lines 2 and 3 are omitted" in {
      val data = completeAddressAbroad - "ukAddress.addressLine3" - "ukAddress.addressLine4"

      deceasedAddressDetailsOutsideUKForm.bind(data).get shouldBe
        DeceasedDetails(ukAddress = Some(UkAddress("Line 1", "Line 2", None, None, "", "AU")))
    }

    "give an error when line 1 is blank" in {
      val data = completeAddressAbroad + ("ukAddress.addressLine1" -> "")
      val expectedErrors = error("ukAddress.addressLine1", "error.address.give")

      checkForError(deceasedAddressDetailsOutsideUKForm, data, expectedErrors)
    }

    "give an error when line 1 is omitted" in {
      val data = completeAddressAbroad - "ukAddress.addressLine1"
      val expectedErrors = error("ukAddress.addressLine1", "error.required")

      checkForError(deceasedAddressDetailsOutsideUKForm, data, expectedErrors)
    }

    "give an error when line 1 is too long" in {
      val data = completeAddressAbroad + ("ukAddress.addressLine1" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddress.addressLine1", "error.address.giveUsing35CharsOrLess")

      checkForError(deceasedAddressDetailsOutsideUKForm, data, expectedErrors)
    }

    "give an error when line 2 is blank" in {
      val data = completeAddressAbroad + ("ukAddress.ukAddressLine2" -> "")
      val expectedErrors = error("ukAddress.ukAddressLine2", "error.address.give")

      checkForError(deceasedAddressDetailsOutsideUKForm, data, expectedErrors)
    }

    "give an error when line 2 is omitted" in {
      val data = completeAddressAbroad - "ukAddress.ukAddressLine2"
      val expectedErrors = error("ukAddress.ukAddressLine2", "error.required")

      checkForError(deceasedAddressDetailsOutsideUKForm, data, expectedErrors)
    }

    "give an error when line 2 is too long" in {
      val data = completeAddressAbroad + ("ukAddress.ukAddressLine2" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddress.ukAddressLine2", "error.address.giveUsing35CharsOrLess")

      checkForError(deceasedAddressDetailsOutsideUKForm, data, expectedErrors)
    }

    "give an error when line 3 is too long" in {
      val data = completeAddressAbroad + ("ukAddress.addressLine3" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddress.addressLine3", "error.address.giveUsing35CharsOrLess")

      checkForError(deceasedAddressDetailsOutsideUKForm, data, expectedErrors)
    }

    "give an error when line 4 is too long" in {
      val data = completeAddressAbroad + ("ukAddress.addressLine4" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddress.addressLine4", "error.address.giveUsing35CharsOrLess")

      checkForError(deceasedAddressDetailsOutsideUKForm, data, expectedErrors)
    }

    "give an error when the country code is blank" in {
      val data = completeAddressAbroad + ("ukAddress.countryCode" -> "")
      val expectedErrors = error("ukAddress.countryCode", "error.country.select")

      checkForError(deceasedAddressDetailsOutsideUKForm, data, expectedErrors)
    }

    "give an error when the country code is omitted" in {
      val data = completeAddressAbroad - "ukAddress.countryCode"
      val expectedErrors = error("ukAddress.countryCode", "error.required")

      checkForError(deceasedAddressDetailsOutsideUKForm, data, expectedErrors)
    }

    "give multiple errors when no data is supplied" in {
      val expectedErrors = error("ukAddress.addressLine1", "error.required") ++
        error("ukAddress.ukAddressLine2", "error.required") ++
        error("ukAddress.countryCode", "error.required")

      checkForError(deceasedAddressDetailsOutsideUKForm, emptyForm, expectedErrors)
    }
  }

  "dateOfBirth" must {
    behave like dateOfBirth[DeceasedDetails](completeAboutDeceased, deceasedForms.aboutDeceasedForm()(messages, createFakeRequest(true), hc, ec))
  }

  //endregion
}
