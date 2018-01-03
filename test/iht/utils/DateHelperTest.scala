/*
 * Copyright 2018 HM Revenue & Customs
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
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.test.UnitSpec

class DateHelperTest extends UnitSpec with FakeIhtApp with MockitoSugar {

  "createDate should return None" in {
    val result = DateHelper.createDate(Some(""), Some("01"), Some("10"))
    result should be(None)
  }

  "verify the input date is within range" in {
    val date = LocalDate.now.plusMonths(12)
    assert(DateHelper.isDateWithInRange(date) == true, "Given date must be with in next 24 months from last " +
      "day of the month of the given date")
  }

  "verify the input date is not within range" in {
    val date = LocalDate.now.minusMonths(27)
    assert(DateHelper.isDateWithInRange(date) == false, "Given date must be out of next 24 months from last" +
      "day of the month of the given date")
  }
 }
