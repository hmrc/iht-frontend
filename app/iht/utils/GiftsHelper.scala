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

import iht.constants.Constants.MaxIterationValueForGiftYears
import iht.constants.IhtProperties
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.PreviousYearsGifts
import org.joda.time.LocalDate

object GiftsHelper {
  def createPreviousYearsGiftsLists(dateOfDeath: LocalDate): Seq[PreviousYearsGifts] = {
    def previousYearsGifts(dateOfDeath: LocalDate): Seq[PreviousYearsGifts] = {
      val startingDate = new LocalDate(dateOfDeath.getYear,
        IhtProperties.giftsStartMonth,
        IhtProperties.giftsStartDay)

      val giftYears = IhtProperties.giftsYears
      val noOfYearsToCalculate = if(startingDate.minusDays(1).isEqual(dateOfDeath)) { giftYears } else {giftYears + 1}
      val sevenYearsPriorDate = dateOfDeath.minusYears(giftYears).plusDays(1)

      val periodDate = if (startingDate.isAfter(dateOfDeath) || startingDate.isEqual(dateOfDeath)) {
        startingDate.minusYears(1)
      } else {
        startingDate
      }

      val endDate: LocalDate = periodDate.minusDays(1)
      val startDateString: String = periodDate.monthOfYear().get() + "-" + periodDate.getDayOfMonth()
      val endDateString: String = endDate.monthOfYear().get() + "-" + endDate.getDayOfMonth

      {
        1 to noOfYearsToCalculate
      }.map { x =>

        val endDateStringForPage = getEndDateString(x, dateOfDeath, periodDate,endDateString)

        val startDateStringForPage = getStartDateString(x, dateOfDeath, sevenYearsPriorDate, startingDate,
          giftYears, periodDate, startDateString)

        PreviousYearsGifts(
          yearId = Some(((noOfYearsToCalculate-x) + 1).toString),
          value = None,
          exemptions = None,
          startDate = Some(startDateStringForPage),
          endDate = Some(endDateStringForPage))
      }
    }
    previousYearsGifts(dateOfDeath)
  }

  /**
    * Get the start date string
    */
  private def getStartDateString(noOfIteration: Int,
                                 dateOfDeath: LocalDate,
                                 sevenYearsPriorDate: LocalDate,
                                 startingDate: LocalDate,
                                 giftYears: Int,
                                 periodDate: LocalDate,
                                 startDateString: String): String = {


    noOfIteration match {
      case `MaxIterationValueForGiftYears` =>
        sevenYearsPriorDate.getYear + "-" + sevenYearsPriorDate.monthOfYear().get()+ "-" + sevenYearsPriorDate.getDayOfMonth
      case _ => s"${(periodDate.getYear - (noOfIteration - 1))}${"-" + startDateString}"
    }
  }

  /**
    * Get the end date string
    */
  private def getEndDateString(noOfIteration: Int,
                               dateOfDeath: LocalDate,
                               periodDate: LocalDate,
                               endDateString: String): String = {
    noOfIteration match {
      case 1 => s"${dateOfDeath.getYear}${"-" + dateOfDeath.monthOfYear.get()}${"-" + dateOfDeath.getDayOfMonth}"
      case _ => s"${periodDate.getYear - (noOfIteration - 2)}${"-" + endDateString}"
    }
  }

  def correctGiftDateFormats(ad:ApplicationDetails): ApplicationDetails = {
    val optionSeqPreviousYearsGifts: Option[Seq[PreviousYearsGifts]] = ad.giftsList.map{ (seqPreviousYearsGifts: Seq[PreviousYearsGifts]) =>
      seqPreviousYearsGifts.map{ (previousYearsGifts: PreviousYearsGifts) =>
        val optionStartDate: Option[String] = previousYearsGifts.startDate.map{ (startDate: String) =>
          StringHelper.parseOldAndNewDatesFormats(startDate)
        }
        val optionEndDate: Option[String] = previousYearsGifts.endDate.map{ (endDate: String) =>
          StringHelper.parseOldAndNewDatesFormats(endDate)
        }
        previousYearsGifts copy (
          startDate = optionStartDate , endDate = optionEndDate
        )
      }
    }
    ad copy ( giftsList = optionSeqPreviousYearsGifts)
  }
}
