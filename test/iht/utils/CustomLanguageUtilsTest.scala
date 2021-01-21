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

package iht.utils

import java.util.Date

import iht.utils.CustomLanguageUtils.Dates
import iht.views.ViewTestHelper
import org.joda.time.LocalDate

class CustomLanguageUtilsTest extends ViewTestHelper {
  val epoch = LocalDate.fromDateFields(Date.from(java.time.Instant.EPOCH))
  "CustomLanguageUtils.Dates#to" must {
    "pass 'language.to' to an instance of Messages" in {
      Dates.to mustBe messages("language.to")
    }
  }

  "CustomLanguageUtils.Dates#singular" must {
    "pass 'language.day.singular' to an instance of Messages" in {
      Dates.singular mustBe messages("language.day.singular")
    }
  }

  "CustomLanguageUtils.Dates#plural" must {
    "pass 'language.day.plural' to an instance of Messages" in {
      Dates.plural mustBe messages("language.day.plural")
    }
  }

  "CustomLanguageUtils.Dates#formatDate" must {
    "convert an optional LocalDate object to a date string (D MMMM Y)" in {
      Dates.formatDate(Some(epoch), "not-valid") mustBe "1 January 1970"
    }
  }

  "CustomLanguageUtils.Dates#formatDateAbbrMonth" must {
    "convert a LocalDate object to a date string (D MMM Y)" in {
      Dates.formatDateAbbrMonth(epoch) mustBe "1 Jan 1970"
    }
  }

  /*
  Jenkins uses a different time-zone than local machines, causing build to fail.

  "CustomLanguageUtils.Dates#formatEasyReadingTimestamp" must {
    "convert an optional LocalDate object to a date string (h:mmaa, EEEE d MMMM yyyy)" in {
      val epochDateTime = DateTime.parse("1970-01-01T00:00").withZone(DateTimeZone.UTC)
      Dates.formatEasyReadingTimestamp(Some(epochDateTime), "not-valid") mustBe "12:00am, Thursday 1 January 1970"
    }
  }
  */

  "CustomLanguageUtils.Dates#shortDateFormat" must {
    "convert a LocalDate object to a date string (yyyy-MM-dd)" in {
      Dates.shortDate(epoch) mustBe "1970-01-01"
    }
  }

  "CustomLanguageUtils.Dates#formatDateRange" must {
    "convert two LocalDate objects to a date string representing the range of time between them" in {
      val dayAfterEpoch = epoch.plusDays(1)
      Dates.formatDateRange(epoch, dayAfterEpoch) mustBe "1 January 1970 to 2 January 1970"
    }
  }

  "CustomLanguageUtils.Dates#formatDays" must {
    "convert an int to a string appended by 'days'" in {
      Dates.formatDays(3) mustBe "3 days"
      Dates.formatDays(1) mustBe "1 day"

    }
  }

  }
