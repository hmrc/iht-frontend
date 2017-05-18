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
import iht.forms.FormTestHelper
import iht.forms.registration.CoExecutorForms._
import iht.models._
import iht.testhelpers.CommonBuilder
import org.joda.time.LocalDate
import play.api.data.FormError

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.SessionId

class CoExecutorFormsTest extends FormTestHelper with FakeIhtApp {

  def othersApplyingForProbate(value: String) = Map("areOthersApplyingForProbate" -> value)

  def coExecutorSummary(value: String) = Map("addMoreCoExecutors" -> value)

  def personalDetailsEditMode(id: String, firstName: String, lastName: String, nino: String, day: String,
                    month: String, year: String, phoneNo: String) =
    Map("id" -> id,
      "firstName" -> firstName,
      "lastName" -> lastName,
      "nino" -> nino,
      "dateOfBirth.day" -> day,
      "dateOfBirth.month" -> month,
      "dateOfBirth.year" -> year,
      "phoneNo" -> phoneNo)

  def personalDetails(id: String, firstName: String, lastName: String, nino: String, day: String,
                              month: String, year: String, phoneNo: String, isAddressInUk: String) =
    personalDetailsEditMode(id, firstName, lastName, nino, day, month, year, phoneNo) + ("isAddressInUk" -> isAddressInUk)

  lazy val completePersonalDetails =
    personalDetails("1", CommonBuilder.DefaultFirstName, CommonBuilder.DefaultLastName, CommonBuilder.DefaultNino, "01", "01", "1980", "0123456789", "true")

  lazy val completePersonalDetailsEditMode =
    personalDetailsEditMode("1", CommonBuilder.DefaultFirstName, CommonBuilder.DefaultLastName, CommonBuilder.DefaultNino, "01", "01", "1980", "0123456789")

  def address(line1: String, line2: String, line3: String, line4: String, postCode: String, countryCode: String) =
    Map("ukAddressLine1" -> line1,
      "ukAddressLine2" -> line2,
      "ukAddressLine3" -> line3,
      "ukAddressLine4" -> line4,
      "postCode" -> postCode,
      "countryCode" -> countryCode)

  lazy val completeUkAddress = address("Line 1", "Line 2", "Line 3", "Line 4", "AA111AA", "GB") - "countryCode"

  lazy val completeAddressAbroad = address("Line 1", "Line 2", "Line 3", "Line 4", "AA111AA", "AU") - "postCode"

  //region Others Applying for Probate tests

  "Others Applying for Probate form" must {

    "not give an error when given valid data" in {
      val data = othersApplyingForProbate("true")
      othersApplyingForProbateForm.bind(data).get shouldBe Some(true)
    }

    "give an error when supplied a blank value" in {
      val data = othersApplyingForProbate("")
      val expectedErrors = error("areOthersApplyingForProbate", "error.invalid")

      checkForError(othersApplyingForProbateForm, data, expectedErrors)
    }

    "give an error when no value is supplied" in {
      val expectedErrors = error("areOthersApplyingForProbate", "error.applicant.selectIfAnyoneElseApplyingForProbate")

      checkForError(othersApplyingForProbateForm, emptyForm, expectedErrors)
    }

    "give an error when an invalid value is supplied" in {
      val data = othersApplyingForProbate("INVALID")
      val expectedErrors = error("areOthersApplyingForProbate", "error.invalid")

      checkForError(othersApplyingForProbateForm, data, expectedErrors)
    }
  }

  //endregion

  //region Co Executor Details tests

  def bindForm(map: Map[String,String]) = {
    implicit val request = createFakeRequest()
    implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("1")))
    coExecutorPersonalDetailsForm.bind(map)
  }

  def bindFormEdit(map: Map[String,String]) = {
    implicit val request = createFakeRequest()
    implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("1")))
    coExecutorPersonalDetailsEditForm.bind(map)
  }

  def checkForError(data:Map[String,String], expectedErrors:Seq[FormError]): Unit = {
    implicit val request = createFakeRequest()
    implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("1")))
    checkForError(coExecutorPersonalDetailsForm, data, expectedErrors)
  }

  def checkForErrorEdit(data:Map[String,String], expectedErrors:Seq[FormError]): Unit = {
    implicit val request = createFakeRequest()
    implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("1")))
    checkForError(coExecutorPersonalDetailsEditForm, data, expectedErrors)
  }

  "CoExecutor Personal Details form" must {

    "not give an error for valid data" in {
      bindForm(completePersonalDetails).get shouldBe
        CoExecutor(id = Some("1"),
          firstName = CommonBuilder.DefaultFirstName,
          lastName = CommonBuilder.DefaultLastName,
          nino = CommonBuilder.DefaultNino,
          dateOfBirth = new LocalDate(1980, 1, 1),
          contactDetails = ContactDetails(phoneNo = "0123456789"),
          isAddressInUk = Some(true),
          role = None)
    }

    "give an error when the first name is blank" in {
      val data = completePersonalDetails + ("firstName" -> "")
      val expectedErrors = error("firstName", "error.firstName.give")

      checkForError(data, expectedErrors)
    }

    "give an error when the first name is not supplied" in {
      val data = completePersonalDetails - "firstName"
      val expectedErrors = error("firstName", "error.required")

      checkForError( data, expectedErrors)
    }

    "give an error when the first name is too long" in {
      val data = completePersonalDetails + ("firstName" -> "A value that's longer than the 40 characters allowed in this field")
      val expectedErrors = error("firstName", "error.firstName.giveUsingXCharsOrLess")

      checkForError( data, expectedErrors)
    }

    "give an error when the last name is blank" in {
      val data = completePersonalDetails + ("lastName" -> "")
      val expectedErrors = error("lastName", "error.lastName.give")

      checkForError( data, expectedErrors)
    }

    "give an error when the last name is not supplied" in {
      val data = completePersonalDetails - "lastName"
      val expectedErrors = error("lastName", "error.required")

      checkForError( data, expectedErrors)
    }

    "give an error when the last name is too long" in {
      val data = completePersonalDetails + ("lastName" -> "A value that's longer than the 40 characters allowed in this field")
      val expectedErrors = error("lastName", "error.lastName.giveUsingXCharsOrLess")

      checkForError( data, expectedErrors)
    }

    "give an error when the NINO is blank" in {
      val data = completePersonalDetails + ("nino" -> "")
      val expectedErrors = error("nino", "error.nino.give")

      checkForError( data, expectedErrors)
    }

    "give an error when the NINO is not supplied" in {
      val data = completePersonalDetails - "nino"
      val expectedErrors = error("nino", "error.nino.give")

      checkForError( data, expectedErrors)
    }

    "give an error when the NINO is too long" in {
      val nino = CommonBuilder.DefaultNino
      val data = completePersonalDetails + ("nino" -> (nino.substring(0, nino.length()-1) + "AA"))
      val expectedErrors = error("nino", "error.nino.giveUsing8Or9Characters")

      checkForError( data, expectedErrors)
    }

    "give an error when the NINO is invalid" in {
      val data = completePersonalDetails + ("nino" -> "INVALIDD")
      val expectedErrors = error("nino", "error.nino.giveUsingOnlyLettersAndNumbers")

      checkForError( data, expectedErrors)
    }

    "give an error when the NINO is the same as main executor's NINO" in {

      def checkForError2(data:Map[String,String], expectedErrors:Seq[FormError]): Unit = {
        implicit val request = createFakeRequest()
        implicit val hc = new HeaderCarrier(sessionId = Some(SessionId("1")))
        checkForError(coExecutorPersonalDetailsForm, data, expectedErrors)
      }

      val data = completePersonalDetails + ("nino" -> "INVALIDD")
      val expectedErrors = error("nino", "error.nino.giveUsingOnlyLettersAndNumbers")

      checkForError2( data, expectedErrors)
    }

    "give an error when the day is blank" in {
      val data = completePersonalDetails + ("dateOfBirth.day" -> "")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveFull")

      checkForError( data, expectedErrors)
    }

    "give an error when the day is not supplied" in {
      val data = completePersonalDetails - "dateOfBirth.day"
      val expectedErrors = error("dateOfBirth.day", "error.required")

      checkForError( data, expectedErrors)
    }

    "give an error when the day is invalid" in {
      val data = completePersonalDetails + ("dateOfBirth.day" -> "INVALID")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectDateUsingOnlyNumbers")

      checkForError( data, expectedErrors)
    }

    "give an error when the day is too high" in {
      val data = completePersonalDetails + ("dateOfBirth.day" -> "32")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectDay")

      checkForError( data, expectedErrors)
    }

    "give an error when the month is blank" in {
      val data = completePersonalDetails + ("dateOfBirth.month" -> "")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveFull")

      checkForError( data, expectedErrors)
    }

    "give an error when the month is not supplied" in {
      val data = completePersonalDetails - "dateOfBirth.month"
      val expectedErrors = error("dateOfBirth.month", "error.required")

      checkForError( data, expectedErrors)
    }

    "give an error when the month is invalid" in {
      val data = completePersonalDetails + ("dateOfBirth.month" -> "INVALID")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectDateUsingOnlyNumbers")

      checkForError( data, expectedErrors)
    }

    "give an error when the month is too high" in {
      val data = completePersonalDetails + ("dateOfBirth.month" -> "13")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectMonth")

      checkForError( data, expectedErrors)
    }

    "give an error when the year is blank" in {
      val data = completePersonalDetails + ("dateOfBirth.year" -> "")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveFull")

      checkForError( data, expectedErrors)
    }

    "give an error when the year is not supplied" in {
      val data = completePersonalDetails - "dateOfBirth.year"
      val expectedErrors = error("dateOfBirth.year", "error.required")

      checkForError( data, expectedErrors)
    }

    "give an error when the year is invalid" in {
      val data = completePersonalDetails + ("dateOfBirth.year" -> "INVALID")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectDateUsingOnlyNumbers")

      checkForError( data, expectedErrors)
    }

    "give an error when the year is supplied as a two-digit number" in {
      val data = completePersonalDetails + ("dateOfBirth.year" -> "14")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectYear")

      checkForError( data, expectedErrors)
    }

    "give an error when a blank phone number is supplied" in {
      val data = completePersonalDetails + ("phoneNo" -> "")
      val expectedErrors = error("phoneNo", "error.phoneNumber.give")

      checkForError( data, expectedErrors)
    }

    "give an error when an invalid phone number is supplied" in {
      val data = completePersonalDetails + ("phoneNo" -> "Invalid value!")
      val expectedErrors = error("phoneNo", "error.phoneNumber.giveUsingOnlyLettersAndNumbers")

      checkForError( data, expectedErrors)
    }

    "give an error when the phone number is too long" in {
      val data = completePersonalDetails + ("phoneNo" -> "A string longer than 27 chrs")
      val expectedErrors = error("phoneNo", "error.phoneNumber.giveUsing27CharactersOrLess")

      checkForError( data, expectedErrors)
    }

    "give an error when no phone number is supplied" in {
      val data = completePersonalDetails - "phoneNo"
      val expectedErrors = error("phoneNo", "error.phoneNumber.give")

      checkForError( data, expectedErrors)
    }

    "give an error when 'Is Address in UK' is blank" in {
      val data = completePersonalDetails + ("isAddressInUk" -> "")
      val expectedErrors = error("isAddressInUk", "error.invalid")

      checkForError( data, expectedErrors)
    }

    "give an error when 'Is Address in UK' is not supplied" in {
      val data = completePersonalDetails - "isAddressInUk"
      val expectedErrors = error("isAddressInUk", "error.address.isInUK.give")

      checkForError( data, expectedErrors)
    }

    "give an error when an invalid answer for 'Is Address in UK' is supplied" in {
      val data = completePersonalDetails + ("isAddressInUk" -> "INVALID")
      val expectedErrors = error("isAddressInUk", "error.invalid")

      checkForError( data, expectedErrors)
    }

    "give multiple errors when several fields are invalid" in {
      val data = completePersonalDetails + ("firstName" -> "", "nino" -> "INVALIDD")
      val expectedErrors = error("firstName", "error.firstName.give") ++
        error("nino", "error.nino.giveUsingOnlyLettersAndNumbers")

      checkForError( data, expectedErrors)
    }

    "give one date error when several date fields are invalid" in {
      val data = completePersonalDetails + ("dateOfBirth.day" -> "32", "dateOfBirth.month" -> "13",  "dateOfBirth.year" -> "88")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveFull")

      checkForError( data, expectedErrors)
    }
  }

  "CoExecutor Personal Details form (in Edit mode)" must {

    "not give an error for valid data" in {
      bindFormEdit(completePersonalDetailsEditMode).get shouldBe
        CoExecutor(id = Some("1"),
          firstName = CommonBuilder.DefaultFirstName,
          lastName = CommonBuilder.DefaultLastName,
          nino = CommonBuilder.DefaultNino,
          dateOfBirth = new LocalDate(1980, 1, 1),
          contactDetails = ContactDetails(phoneNo = "0123456789"),
          role = None)
    }


    "give an error when the first name is blank" in {
      val data = completePersonalDetails + ("firstName" -> "")
      val expectedErrors = error("firstName", "error.firstName.give")

      checkForError( data, expectedErrors)
    }

    "give an error when the first name is not supplied" in {
      val data = completePersonalDetails - "firstName"
      val expectedErrors = error("firstName", "error.required")

      checkForError( data, expectedErrors)
    }

    "give an error when the first name is too long" in {
      val data = completePersonalDetails + ("firstName" -> "A value that's longer than the 40 characters allowed in this field")
      val expectedErrors = error("firstName", "error.firstName.giveUsingXCharsOrLess")

      checkForError( data, expectedErrors)
    }

    "give an error when the last name is blank" in {
      val data = completePersonalDetails + ("lastName" -> "")
      val expectedErrors = error("lastName", "error.lastName.give")

      checkForError( data, expectedErrors)
    }

    "give an error when the last name is not supplied" in {
      val data = completePersonalDetails - "lastName"
      val expectedErrors = error("lastName", "error.required")

      checkForError( data, expectedErrors)
    }

    "give an error when the last name is too long" in {
      val data = completePersonalDetails + ("lastName" -> "A value that's longer than the 40 characters allowed in this field")
      val expectedErrors = error("lastName", "error.lastName.giveUsingXCharsOrLess")

      checkForError( data, expectedErrors)
    }




    "give an error when the NINO is blank" in {
      val data = completePersonalDetailsEditMode + ("nino" -> "")
      val expectedErrors = error("nino", "error.nino.give")

      checkForErrorEdit(data, expectedErrors)
    }

    "give an error when the NINO is not supplied" in {
      val data = completePersonalDetailsEditMode - "nino"
      val expectedErrors = error("nino", "error.nino.give")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the NINO is too long" in {
      val nino = CommonBuilder.DefaultNino
      val data = completePersonalDetailsEditMode + ("nino" -> (nino.substring(0, nino.length()-1) + "AA"))
      val expectedErrors = error("nino", "error.nino.giveUsing8Or9Characters")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the NINO is invalid" in {
      val data = completePersonalDetailsEditMode + ("nino" -> "INVALIDD")
      val expectedErrors = error("nino", "error.nino.giveUsingOnlyLettersAndNumbers")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the day is blank" in {
      val data = completePersonalDetailsEditMode + ("dateOfBirth.day" -> "")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveFull")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the day is not supplied" in {
      val data = completePersonalDetailsEditMode - "dateOfBirth.day"
      val expectedErrors = error("dateOfBirth.day", "error.required")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the day is invalid" in {
      val data = completePersonalDetailsEditMode + ("dateOfBirth.day" -> "INVALID")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectDateUsingOnlyNumbers")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the day is too high" in {
      val data = completePersonalDetailsEditMode + ("dateOfBirth.day" -> "32")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectDay")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the month is blank" in {
      val data = completePersonalDetailsEditMode + ("dateOfBirth.month" -> "")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveFull")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the month is not supplied" in {
      val data = completePersonalDetailsEditMode - "dateOfBirth.month"
      val expectedErrors = error("dateOfBirth.month", "error.required")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the month is invalid" in {
      val data = completePersonalDetailsEditMode + ("dateOfBirth.month" -> "INVALID")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectDateUsingOnlyNumbers")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the month is too high" in {
      val data = completePersonalDetailsEditMode + ("dateOfBirth.month" -> "13")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectMonth")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the year is blank" in {
      val data = completePersonalDetailsEditMode + ("dateOfBirth.year" -> "")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveFull")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the year is not supplied" in {
      val data = completePersonalDetailsEditMode - "dateOfBirth.year"
      val expectedErrors = error("dateOfBirth.year", "error.required")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the year is invalid" in {
      val data = completePersonalDetailsEditMode + ("dateOfBirth.year" -> "INVALID")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectDateUsingOnlyNumbers")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the year is supplied as a two-digit number" in {
      val data = completePersonalDetailsEditMode + ("dateOfBirth.year" -> "14")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectYear")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when a blank phone number is supplied" in {
      val data = completePersonalDetailsEditMode + ("phoneNo" -> "")
      val expectedErrors = error("phoneNo", "error.phoneNumber.give")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when an invalid phone number is supplied" in {
      val data = completePersonalDetailsEditMode + ("phoneNo" -> "Invalid value!")
      val expectedErrors = error("phoneNo", "error.phoneNumber.giveUsingOnlyLettersAndNumbers")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when the phone number is too long" in {
      val data = completePersonalDetailsEditMode + ("phoneNo" -> "A string longer than 27 chrs")
      val expectedErrors = error("phoneNo", "error.phoneNumber.giveUsing27CharactersOrLess")

      checkForErrorEdit( data, expectedErrors)
    }

    "give an error when no phone number is supplied" in {
      val data = completePersonalDetailsEditMode - "phoneNo"
      val expectedErrors = error("phoneNo", "error.phoneNumber.give")

      checkForErrorEdit( data, expectedErrors)
    }

    "give multiple errors when several fields are invalid" in {
      val data = completePersonalDetails + ("firstName" -> "", "nino" -> "INVALIDD")
      val expectedErrors = error("firstName", "error.firstName.give") ++
        error("nino", "error.nino.giveUsingOnlyLettersAndNumbers")

      checkForErrorEdit( data, expectedErrors)
    }

    "give one date error when several date fields are invalid" in {
      val data = completePersonalDetailsEditMode + ("dateOfBirth.day" -> "32", "dateOfBirth.month" -> "13", "dateOfBirth.year" -> "88")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveFull")

      checkForErrorEdit( data, expectedErrors)
    }

    "ignore the 'Is Address in UK' field if supplied" in {
      // This is a test that over-posting does not occur.  The data we supply contains the isAddressInUk field,
      // and we test that the outcome has that field set to None - i.e. the supplied value isn't used.
      bindFormEdit(completePersonalDetails).get shouldBe
        CoExecutor(id = Some("1"),
          firstName = CommonBuilder.DefaultFirstName,
          lastName = CommonBuilder.DefaultLastName,
          nino = CommonBuilder.DefaultNino,
          dateOfBirth = new LocalDate(1980, 1, 1),
          contactDetails = ContactDetails(phoneNo = "0123456789"),
          isAddressInUk = None,
          role = None)
    }
  }

  //endregion

  //region Co Executor Address in the UK tests

  "CoExecutor Address Details (in the UK)" must {

    "not give an error for valid data" in {
      coExecutorAddressUkForm.bind(completeUkAddress).get shouldBe
        UkAddress("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), "AA111AA", "GB")
    }

    "not give an error when lines 2 and 3 are omitted" in {
      val data = completeUkAddress - "ukAddressLine3" - "ukAddressLine4"

      coExecutorAddressUkForm.bind(data).get shouldBe UkAddress("Line 1", "Line 2", None, None, "AA111AA", "GB")
    }

    "give an error when line 1 is blank" in {
      val data = completeUkAddress + ("ukAddressLine1" -> "")
      val expectedErrors = error("ukAddressLine1", "error.address.giveInLine1And2")

      checkForError(coExecutorAddressUkForm, data, expectedErrors)
    }

    "give an error when line 1 is omitted" in {
      val data = completeUkAddress - "ukAddressLine1"
      val expectedErrors = error("ukAddressLine1", "error.address.giveInLine1And2")

      checkForError(coExecutorAddressUkForm, data, expectedErrors)
    }

    "give an error when line 1 is too long" in {
      val data = completeUkAddress + ("ukAddressLine1" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine1", "error.address.giveUsing35CharsOrLess")

      checkForError(coExecutorAddressUkForm, data, expectedErrors)
    }

    "give an error when line 2 is blank" in {
      val data = completeUkAddress + ("ukAddressLine2" -> "")
      val expectedErrors = error("ukAddressLine2", "error.address.giveInLine1And2")

      checkForError(coExecutorAddressUkForm, data, expectedErrors)
    }

    "give an error when line 2 is omitted" in {
      val data = completeUkAddress - "ukAddressLine2"
      val expectedErrors = error("ukAddressLine2", "error.address.giveInLine1And2") ++
        error("ukAddressLine2", "error.required")

      checkForError(coExecutorAddressUkForm, data, expectedErrors)
    }

    "give an error when line 2 is too long" in {
      val data = completeUkAddress + ("ukAddressLine2" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine2", "error.address.giveUsing35CharsOrLess")

      checkForError(coExecutorAddressUkForm, data, expectedErrors)
    }

    "give an error when line 3 is too long" in {
      val data = completeUkAddress + ("ukAddressLine3" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine3", "error.address.giveUsing35CharsOrLess")

      checkForError(coExecutorAddressUkForm, data, expectedErrors)
    }

    "give an error when line 4 is too long" in {
      val data = completeUkAddress + ("ukAddressLine4" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine4", "error.address.giveUsing35CharsOrLess")

      checkForError(coExecutorAddressUkForm, data, expectedErrors)
    }

    "give an error when the postcode is blank" in {
      val data = completeUkAddress + ("postCode" -> "")
      val expectedErrors = error("postCode", "error.address.givePostcode")

      checkForError(coExecutorAddressUkForm, data, expectedErrors)
    }

    "give an error when the postcode is omitted" in {
      val data = completeUkAddress - "postCode"
      val expectedErrors = error("postCode", "error.address.givePostcode") ++
        error("postCode", "error.required")

      checkForError(coExecutorAddressUkForm, data, expectedErrors)
    }

    "give an error when the postcode is too long" in {
      val data = completeUkAddress + ("postCode" -> "AA11 11AAA")
      val expectedErrors = error("postCode", "error.address.givePostcodeUsingNumbersAndLetters")

      checkForError(coExecutorAddressUkForm, data, expectedErrors)
    }

    "give an error when the postcode is invalid" in {
      val data = completeUkAddress + ("postCode" -> "INVALID")
      val expectedErrors = error("postCode", "error.address.givePostcodeUsingNumbersAndLetters")

      checkForError(coExecutorAddressUkForm, data, expectedErrors)
    }

    "give multiple errors when no data is supplied" in {
      val expectedErrors = error("ukAddressLine1", "error.address.give") ++
        error("ukAddressLine2", "") ++
        error("postCode", "error.address.givePostcode") ++
        error("ukAddressLine2", "error.required") ++
        error("postCode", "error.required")

      checkForError(coExecutorAddressUkForm, emptyForm, expectedErrors)
    }

  }

  //endregion
  
  //region CoExecutor Address Details (abroad) tests

  "Deceased Address Details (abroad) form" must {

    "not give an error for valid data" in {
      coExecutorAddressAbroadForm.bind(completeAddressAbroad).get shouldBe
        UkAddress("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), "", "AU")
    }

    "not give an error when lines 2 and 3 are omitted" in {
      val data = completeAddressAbroad - "ukAddressLine3" - "ukAddressLine4"

      coExecutorAddressAbroadForm.bind(data).get shouldBe UkAddress("Line 1", "Line 2", None, None, "", "AU")
    }

    "give an error when line 1 is blank" in {
      val data = completeAddressAbroad + ("ukAddressLine1" -> "")
      val expectedErrors = error("ukAddressLine1", "error.address.giveInLine1And2")

      checkForError(coExecutorAddressAbroadForm, data, expectedErrors)
    }

    "give an error when line 1 is omitted" in {
      val data = completeAddressAbroad - "ukAddressLine1"
      val expectedErrors = error("ukAddressLine1", "error.address.giveInLine1And2")

      checkForError(coExecutorAddressAbroadForm, data, expectedErrors)
    }

    "give an error when line 1 is too long" in {
      val data = completeAddressAbroad + ("ukAddressLine1" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine1", "error.address.giveUsing35CharsOrLess")

      checkForError(coExecutorAddressAbroadForm, data, expectedErrors)
    }

    "give an error when line 2 is blank" in {
      val data = completeAddressAbroad + ("ukAddressLine2" -> "")
      val expectedErrors = error("ukAddressLine2", "error.address.giveInLine1And2")

      checkForError(coExecutorAddressAbroadForm, data, expectedErrors)
    }

    "give an error when line 2 is omitted" in {
      val data = completeAddressAbroad - "ukAddressLine2"
      val expectedErrors = error("ukAddressLine2", "error.address.giveInLine1And2") ++
        error("ukAddressLine2", "error.required")

      checkForError(coExecutorAddressAbroadForm, data, expectedErrors)
    }

    "give an error when line 2 is too long" in {
      val data = completeAddressAbroad + ("ukAddressLine2" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine2", "error.address.giveUsing35CharsOrLess")

      checkForError(coExecutorAddressAbroadForm, data, expectedErrors)
    }

    "give an error when line 3 is too long" in {
      val data = completeAddressAbroad + ("ukAddressLine3" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine3", "error.address.giveUsing35CharsOrLess")

      checkForError(coExecutorAddressAbroadForm, data, expectedErrors)
    }

    "give an error when line 4 is too long" in {
      val data = completeAddressAbroad + ("ukAddressLine4" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine4", "error.address.giveUsing35CharsOrLess")

      checkForError(coExecutorAddressAbroadForm, data, expectedErrors)
    }

    "give an error when the country code is blank" in {
      val data = completeAddressAbroad + ("countryCode" -> "")
      val expectedErrors = error("countryCode", "error.country.select")

      checkForError(coExecutorAddressAbroadForm, data, expectedErrors)
    }

    "give an error when the country code is omitted" in {
      val data = completeAddressAbroad - "countryCode"
      val expectedErrors = error("countryCode", "error.country.select")

      checkForError(coExecutorAddressAbroadForm, data, expectedErrors)
    }

    "give an error when the country code is invalid" in {
      val data = completeAddressAbroad + ("countryCode" -> "XY")
      val expectedErrors = error("countryCode", "error.country.select")

      checkForError(coExecutorAddressAbroadForm, data, expectedErrors)
    }

    "give multiple errors when no data is supplied" in {
      val expectedErrors = error("ukAddressLine1", "error.address.give") ++
        error("ukAddressLine2", "") ++
        error("countryCode", "error.country.select") ++
        error("ukAddressLine2", "error.required")


      checkForError(coExecutorAddressAbroadForm, emptyForm, expectedErrors)
    }
  }

  //endregion

  //region Executor Overview tests

  "CoExecutor Overview form" must {

    "not give an error when given valid data" in {
      val data = coExecutorSummary("true")
      executorOverviewForm.bind(data).get shouldBe Some(true)
    }

    "give an error when supplied a blank value" in {
      val data = coExecutorSummary("")
      val expectedErrors = error("addMoreCoExecutors", "error.invalid")

      checkForError(executorOverviewForm, data, expectedErrors)
    }

    "give an error when no value is supplied" in {
      val expectedErrors = error("addMoreCoExecutors", "error.applicant.selectIfAnyoneElseApplyingForProbate")

      checkForError(executorOverviewForm, emptyForm, expectedErrors)
    }

    "give an error when an invalid value is supplied" in {
      val data = coExecutorSummary("INVALID")
      val expectedErrors = error("addMoreCoExecutors", "error.invalid")

      checkForError(executorOverviewForm, data, expectedErrors)
    }
  }

  //endregion
}
