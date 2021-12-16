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

package iht.forms.registration

import iht.FakeIhtApp
import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.forms.FormTestHelper
import iht.forms.registration.DeceasedForms._
import iht.models.{DeceasedDateOfDeath, DeceasedDetails, RegistrationDetails, UkAddress}
import iht.testhelpers.{CommonBuilder, NinoBuilder}
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, FormError, Forms}
import play.api.i18n.Messages
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.ExecutionContext

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
    firstName, surname, CommonBuilder.DefaultNino, "01", "01", "1980", mockAppConfig.statusSingle)

  def deceasedAddress(line1: String, line2: String, line3: String, line4: String, postCode: String, countryCode: String) =
    Map("ukAddress.ukAddressLine1" -> line1,
      "ukAddress.ukAddressLine2" -> line2,
      "ukAddress.ukAddressLine3" -> line3,
      "ukAddress.ukAddressLine4" -> line4,
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
      deceasedDateOfDeathForm.bind(completeDateOfDeath).get mustBe DeceasedDateOfDeath(new LocalDate(2015, 1, 1))
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

    "give an error when a date beyond the cut-off date is supplied" in {
      val innerMockAppConfig = mock[AppConfig]
      val cutoffDateForm = deceasedDateOfDeathForm()(innerMockAppConfig)
      when(innerMockAppConfig.dateOfDeathCutOffDate).thenReturn(new LocalDate(2000, 1, 1))

      val data: Map[String, String] = dateOfDeath("01", "01", "2013")
      checkForError(cutoffDateForm, data, error("dateOfDeath", "error.dateOfDeath.deceasedBefore2022"))
    }
  }

  //endregion

  //region Deceased Permanent Home tests

  "Deceased Permanent Home form" must {

    "not give an error for a valid answer" in {
      val data = permanentHome(mockAppConfig.domicileEnglandOrWales)
      deceasedPermanentHomeForm(messages, mockAppConfig).bind(data).get mustBe DeceasedDetails(domicile = Some(mockAppConfig.domicileEnglandOrWales))
    }

    "give an error when a blank value is supplied" in {
      val data = permanentHome("")
      val expectedErrors = error("domicile", "error.invalid")

      checkForError(deceasedPermanentHomeForm(messages, mockAppConfig), data, expectedErrors)
    }

    "give an error when invalid data is supplied" in {
      val data = permanentHome("INVALID")
      val expectedErrors = error("domicile", "error.invalid")

      checkForError(deceasedPermanentHomeForm(messages, mockAppConfig), data, expectedErrors)
    }

    "give an error when no data is supplied" in {
      val expectedErrors = error("domicile", "error.deceasedPermanentHome.selectLocation")

      checkForError(deceasedPermanentHomeForm(messages, mockAppConfig), emptyForm, expectedErrors)
    }
  }

  //endregion

  //region About Deceased tests

  def deceasedFormsGen(customFormatter: Option[FieldMapping[String]] = None): DeceasedForms = {
    new DeceasedForms {
      override def ninoForDeceased(blankMessageKey: String, lengthMessageKey: String,
                                   formatMessageKey: String, oRegDetails: Option[RegistrationDetails])
                                  (implicit appConfig: AppConfig): FieldMapping[String] = {
        customFormatter getOrElse {
          val formatter = new Formatter[String] {
            override val format: Option[(String, Seq[Any])] = None

            override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = Right("")

            override def unbind(key: String, value: String): Map[String, String] = Map.empty
          }
          Forms.of(formatter)
        }
      }
    }
  }

  def formsWithIhtFormValidatorMockedToFail = {
    val formatter = mock[Formatter[String]]
    when(formatter.bind(any(), any())).thenReturn(Left(Seq(FormError("nino", "error.nino.alreadyGiven"))))
    when(formatter.format).thenReturn(None)

    val fieldMapping: FieldMapping[String] = Forms.of(formatter)

    deceasedFormsGen(Some(fieldMapping))
  }

  def formsWithIhtFormValidatorMockedToSucceed(nino:String): DeceasedForms = {
    val formatter = mock[Formatter[String]]
    when(formatter.bind(any(), any())).thenReturn(Right(nino))
    when(formatter.format).thenReturn(None)

    val fieldMapping: FieldMapping[String] = Forms.of(formatter)

    deceasedFormsGen(Some(fieldMapping))
  }

  "About Deceased form" must {

    "not give an error for valid data" in {
      val nino = NinoBuilder.randomNino.toString

      def bindFormEdit(map: Map[String, String]) = {
        implicit val msg: Messages = messages
        formsWithIhtFormValidatorMockedToSucceed(nino).aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino).bind(map)
      }

      bindFormEdit(completeAboutDeceased).get mustBe
        DeceasedDetails(firstName = Some(firstName),
          middleName = None,
          lastName = Some(surname),
          nino = Some(nino),
          ukAddress = None,
          dateOfBirth = Some(new LocalDate(1980, 1, 1)),
          maritalStatus = Some(mockAppConfig.statusSingle),
          domicile = None,
          isAddressInUK = None)
    }

    "give an error when the first name is blank" in {
      val data = completeAboutDeceased + ("firstName" -> "")
      val expectedErrors = error("firstName", "error.firstName.give")

      checkForError(deceasedFormsGen().aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino)(messages, mockAppConfig), data, expectedErrors)
    }

    "give an error when the first name is not supplied" in {
      val data = completeAboutDeceased - "firstName"
      val expectedErrors = error("firstName", "error.required")

      checkForError(deceasedFormsGen().aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino)(messages, mockAppConfig), data, expectedErrors)
    }

    "give an error when the first name is too long" in {
      val data = completeAboutDeceased + ("firstName" -> "A value that's longer than the 40 characters allowed in this field")
      val expectedErrors = error("firstName", "error.firstName.giveUsingXCharsOrLess")

      checkForError(deceasedFormsGen().aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino)(messages, mockAppConfig), data, expectedErrors)
    }

    "give an error when the last name is blank" in {
      val data = completeAboutDeceased + ("lastName" -> "")
      val expectedErrors = error("lastName", "error.lastName.give")

      checkForError(deceasedFormsGen().aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino)(messages, mockAppConfig), data, expectedErrors)
    }

    "give an error when the last name is not supplied" in {
      val data = completeAboutDeceased - "lastName"
      val expectedErrors = error("lastName", "error.required")

      checkForError(deceasedFormsGen().aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino)(messages, mockAppConfig), data, expectedErrors)
    }

    "give an error when the last name is too long" in {
      val data = completeAboutDeceased + ("lastName" -> "A value that's longer than the 40 characters allowed in this field")
      val expectedErrors = error("lastName", "error.lastName.giveUsingXCharsOrLess")

      checkForError(deceasedFormsGen().aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino)(messages, mockAppConfig), data, expectedErrors)
    }

    "give an error when a blank marital status is supplied" in {
      val data = completeAboutDeceased + ("maritalStatus" -> "")
      val expectedErrors = error("maritalStatus", "error.invalid")

      checkForError(deceasedFormsGen().aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino)(messages, mockAppConfig), data, expectedErrors)
    }

    "give an error when an invalid marital status is supplied" in {
      val data = completeAboutDeceased + ("maritalStatus" -> "INVALID")
      val expectedErrors = error("maritalStatus", "error.invalid")

      checkForError(deceasedFormsGen().aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino)(messages, mockAppConfig), data, expectedErrors)
    }

    "give an error when no marital status is supplied" in {
      val data = completeAboutDeceased - "maritalStatus"
      val expectedErrors = error("maritalStatus", "error.deceasedMaritalStatus.select")

      checkForError(deceasedFormsGen().aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino)(messages, mockAppConfig), data, expectedErrors)
    }

    "give multiple errors when several fields are invalid" in {
      val data = completeAboutDeceased + ("firstName" -> "", "lastName" -> "")
      val expectedErrors = error("firstName", "error.firstName.give") ++
        error("lastName", "error.lastName.give")
      val form = deceasedFormsGen().aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino)(messages, mockAppConfig)

      checkForError(form, data, expectedErrors)
    }

    "give one date error when several date fields are invalid" in {
      val data = completeAboutDeceased + ("dateOfBirth.day" -> "32", "dateOfBirth.month" -> "13", "dateOfBirth.year" -> "12", "dateOfBirth.day" -> "99")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveFull")

      checkForError(deceasedFormsGen().aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino)(messages, mockAppConfig), data, expectedErrors)
    }

    "indicate validation error when nino for deceased validation fails" in {
      def checkForError(data: Map[String, String], expectedErrors: Seq[FormError]): Unit = {
        implicit val msg = messages
        super.checkForError(formsWithIhtFormValidatorMockedToFail
          .aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino), data, expectedErrors)
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
      deceasedAddressQuestionForm.bind(data).get mustBe DeceasedDetails(isAddressInUK = Some(true))
    }

    "not give an error when answered No" in {
      val data = deceasedAddressQuestion("false")
      deceasedAddressQuestionForm.bind(data).get mustBe DeceasedDetails(isAddressInUK = Some(false))
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
      deceasedAddressDetailsUKForm.bind(completeUkAddress).get mustBe
        DeceasedDetails(ukAddress = Some(UkAddress("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), "AA111AA", "GB")))
    }

    "not give an error when lines 2 and 3 are omitted" in {
      val data = completeUkAddress - "ukAddress.ukAddressLine3" - "ukAddress.ukAddressLine4"

      deceasedAddressDetailsUKForm.bind(data).get mustBe
        DeceasedDetails(ukAddress = Some(UkAddress("Line 1", "Line 2", None, None, "AA111AA", "GB")))
    }

    "give an error when line 1 is blank" in {
      val data = completeUkAddress + ("ukAddress.ukAddressLine1" -> "")
      val expectedErrors = error("ukAddress.ukAddressLine1", "error.address.giveInLine1And2")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give an error when line 1 is omitted" in {
      val data = completeUkAddress - "ukAddress.ukAddressLine1"
      val expectedErrors = error("ukAddress.ukAddressLine1", "error.address.giveInLine1And2")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give an error when line 1 is too long" in {
      val data = completeUkAddress + ("ukAddress.ukAddressLine1" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddress.ukAddressLine1", "error.address.giveUsing35CharsOrLess")

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
      val data = completeUkAddress + ("ukAddress.ukAddressLine3" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddress.ukAddressLine3", "error.address.giveUsing35CharsOrLess")

      checkForError(deceasedAddressDetailsUKForm, data, expectedErrors)
    }

    "give an error when line 4 is too long" in {
      val data = completeUkAddress + ("ukAddress.ukAddressLine4" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddress.ukAddressLine4", "error.address.giveUsing35CharsOrLess")

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
      val expectedErrors = error("ukAddress.ukAddressLine1", "error.address.give") ++
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
      deceasedAddressDetailsOutsideUKForm.bind(completeAddressAbroad).get mustBe
        DeceasedDetails(ukAddress = Some(UkAddress("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), "", "AU")))
    }

    "not give an error when lines 2 and 3 are omitted" in {
      val data = completeAddressAbroad - "ukAddress.ukAddressLine3" - "ukAddress.ukAddressLine4"

      deceasedAddressDetailsOutsideUKForm.bind(data).get mustBe
        DeceasedDetails(ukAddress = Some(UkAddress("Line 1", "Line 2", None, None, "", "AU")))
    }

    "give an error when line 1 is blank" in {
      val data = completeAddressAbroad + ("ukAddress.ukAddressLine1" -> "")
      val expectedErrors = error("ukAddress.ukAddressLine1", "error.address.give")

      checkForError(deceasedAddressDetailsOutsideUKForm, data, expectedErrors)
    }

    "give an error when line 1 is omitted" in {
      val data = completeAddressAbroad - "ukAddress.ukAddressLine1"
      val expectedErrors = error("ukAddress.ukAddressLine1", "error.required")

      checkForError(deceasedAddressDetailsOutsideUKForm, data, expectedErrors)
    }

    "give an error when line 1 is too long" in {
      val data = completeAddressAbroad + ("ukAddress.ukAddressLine1" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddress.ukAddressLine1", "error.address.giveUsing35CharsOrLess")

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
      val data = completeAddressAbroad + ("ukAddress.ukAddressLine3" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddress.ukAddressLine3", "error.address.giveUsing35CharsOrLess")

      checkForError(deceasedAddressDetailsOutsideUKForm, data, expectedErrors)
    }

    "give an error when line 4 is too long" in {
      val data = completeAddressAbroad + ("ukAddress.ukAddressLine4" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddress.ukAddressLine4", "error.address.giveUsing35CharsOrLess")

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
      val expectedErrors = error("ukAddress.ukAddressLine1", "error.required") ++
        error("ukAddress.ukAddressLine2", "error.required") ++
        error("ukAddress.countryCode", "error.required")

      checkForError(deceasedAddressDetailsOutsideUKForm, emptyForm, expectedErrors)
    }
  }

  "dateOfBirth" must {
    behave like dateOfBirth[DeceasedDetails](completeAboutDeceased, deceasedFormsGen().aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino)(messages, mockAppConfig))
  }
}
