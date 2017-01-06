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

package iht.forms.mappings

import iht.FakeIhtApp
import org.joda.time.LocalDate
import play.api.data.{FormError, Mapping}
import uk.gov.hmrc.play.test.UnitSpec

class DateMappingTest extends UnitSpec with FakeIhtApp {
  def assertError(mapping: Mapping[LocalDate], data: Map[String, String], errorMessage: String) = {
    mapping.bind(data).left.get shouldBe Seq(FormError("", errorMessage))
  }

  def createDateMapping(errorBlankFieldKey: String = "",
                        errorInvalidCharsKey: String = "",
                        errorInvalidDayKey: String = "",
                        errorInvalidDayForMonthKey: String = "",
                        errorInvalidMonthKey: String = "",
                        errorInvalidYearKey: String = "",
                        errorInvalidAllKey: String = "",
                        errorDateInFutureKey: String = "") = {
    DateMapping.apply(
      errorBlankFieldKey = errorBlankFieldKey,
      errorInvalidCharsKey = errorInvalidCharsKey,
      errorInvalidDayKey = errorInvalidDayKey,
      errorInvalidDayForMonthKey = errorInvalidDayForMonthKey,
      errorInvalidMonthKey = errorInvalidMonthKey,
      errorInvalidYearKey = errorInvalidYearKey,
      errorInvalidAllKey = errorInvalidAllKey,
      errorDateInFutureKey = errorDateInFutureKey
    )
  }

  "DateMapping generated from old apply constructor" must {
    "correctly validate a valid date tuple" in {
      DateMapping().bind(Map("day" -> "1", "month" -> "1", "year" -> "1980")).right.get shouldBe new LocalDate(1980, 1, 1)
    }

    "return a blank field error when one of the date tuple fields is blank" in {
      val blankFieldErrorMessage = "Blank field error message"
      assertError(DateMapping(errorBlankFieldKey = blankFieldErrorMessage),
        Map("day" -> "", "month" -> "1", "year" -> "2014"),
        blankFieldErrorMessage)
    }

    "return an invalid field error when date tuple contains non-numbers" in {
      val invalidFieldErrorMessage = "Invalid field error message"
      assertError(DateMapping(errorInvalidFieldKey = invalidFieldErrorMessage),
        Map("day" -> "1", "month" -> "1", "year" -> "blah"),
        invalidFieldErrorMessage)
    }

    "return an invalid date error when date tuple contains an invalid month number" in {
      val invalidDateErrorMessage = "Invalid date error message"
      assertError(DateMapping(errorInvalidDateKey = invalidDateErrorMessage),
        Map("day" -> "1", "month" -> "23", "year" -> "2014"),
        invalidDateErrorMessage)
    }

    "return an invalid date error when date tuple contains a two digit year" in {
      val invalidDateErrorMessage = "Invalid date error message"
      assertError(DateMapping(errorInvalidDateKey = invalidDateErrorMessage),
        Map("day" -> "1", "month" -> "2", "year" -> "14"),
        invalidDateErrorMessage)
    }

    "return a date in future error when date tuple represents date in the future" in {
      val dateInFutureErrorMessage = "Date in future error message"
      val date = LocalDate.now().plusDays(1)
      val data = Map("day" -> date.dayOfMonth().getAsText, "month" -> date.monthOfYear().get().toString, "year" -> date.year().getAsText())
      assertError(DateMapping(errorDateInFutureKey = dateInFutureErrorMessage), data, dateInFutureErrorMessage)
    }
  }

  "DateMapping generated from new apply constructor" must {
    "correctly validate a valid date tuple with month having no leading zero" in {
      createDateMapping()
        .bind(Map("day" -> "1", "month" -> "1", "year" -> "1980")).right.get shouldBe new LocalDate(1980, 1, 1)
    }

    "correctly validate a valid date tuple with month having leading zero" in {
      createDateMapping()
        .bind(Map("day" -> "1", "month" -> "01", "year" -> "1980")).right.get shouldBe new LocalDate(1980, 1, 1)
    }

    "correctly validate a valid date tuple with month where leapyear and 29th Feb" in {
      createDateMapping()
        .bind(Map("day" -> "29", "month" -> "2", "year" -> "2016")).right.get shouldBe new LocalDate(2016, 2, 29)
    }

    "return a blank field error when one of the date tuple fields is blank" in {
      val blankFieldErrorMessage = "Blank field error message"
      assertError(createDateMapping(errorBlankFieldKey = blankFieldErrorMessage),
        Map("day" -> "", "month" -> "1", "year" -> "2014"),
        blankFieldErrorMessage)
    }

    "return an invalid field error when date tuple contains non-numbers" in {
      val invalidCharsErrorMessage = "Invalid field error message"
      assertError(createDateMapping(errorInvalidCharsKey = invalidCharsErrorMessage),
        Map("day" -> "1", "month" -> "1", "year" -> "blah"),
        invalidCharsErrorMessage)
    }

    "return an invalid date error when date tuple contains an invalid day number" in {
      val invalidDayErrorMessage = "Invalid day error message"
      assertError(createDateMapping(errorInvalidDayKey = invalidDayErrorMessage),
        Map("day" -> "33", "month" -> "12", "year" -> "2014"),
        invalidDayErrorMessage)
    }

    "return an invalid date error when date tuple contains an invalid day number for the month" in {
      val invalidDayForMonthErrorMessage = "Invalid day for month error message"
      assertError(createDateMapping(errorInvalidDayForMonthKey = invalidDayForMonthErrorMessage),
        Map("day" -> "31", "month" -> "2", "year" -> "2014"),
        invalidDayForMonthErrorMessage)
    }

    "return an invalid date error when date tuple contains a year that is NOT a leapyear and month is 29th Feb" in {
      val invalidDayForMonthLeapYearErrorMessage = "Invalid day for month leapyear error message"
      assertError(createDateMapping(errorInvalidDayForMonthKey = invalidDayForMonthLeapYearErrorMessage),
        Map("day" -> "29", "month" -> "2", "year" -> "2017"),
        invalidDayForMonthLeapYearErrorMessage)
    }

    "return an invalid date error when date tuple contains an invalid month number" in {
      val invalidMonthErrorMessage = "Invalid month error message"
      assertError(createDateMapping(errorInvalidMonthKey = invalidMonthErrorMessage),
        Map("day" -> "1", "month" -> "13", "year" -> "2014"),
        invalidMonthErrorMessage)
    }

    "return an invalid date error when date tuple contains an invalid year number" in {
      val invalidYearErrorMessage = "Invalid year error message"
      assertError(createDateMapping(errorInvalidYearKey = invalidYearErrorMessage),
        Map("day" -> "1", "month" -> "3", "year" -> "14"),
        invalidYearErrorMessage)
    }

    "return an invalid date error when date tuple contains all numeric but all invalid components" in {
      val invalidAllErrorMessage = "Invalid all error message"
      assertError(createDateMapping(errorInvalidAllKey = invalidAllErrorMessage),
        Map("day" -> "33", "month" -> "23", "year" -> "323"),
        invalidAllErrorMessage)
    }

    "return a date in future error when date tuple represents date in the future" in {
      val dateInFutureErrorMessage = "Date in future error message"
      val date = LocalDate.now().plusDays(1)
      val data = Map("day" -> date.dayOfMonth().getAsText, "month" -> date.monthOfYear().get().toString, "year" -> date.year().getAsText())
      assertError(createDateMapping(errorDateInFutureKey = dateInFutureErrorMessage), data, dateInFutureErrorMessage)
    }
  }
}
