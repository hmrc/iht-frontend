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


import iht.constants.IhtProperties
import org.joda.time.{DateTime, LocalDate}

object DateHelper {

  /**
    * Check the current date against input date plus range (thats add 24 months in the last day of the month
    * of input date)
    */
  def isDateWithInRange(date: LocalDate): Boolean = {
    val dateString = date.toString
    val dateTime = new DateTime(dateString)
    val dateRange = dateTime.dayOfMonth.withMaximumValue.plusMonths(IhtProperties.DateRangeMonths).toLocalDate
    LocalDate.now().compareTo(dateRange) < 0
  }

  def createDate(y: Option[String], m: Option[String], d: Option[String]): Option[LocalDate] = {
    val year: String = if (y.getOrElse("").replaceAll(" ", "").length > 4) {
      ""
    } else {
      y.getOrElse("")
    }

    try {
      Some(
        new LocalDate(
          year.replaceAll(" ", "").toInt,
          m.getOrElse("").replaceAll(" ", "").toInt,
          d.getOrElse("").replaceAll(" ", "").toInt
        )
      )
    } catch {
      case e: Exception => None
    }
  }
}
