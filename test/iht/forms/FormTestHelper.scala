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

package iht.forms

import iht.models.UkAddress
import iht.testhelpers.CommonBuilder
import iht.{FakeIhtApp, TestUtils}
import org.scalatest.mock.MockitoSugar
import play.api.data.{Form, FormError}
import play.api.i18n.MessagesApi
import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

import scala.collection.immutable.ListMap

trait FormTestHelper extends FakeIhtApp with MockitoSugar with TestUtils {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val request = FakeRequest()
  val messages = messagesApi.preferred(request)

  def addressMap(line1: String, line2: String, line3: String, line4: String, postCode: String, countryCode: String) =
    Map("address.ukAddressLine1" -> line1,
      "address.ukAddressLine2" -> line2,
      "address.ukAddressLine3" -> line3,
      "address.ukAddressLine4" -> line4,
      "address.postCode" -> postCode,
      "address.countryCode" -> countryCode)

  lazy val fullUkAddress = addressMap("Line 1", "Line 2", "Line 3", "Line 4", "AA111AA", "GB") - "countryCode"

  def checkForError(form: Form[_], data: Map[String, String], expectedErrors: Seq[FormError]): Unit = {
    form.bind(data).fold(
      formWithErrors => {
        formWithErrors.errors mustBe expectedErrors
      },
      form => {
        fail("Expected a validation error when binding the form, but it was bound successfully.")
      }
    )
  }

  def checkForContainsError(form: Form[_], data: Map[String, String], expectedError: FormError) = {
    form.bind(data).fold(
      formWithErrors => {
        assert(formWithErrors.errors.contains(expectedError))
      },
      form => {
        fail("Expected a validation error when binding the form, but it was bound successfully.")
      }
    )
  }

  def checkForError(form: Form[_], data: JsValue, expectedErrors: Seq[FormError]) = {
    form.bind(data).fold(
      formWithErrors => {
        formWithErrors.errors mustBe expectedErrors
      },
      form => {
        fail("Expected a validation error when binding the form, but it was bound successfully.")
      }
    )
  }

  def checkForNotContainsError[A](form: Form[A], fieldName: String,  data: Map[String, String]) = {
    form.bind(data).fold(
      formWithErrors => {
        assert(!formWithErrors.errors.exists(_.key == fieldName))
      },
      form => {
        assert(true)
      }
    )
  }

  def formWithNoError(form: Form[_], data: JsValue) = {
    form.bind(data).fold(
      formWithErrors => {
        fail("Form has errors")
      },
      form => form
    )
  }


  def formWithNoError[A](form: Form[A], data: Map[String, String]):A = {
    form.bind(data).fold(
      formWithErrors => {
        fail("Form has errors")
      },
      form => form
    )
  }

  def error(key: String, value: String) = Seq(singleError(key, value))

  def singleError(key: String, value: String) = FormError(key, value)

  lazy val valueLongerThan36Chars = "A sentence that is 37 characters long"

  lazy val valueInvalidChars = "<<<<"

  lazy val emptyForm = Map[String, String]()

  def formData(fieldName: String, value: String) = Map(fieldName -> value)

  def formData(fieldName1: String, value1: String, fieldName2: String, value2: String) =
    Map(fieldName1 -> value1, fieldName2 -> value2)

  def emptyData = Map[String, String]()


  def yesNoQuestion[A](fieldName: String,
                       form: Form[A],
                       retrieveValueFromModel: A => Option[Boolean],
                       formErrorMessageKey: String) = {
    "not give an error when answered Yes" in {
      val data = formData(fieldName, "true")
      retrieveValueFromModel(form.bind(data).get) mustBe Some(true)
    }

    "not give an error when answered No" in {
      val data = formData(fieldName, "false")
      retrieveValueFromModel(form.bind(data).get) mustBe Some(false)
    }

    "give an error when the question is not answered" in {
      val expectedErrors = error(fieldName, formErrorMessageKey)
      checkForError(form, emptyForm, expectedErrors)
    }
  }

  def yesNoQuestionAndValue[A](questionFieldName: String,
                               valueFieldName: String,
                               form: Form[A],
                               retrieveQuestionValueFromModel: A => Option[Boolean],
                               retrieveValueValueFromModel: A => Option[BigDecimal],
                               formErrorMessageKeySelect: String,
                               formErrorMessageKeyEnterValue: String
                              ) = {
    "not give an error when answered Yes and a value given" in {
      val data = formData(questionFieldName, "true", valueFieldName, "666")
      retrieveQuestionValueFromModel(form.bind(data).get) mustBe Some(true)
      retrieveValueValueFromModel(form.bind(data).get) mustBe Some(BigDecimal(666))
    }

    "give an error when answered Yes and no value is given" in {
      val data = formData(questionFieldName, "true")
      val expectedErrors = error(valueFieldName, formErrorMessageKeyEnterValue)
      checkForError(form, data, expectedErrors)
    }

    "not give an error when answered No" in {
      val data = formData(questionFieldName, "false")
      retrieveQuestionValueFromModel(form.bind(data).get) mustBe Some(false)
    }

    "give an error when the question is not answered" in {
      val expectedErrors = error(questionFieldName, formErrorMessageKeySelect)
      checkForError(form, emptyForm, expectedErrors)
    }
  }

  def multipleChoiceQuestion[A](fieldName: String,
                       form: Form[A],
                       retrieveValueFromModel: A => Option[String],
                       formErrorMessageKey: String,
                       items: => ListMap[String, _]) = {

    "not give an error when the question is answered " in {
      val data = formData(fieldName, items.head._1)
      retrieveValueFromModel(form.bind(data).get) mustBe Some(items.head._1)
    }

    "give an error when the question is not answered" in {
      val expectedErrors = error(fieldName, formErrorMessageKey)
      checkForError(form, emptyForm, expectedErrors)
    }
  }

  def currencyValue[A](fieldName: String,
                       form: Form[A]
                      ) = {

    "give an error when the length is exceeded" in {
      val data = formData(fieldName, "9" * 11)
      val expectedErrors = error(fieldName, "error.estateReport.value.giveLessThanEleven")

      checkForContainsError(form, data, expectedErrors.head)
    }

    "give the correct error when value field has invalid characters" in {
      val data = formData(fieldName, "abc")
      val expectedErrors = error(fieldName, "error.estateReport.value.giveValueUsingNumbers")

      checkForContainsError(form, data, expectedErrors.head)
    }

    "give the correct error when value field has special characters" in {
      val data = formData(fieldName, "12356%&£")
      val expectedErrors = error(fieldName, "error.estateReport.value.giveValueUsingNumbers")

      checkForContainsError(form, data, expectedErrors.head)
    }

    "give the correct error when value field has more than 2 numbers after decimal" in {
      val data = formData(fieldName, "1235.345")
      val expectedErrors = error(fieldName, "error.estateReport.value.giveCorrectNumberOfPence")

      checkForContainsError(form, data, expectedErrors.head)
    }

    "give the correct error when value field has more than one decimal" in {
      val data = formData(fieldName, "123.45.5")
      val expectedErrors = error(fieldName, "error.estateReport.value.giveCorrectNumberOfPence")

      checkForContainsError(form, data, expectedErrors.head)
    }

    "give the correct error when value field has space between numbers" in {
      val data = formData(fieldName, "12 1 3  4")
      val expectedErrors = error(fieldName, "error.estateReport.value.giveWithNoSpaces")

      checkForContainsError(form, data, expectedErrors.head)
    }

    "not give the error when value field has value with valid length" in {
      checkForNotContainsError(form, "value", Map("value" ->"123445"))
    }

    "not give the error when value field has valid value with one decimal" in {
      checkForNotContainsError(form, "value", Map("value" ->"0.11"))
    }

    "give no error if the value is valid and with 2 decimal points" in {
      checkForNotContainsError(form, "value", Map("value" ->"1.99"))
    }
  }

  def mandatoryCurrencyValue[A](fieldName: String,
                       form: Form[A]
                      ) = {

    currencyValue(fieldName, form)

    "give the correct error when value field is empty" in {
      val data = formData(fieldName, "")
      val expectedErrors = error(fieldName, "error.estateReport.value.give")
      checkForContainsError(form, data, expectedErrors.head)
    }
  }

  def dateOfBirth[A](completeMapOfFieldValues: => Map[String, String], form: => Form[A]) = {
    "give an error when the day is blank" in {
      val data = completeMapOfFieldValues + ("dateOfBirth.day" -> "")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveFull")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the day is not supplied" in {
      val data = completeMapOfFieldValues - "dateOfBirth.day"
      val expectedErrors = error("dateOfBirth.day", "error.required")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the day is invalid" in {
      val data = completeMapOfFieldValues + ("dateOfBirth.day" -> "INVALID")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectDateUsingOnlyNumbers")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the day is too high" in {
      val data = completeMapOfFieldValues + ("dateOfBirth.day" -> "32")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectDay")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the month is blank" in {
      val data = completeMapOfFieldValues + ("dateOfBirth.month" -> "")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveFull")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the month is not supplied" in {
      val data = completeMapOfFieldValues - "dateOfBirth.month"
      val expectedErrors = error("dateOfBirth.month", "error.required")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the month is invalid" in {
      val data = completeMapOfFieldValues + ("dateOfBirth.month" -> "INVALID")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectDateUsingOnlyNumbers")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the month is too high" in {
      val data = completeMapOfFieldValues + ("dateOfBirth.month" -> "13")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectMonth")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the year is blank" in {
      val data = completeMapOfFieldValues + ("dateOfBirth.year" -> "")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveFull")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the year is not supplied" in {
      val data = completeMapOfFieldValues - "dateOfBirth.year"
      val expectedErrors = error("dateOfBirth.year", "error.required")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the year is invalid" in {
      val data = completeMapOfFieldValues + ("dateOfBirth.year" -> "INVALID")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectDateUsingOnlyNumbers")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the year is supplied as a two-digit number" in {
      val data = completeMapOfFieldValues + ("dateOfBirth.year" -> "14")
      val expectedErrors = error("dateOfBirth", "error.dateOfBirth.giveCorrectYear")

      checkForError(form, data, expectedErrors)
    }
  }

  def dateOfDeath[A](completeMapOfFieldValues: => Map[String, String], form: => Form[A], fieldName: String = "dateOfDeath") = {
    "give an error when the day is blank" in {
      val data = completeMapOfFieldValues + (s"$fieldName.day" -> "")
      val expectedErrors = error(fieldName, "error.dateOfDeath.giveFull")
      checkForError(form, data, expectedErrors)
    }

    "give an error when the day is not supplied" in {
      val data = completeMapOfFieldValues - s"$fieldName.day"
      val expectedErrors = error(s"$fieldName.day", "error.required")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the day is invalid" in {
      val data = completeMapOfFieldValues + (s"$fieldName.day" -> "INVALID")
      val expectedErrors = error(fieldName, "error.dateOfDeath.giveCorrectDateUsingOnlyNumbers")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the day is too high" in {
      val data = completeMapOfFieldValues + (s"$fieldName.day" -> "32")
      val expectedErrors = error(fieldName, "error.dateOfDeath.giveCorrectDay")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the month is blank" in {
      val data = completeMapOfFieldValues + (s"$fieldName.month" -> "")
      val expectedErrors = error(fieldName, "error.dateOfDeath.giveFull")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the month is not supplied" in {
      val data = completeMapOfFieldValues - s"$fieldName.month"
      val expectedErrors = error(s"$fieldName.month", "error.required")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the month is invalid" in {
      val data = completeMapOfFieldValues + (s"$fieldName.month" -> "INVALID")
      val expectedErrors = error(fieldName, "error.dateOfDeath.giveCorrectDateUsingOnlyNumbers")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the month is too high" in {
      val data = completeMapOfFieldValues + (s"$fieldName.month" -> "13")
      val expectedErrors = error(fieldName, "error.dateOfDeath.giveCorrectMonth")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the year is blank" in {
      val data = completeMapOfFieldValues + (s"$fieldName.year" -> "")
      val expectedErrors = error(fieldName, "error.dateOfDeath.giveFull")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the year is not supplied" in {
      val data = completeMapOfFieldValues - s"$fieldName.year"
      val expectedErrors = error(s"$fieldName.year", "error.required")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the year is invalid" in {
      val data = completeMapOfFieldValues + (s"$fieldName.year" -> "INVALID")
      val expectedErrors = error(fieldName, "error.dateOfDeath.giveCorrectDateUsingOnlyNumbers")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the year is supplied as a two-digit number" in {
      val data = completeMapOfFieldValues + (s"$fieldName.year" -> "14")
      val expectedErrors = error(fieldName, "error.dateOfDeath.giveCorrectYear")

      checkForError(form, data, expectedErrors)
    }
  }

  def dateOfMarriage[A](completeMapOfFieldValues: => Map[String, String], form: => Form[A]) = {
    "give an error when the day is blank" in {
      val data = completeMapOfFieldValues + ("dateOfMarriage.day" -> "")
      val expectedErrors = error("dateOfMarriage", "error.dateOfMarriage.giveFull")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the day is not supplied" in {
      val data = completeMapOfFieldValues - "dateOfMarriage.day"
      val expectedErrors = error("dateOfMarriage.day", "error.required")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the day is invalid" in {
      val data = completeMapOfFieldValues + ("dateOfMarriage.day" -> "INVALID")
      val expectedErrors = error("dateOfMarriage", "error.dateOfMarriage.giveCorrectDateUsingOnlyNumbers")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the day is too high" in {
      val data = completeMapOfFieldValues + ("dateOfMarriage.day" -> "32")
      val expectedErrors = error("dateOfMarriage", "error.dateOfMarriage.giveCorrectDay")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the month is blank" in {
      val data = completeMapOfFieldValues + ("dateOfMarriage.month" -> "")
      val expectedErrors = error("dateOfMarriage", "error.dateOfMarriage.giveFull")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the month is not supplied" in {
      val data = completeMapOfFieldValues - "dateOfMarriage.month"
      val expectedErrors = error("dateOfMarriage.month", "error.required")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the month is invalid" in {
      val data = completeMapOfFieldValues + ("dateOfMarriage.month" -> "INVALID")
      val expectedErrors = error("dateOfMarriage", "error.dateOfMarriage.giveCorrectDateUsingOnlyNumbers")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the month is too high" in {
      val data = completeMapOfFieldValues + ("dateOfMarriage.month" -> "13")
      val expectedErrors = error("dateOfMarriage", "error.dateOfMarriage.giveCorrectMonth")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the year is blank" in {
      val data = completeMapOfFieldValues + ("dateOfMarriage.year" -> "")
      val expectedErrors = error("dateOfMarriage", "error.dateOfMarriage.giveFull")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the year is not supplied" in {
      val data = completeMapOfFieldValues - "dateOfMarriage.year"
      val expectedErrors = error("dateOfMarriage.year", "error.required")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the year is invalid" in {
      val data = completeMapOfFieldValues + ("dateOfMarriage.year" -> "INVALID")
      val expectedErrors = error("dateOfMarriage", "error.dateOfMarriage.giveCorrectDateUsingOnlyNumbers")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the year is supplied as a two-digit number" in {
      val data = completeMapOfFieldValues + ("dateOfMarriage.year" -> "14")
      val expectedErrors = error("dateOfMarriage", "error.dateOfMarriage.giveCorrectYear")

      checkForError(form, data, expectedErrors)
    }
  }  

  def nino[A](completeMapOfFieldValues: => Map[String, String], form: => Form[A]) = {
    "give an error when the NINO is blank" in {
      val data = completeMapOfFieldValues + ("nino" -> "")
      val expectedErrors = error("nino", "error.nino.give")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the NINO is not supplied" in {
      val data = completeMapOfFieldValues - "nino"
      val expectedErrors = error("nino", "error.nino.give")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the NINO is too long" in {
      val nino = CommonBuilder.DefaultNino
      val data = completeMapOfFieldValues + ("nino" -> (nino.substring(0, nino.length() - 1) + "AA"))
      val expectedErrors = error("nino", "error.nino.giveUsing8Or9Characters")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the NINO is invalid" in {
      val data = completeMapOfFieldValues + ("nino" -> "INVALIDD")
      val expectedErrors = error("nino", "error.nino.giveUsingOnlyLettersAndNumbers")

      checkForError(form, data, expectedErrors)
    }
  }


  def ukAddress[A](fieldName: String,
                   form: Form[A],
                   retrieveValueFromModel: A => Option[UkAddress]) = {
    "not give an error for valid address" in {
      formWithNoError(form, fullUkAddress)
    }

    "not give an error when lines 2 and 3 are omitted" in {
      val data = fullUkAddress - "address.ukAddressLine3" - "address.ukAddressLine4"
      formWithNoError(form, data)
    }

    "give an error when line 1 is blank" in {
      val data = fullUkAddress + ("address.ukAddressLine1" -> "")
      val expectedErrors = error("address.ukAddressLine1", "error.address.giveInLine1And2")
      checkForError(form, data, expectedErrors)
    }

    "give an error when line 1 is too long" in {
      val data = fullUkAddress + ("address.ukAddressLine1" -> valueLongerThan36Chars)
      val expectedErrors = error("address.ukAddressLine1", "error.address.giveUsing35CharsOrLess")
      checkForError(form, data, expectedErrors)
    }

    "give an error when line 2 is blank" in {
      val data = fullUkAddress + ("address.ukAddressLine2" -> "")
      val expectedErrors = error("address.ukAddressLine2", "error.address.giveInLine1And2")
      checkForError(form, data, expectedErrors)
    }

    "give an error when line 2 is omitted" in {
      val data = fullUkAddress - "address.ukAddressLine2"
      val expectedErrors = error("address.ukAddressLine2", "error.address.giveInLine1And2") ++
        error("address.ukAddressLine2", "error.required")
      checkForError(form, data, expectedErrors)
    }

    "give an error when line 2 is too long" in {
      val data = fullUkAddress + ("address.ukAddressLine2" -> valueLongerThan36Chars)
      val expectedErrors = error("address.ukAddressLine2", "error.address.giveUsing35CharsOrLess")

      checkForError(form, data, expectedErrors)
    }

    "give an error when line 3 is too long" in {
      val data = fullUkAddress + ("address.ukAddressLine3" -> valueLongerThan36Chars)
      val expectedErrors = error("address.ukAddressLine3", "error.address.giveUsing35CharsOrLess")

      checkForError(form, data, expectedErrors)
    }

    "give an error when line 4 is too long" in {
      val data = fullUkAddress + ("address.ukAddressLine4" -> valueLongerThan36Chars)
      val expectedErrors = error("address.ukAddressLine4", "error.address.giveUsing35CharsOrLess")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the postcode is blank" in {
      val data = fullUkAddress + ("address.postCode" -> "")
      val expectedErrors = error("address.postCode", "error.address.givePostcode")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the postcode is omitted" in {
      val data = fullUkAddress - "address.postCode"
      val expectedErrors = error("address.postCode", "error.address.givePostcode") ++
        error("address.postCode", "error.required")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the postcode is too long" in {
      val data = fullUkAddress + ("address.postCode" -> "AA11 11AAA")
      val expectedErrors = error("address.postCode", "error.address.givePostcodeUsingNumbersAndLetters")

      checkForError(form, data, expectedErrors)
    }

    "give an error when the postcode is invalid" in {
      val data = fullUkAddress + ("address.postCode" -> "INVALID")
      val expectedErrors = error("address.postCode", "error.address.givePostcodeUsingNumbersAndLetters")

      checkForError(form, data, expectedErrors)
    }

    "give multiple errors when no data is supplied" in {
      val expectedErrors = error("address.ukAddressLine1", "error.address.give") ++
        error("address.ukAddressLine2", "") ++
        error("address.postCode", "error.address.givePostcode") ++
        error("address.ukAddressLine2", "error.required") ++
        error("address.postCode", "error.required")

      checkForError(form, emptyForm, expectedErrors)
    }
  }

}
