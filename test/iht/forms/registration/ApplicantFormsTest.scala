/*
 * Copyright 2020 HM Revenue & Customs
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
import iht.forms.registration.ApplicantForms._
import iht.models.{ApplicantDetails, UkAddress}
import play.api.i18n.Lang

class ApplicantFormsTest extends FormTestHelper with FakeIhtApp {

  implicit val lang = Lang.defaultLang
  implicit val msg = messages

  def applyingForProbate(value: String) = Map("isApplyingForProbate" -> value)

  def probateLocation(value: String) = Map("country" -> value)

  def applicantTellUsAboutYourself(phoneNo: String, doesLiveInUk: String) =
    Map("phoneNo" -> phoneNo, "doesLiveInUK" -> doesLiveInUk)

  def applicantTellUsAboutYourselfEdit(phoneNo: String) = Map("phoneNo" -> phoneNo)

  def address(line1: String, line2: String, line3: String, line4: String, postCode: String, countryCode: String) =
    Map("ukAddressLine1" -> line1,
      "ukAddressLine2" -> line2,
      "ukAddressLine3" -> line3,
      "ukAddressLine4" -> line4,
      "postCode" -> postCode,
      "countryCode" -> countryCode)

  lazy val completeUkAddress = address("Line 1", "Line 2", "Line 3", "Line 4", "AA111AA", "GB") - "countryCode"

  lazy val completeAddressAbroad = address("Line 1", "Line 2", "Line 3", "Line 4", "AA111AA", "AU") - "postCode"

  //region Applying for Probate tests

  "Applying for Probate form" must {

    "not give an error when answered Yes" in {
      val data = applyingForProbate("true")
      applyingForProbateForm.bind(data).get mustBe ApplicantDetails(isApplyingForProbate = Some(true), role = Some(mockAppConfig.roleLeadExecutor))
    }

    "not give an error when answered No" in {
      val data = applyingForProbate("false")
      applyingForProbateForm.bind(data).get mustBe ApplicantDetails(isApplyingForProbate = Some(false), role = Some(mockAppConfig.roleLeadExecutor))
    }

    "give an error when the question is not answered" in {
      val expectedErrors = error("isApplyingForProbate", "error.applicantIsApplyingForProbate.select")

      checkForError(applyingForProbateForm, emptyForm, expectedErrors)
    }

    "give an error when an invalid answer is given" in {
      val data = applyingForProbate("INVALID")
      val expectedErrors = error("isApplyingForProbate", "error.invalid")

      checkForError(applyingForProbateForm, data, expectedErrors)
    }

  }

  //endregion

  //region Deceased Permanent Home tests

  "Probate Location form" must {

    "not give an error for a valid answer" in {
      val data = probateLocation(mockAppConfig.applicantCountryEnglandOrWales)
      probateLocationForm(messages, mockAppConfig).bind(data).get mustBe
        ApplicantDetails(country = Some(mockAppConfig.applicantCountryEnglandOrWales), role = Some(mockAppConfig.roleLeadExecutor))
    }

    "give an error when a blank value is supplied" in {
      val data = probateLocation("")
      val expectedErrors = error("country", "error.invalid")

      checkForError(probateLocationForm(messages, mockAppConfig), data, expectedErrors)
    }

    "give an error when invalid data is supplied" in {
      val data = probateLocation("INVALID")
      val expectedErrors = error("country", "error.invalid")

      checkForError(probateLocationForm(messages, mockAppConfig), data, expectedErrors)
    }

    "give an error when no data is supplied" in {
      val expectedErrors = error("country", "error.applicantProbateLocation.select")

      checkForError(probateLocationForm(messages, mockAppConfig), emptyForm, expectedErrors)
    }
  }

  //endregion

  //region About Applicant tests

  "Applicant Tell Us About Yourself form" must {

    "not give an error for valid data" in {
      // Note that Applicant Details set the role to Lead Executor by default, hence setting it to None for comparison here
      val data = applicantTellUsAboutYourself("0123456789", "true")
      applicantTellUsAboutYourselfForm.bind(data).get mustBe
        ApplicantDetails(phoneNo = Some("0123456789"), doesLiveInUK = Some(true), role = None)
    }

    "give an error when a blank phone number is supplied" in {
      val data = applicantTellUsAboutYourself("", "true")
      val expectedErrors = error("phoneNo", "error.phoneNumber.give")

      checkForError(applicantTellUsAboutYourselfForm, data, expectedErrors)
    }

    "give an error when an invalid phone number is supplied" in {
      val data = applicantTellUsAboutYourself("Invalid value!", "true")
      val expectedErrors = error("phoneNo", "error.phoneNumber.giveUsingOnlyLettersAndNumbers")

      checkForError(applicantTellUsAboutYourselfForm, data, expectedErrors)
    }

    "give an error when the phone number is too long" in {
      val data = applicantTellUsAboutYourself("A string longer than 27 chrs", "true")
      val expectedErrors = error("phoneNo", "error.phoneNumber.giveUsing27CharactersOrLess")

      checkForError(applicantTellUsAboutYourselfForm, data, expectedErrors)
    }

    "give an error when no phone number is supplied" in {
      val data = Map("doesLiveInUK" -> "true")
      val expectedErrors = error("phoneNo", "error.phoneNumber.give")

      checkForError(applicantTellUsAboutYourselfForm, data, expectedErrors)
    }

    "give an error when 'Lives in UK' is blank" in {
      val data = applicantTellUsAboutYourself("0123456789", "")
      val expectedErrors = error("doesLiveInUK", "error.invalid")

      checkForError(applicantTellUsAboutYourselfForm, data, expectedErrors)
    }

    "give an error when 'Lives in UK' is not supplied" in {
      val data = Map("phoneNo" -> "0123456789")
      val expectedErrors = error("doesLiveInUK", "error.address.isInUK.give")

      checkForError(applicantTellUsAboutYourselfForm, data, expectedErrors)
    }

    "give an error when invalid data is supplied for 'Lives in UK'" in {
      val data = applicantTellUsAboutYourself("0123456789", "INVALID")
      val expectedErrors = error("doesLiveInUK", "error.invalid")

      checkForError(applicantTellUsAboutYourselfForm, data, expectedErrors)
    }

    "give two errors when both phone number and 'Lives in UK' are in error" in {
      val expectedErrors = error("phoneNo", "error.phoneNumber.give") ++
        error("doesLiveInUK", "error.address.isInUK.give")

      checkForError(applicantTellUsAboutYourselfForm, emptyForm, expectedErrors)
    }
  }

  "Applicant Tell Us About Yourself form (in Edit mode)" must {

    "not give an error for valid data" in {
      // Note that Applicant Details set the role to Lead Executor by default, hence setting it to None for comparison here
      val data = applicantTellUsAboutYourselfEdit("0123456789")
      applicantTellUsAboutYourselfEditForm.bind(data).get mustBe ApplicantDetails(phoneNo = Some("0123456789"), role = None)
    }

    "give an error when a blank phone number is supplied" in {
      val data = applicantTellUsAboutYourselfEdit("")
      val expectedErrors = error("phoneNo", "error.phoneNumber.give")

      checkForError(applicantTellUsAboutYourselfEditForm, data, expectedErrors)
    }

    "give an error when an invalid phone number is supplied" in {
      val data = applicantTellUsAboutYourselfEdit("Invalid value!")
      val expectedErrors = error("phoneNo", "error.phoneNumber.giveUsingOnlyLettersAndNumbers")

      checkForError(applicantTellUsAboutYourselfEditForm, data, expectedErrors)
    }

    "give an error when the phone number is too long" in {
      val data = applicantTellUsAboutYourselfEdit("A string longer than 27 chrs")
      val expectedErrors = error("phoneNo", "error.phoneNumber.giveUsing27CharactersOrLess")

      checkForError(applicantTellUsAboutYourselfEditForm, data, expectedErrors)
    }

    "give an error when no phone number is supplied" in {
      val expectedErrors = error("phoneNo", "error.phoneNumber.give")

      checkForError(applicantTellUsAboutYourselfEditForm, emptyForm, expectedErrors)
    }

    "ignore the 'Lives in UK' field if supplied" in {
      // This is a test that over-posting does not occur.  The data we supply contains the isAddressInUk field,
      // and we test that the outcome has that field set to None - i.e. the supplied value isn't used.
      val data = applicantTellUsAboutYourself("0123456789", "true")
      applicantTellUsAboutYourselfEditForm.bind(data).get mustBe
        ApplicantDetails(phoneNo = Some("0123456789"), role = None, doesLiveInUK = None)
    }
  }

  //endregion

  "Deceased Address Details (in the UK) form" must {

    "not give an error for valid data" in {
      applicantAddressUkForm.bind(completeUkAddress).get mustBe
        UkAddress("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), "AA111AA", "GB")
    }

    "not give an error when lines 2 and 3 are omitted" in {
      val data = completeUkAddress - "ukAddressLine3" - "ukAddressLine4"

      applicantAddressUkForm.bind(data).get mustBe UkAddress("Line 1", "Line 2", None, None, "AA111AA", "GB")
    }

    "give an error when line 1 is blank" in {
      val data = completeUkAddress + ("ukAddressLine1" -> "")
      val expectedErrors = error("ukAddressLine1", "error.address.giveInLine1And2")

      checkForError(applicantAddressUkForm, data, expectedErrors)
    }

    "give an error when line 1 is omitted" in {
      val data = completeUkAddress - "ukAddressLine1"
      val expectedErrors = error("ukAddressLine1", "error.address.giveInLine1And2")

      checkForError(applicantAddressUkForm, data, expectedErrors)
    }

    "give an error when line 1 is too long" in {
      val data = completeUkAddress + ("ukAddressLine1" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine1", "error.address.giveUsing35CharsOrLess")

      checkForError(applicantAddressUkForm, data, expectedErrors)
    }

    "give an error when line 2 is blank" in {
      val data = completeUkAddress + ("ukAddressLine2" -> "")
      val expectedErrors = error("ukAddressLine2", "error.address.giveInLine1And2")

      checkForError(applicantAddressUkForm, data, expectedErrors)
    }

    "give an error when line 2 is omitted" in {
      val data = completeUkAddress - "ukAddressLine2"
      val expectedErrors = error("ukAddressLine2", "error.address.giveInLine1And2") ++
        error("ukAddressLine2", "error.required")

      checkForError(applicantAddressUkForm, data, expectedErrors)
    }

    "give an error when line 2 is too long" in {
      val data = completeUkAddress + ("ukAddressLine2" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine2", "error.address.giveUsing35CharsOrLess")

      checkForError(applicantAddressUkForm, data, expectedErrors)
    }

    "give an error when line 3 is too long" in {
      val data = completeUkAddress + ("ukAddressLine3" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine3", "error.address.giveUsing35CharsOrLess")

      checkForError(applicantAddressUkForm, data, expectedErrors)
    }

    "give an error when line 4 is too long" in {
      val data = completeUkAddress + ("ukAddressLine4" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine4", "error.address.giveUsing35CharsOrLess")

      checkForError(applicantAddressUkForm, data, expectedErrors)
    }

    "give an error when the postcode is blank" in {
      val data = completeUkAddress + ("postCode" -> "")
      val expectedErrors = error("postCode", "error.address.givePostcode")

      checkForError(applicantAddressUkForm, data, expectedErrors)
    }

    "give an error when the postcode is omitted" in {
      val data = completeUkAddress - "postCode"
      val expectedErrors = error("postCode", "error.address.givePostcode") ++
        error("postCode", "error.required")

      checkForError(applicantAddressUkForm, data, expectedErrors)
    }

    "give an error when the postcode is too long" in {
      val data = completeUkAddress + ("postCode" -> "AA11 11AAA")
      val expectedErrors = error("postCode", "error.address.givePostcodeUsingNumbersAndLetters")

      checkForError(applicantAddressUkForm, data, expectedErrors)
    }

    "give an error when the postcode is invalid" in {
      val data = completeUkAddress + ("postCode" -> "INVALID")
      val expectedErrors = error("postCode", "error.address.givePostcodeUsingNumbersAndLetters")

      checkForError(applicantAddressUkForm, data, expectedErrors)
    }

    "give multiple errors when no data is supplied" in {
      val expectedErrors = error("ukAddressLine1", "error.address.give") ++
        error("ukAddressLine2", "") ++
        error("postCode", "error.address.givePostcode") ++
        error("ukAddressLine2", "error.required") ++
        error("postCode", "error.required")

      checkForError(applicantAddressUkForm, emptyForm, expectedErrors)
    }
  }

  //endregion

  //region Deceased Address (abroad) tests

  "Deceased Address Details (abroad) form" must {

    "not give an error for valid data" in {
      applicantAddressAbroadForm.bind(completeAddressAbroad).get mustBe
        UkAddress("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), "", "AU")
    }

    "not give an error when lines 2 and 3 are omitted" in {
      val data = completeAddressAbroad - "ukAddressLine3" - "ukAddressLine4"

      applicantAddressAbroadForm.bind(data).get mustBe UkAddress("Line 1", "Line 2", None, None, "", "AU")
    }

    "give an error when line 1 is blank" in {
      val data = completeAddressAbroad + ("ukAddressLine1" -> "")
      val expectedErrors = error("ukAddressLine1", "error.address.giveInLine1And2")

      checkForError(applicantAddressAbroadForm, data, expectedErrors)
    }

    "give an error when line 1 is omitted" in {
      val data = completeAddressAbroad - "ukAddressLine1"
      val expectedErrors = error("ukAddressLine1", "error.address.giveInLine1And2")

      checkForError(applicantAddressAbroadForm, data, expectedErrors)
    }

    "give an error when line 1 is too long" in {
      val data = completeAddressAbroad + ("ukAddressLine1" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine1", "error.address.giveUsing35CharsOrLess")

      checkForError(applicantAddressAbroadForm, data, expectedErrors)
    }

    "give an error when line 2 is blank" in {
      val data = completeAddressAbroad + ("ukAddressLine2" -> "")
      val expectedErrors = error("ukAddressLine2", "error.address.giveInLine1And2")

      checkForError(applicantAddressAbroadForm, data, expectedErrors)
    }

    "give an error when line 2 is omitted" in {
      val data = completeAddressAbroad - "ukAddressLine2"
      val expectedErrors = error("ukAddressLine2", "error.address.giveInLine1And2") ++
        error("ukAddressLine2", "error.required")

      checkForError(applicantAddressAbroadForm, data, expectedErrors)
    }

    "give an error when line 2 is too long" in {
      val data = completeAddressAbroad + ("ukAddressLine2" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine2", "error.address.giveUsing35CharsOrLess")

      checkForError(applicantAddressAbroadForm, data, expectedErrors)
    }

    "give an error when line 3 is too long" in {
      val data = completeAddressAbroad + ("ukAddressLine3" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine3", "error.address.giveUsing35CharsOrLess")

      checkForError(applicantAddressAbroadForm, data, expectedErrors)
    }

    "give an error when line 4 is too long" in {
      val data = completeAddressAbroad + ("ukAddressLine4" -> valueLongerThan36Chars)
      val expectedErrors = error("ukAddressLine4", "error.address.giveUsing35CharsOrLess")

      checkForError(applicantAddressAbroadForm, data, expectedErrors)
    }

    "give an error when the country code is blank" in {
      val data = completeAddressAbroad + ("countryCode" -> "")
      val expectedErrors = error("countryCode", "error.country.select")

      checkForError(applicantAddressAbroadForm, data, expectedErrors)
    }

    "give an error when the country code is omitted" in {
      val data = completeAddressAbroad - "countryCode"
      val expectedErrors = error("countryCode", "error.country.select")

      checkForError(applicantAddressAbroadForm, data, expectedErrors)
    }

    "give an error when the country code is invalid" in {
      val data = completeAddressAbroad + ("countryCode" -> "XY")
      val expectedErrors = error("countryCode", "error.country.select")

      checkForError(applicantAddressAbroadForm, data, expectedErrors)
    }

    "give multiple errors when no data is supplied" in {
      val expectedErrors = error("ukAddressLine1", "error.address.give") ++
        error("ukAddressLine2", "") ++
        error("countryCode", "error.country.select") ++
        error("ukAddressLine2", "error.required")


      checkForError(applicantAddressAbroadForm, emptyForm, expectedErrors)
    }
  }

  //endregion
}
