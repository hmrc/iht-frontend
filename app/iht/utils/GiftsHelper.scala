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

import iht.config.AppConfig
import iht.constants.Constants.MaxIterationValueForGiftYears
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.PreviousYearsGifts
import iht.utils.CommonHelper._
import org.joda.time.LocalDate
import play.api.i18n.Messages

object GiftsHelper {
  def createPreviousYearsGiftsLists(dateOfDeath: LocalDate)(implicit appConfig: AppConfig): Seq[PreviousYearsGifts] = {
    def previousYearsGifts(dateOfDeath: LocalDate): Seq[PreviousYearsGifts] = {
      val startingDate = new LocalDate(dateOfDeath.getYear,
        appConfig.giftsStartMonth,
        appConfig.giftsStartDay)

      val giftYears = appConfig.giftsYears
      val noOfYearsToCalculate = if(startingDate.minusDays(1).isEqual(dateOfDeath)) { giftYears } else {giftYears + 1}
      val sevenYearsPriorDate = dateOfDeath.minusYears(giftYears).plusDays(1)

      val periodDate = if (startingDate.isAfter(dateOfDeath) || startingDate.isEqual(dateOfDeath)) {
        startingDate.minusYears(1)
      } else {
        startingDate
      }

      val endDate: LocalDate = periodDate.minusDays(1)
      val startDateString: String = periodDate.monthOfYear().get() + "-" + periodDate.getDayOfMonth
      val endDateString: String = endDate.monthOfYear().get() + "-" + endDate.getDayOfMonth

      (1 to noOfYearsToCalculate) map { x =>
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

  private def getStartDateString(noOfIteration: Int,
                                 dateOfDeath: LocalDate,
                                 sevenYearsPriorDate: LocalDate,
                                 startingDate: LocalDate,
                                 giftYears: Int,
                                 periodDate: LocalDate,
                                 startDateString: String): String = {


    noOfIteration match {
      case `MaxIterationValueForGiftYears` =>
        sevenYearsPriorDate.getYear + "-" + sevenYearsPriorDate.monthOfYear().get() + "-" + sevenYearsPriorDate.getDayOfMonth
      case _ => s"${periodDate.getYear - (noOfIteration - 1)}${"-" + startDateString}"
    }
  }

  private def getEndDateString(noOfIteration: Int,
                               dateOfDeath: LocalDate,
                               periodDate: LocalDate,
                               endDateString: String): String = {
    noOfIteration match {
      case 1 => s"${dateOfDeath.getYear}${"-" + dateOfDeath.monthOfYear.get()}${"-" + dateOfDeath.getDayOfMonth}"
      case _ => s"${periodDate.getYear - (noOfIteration - 2)}${"-" + endDateString}"
    }
  }

  def correctGiftDateFormats(ad:ApplicationDetails)(implicit appConfig: AppConfig): ApplicationDetails = {
    val optionSeqPreviousYearsGifts: Option[Seq[PreviousYearsGifts]] = ad.giftsList.map{ seqPreviousYearsGifts: Seq[PreviousYearsGifts] =>
      seqPreviousYearsGifts.map{ previousYearsGifts: PreviousYearsGifts =>
        val fixture = StringHelperFixture()
        val optionStartDate: Option[String] = previousYearsGifts.startDate.map{ startDate: String =>
          fixture.parseOldAndNewDatesFormats(startDate)
        }
        val optionEndDate: Option[String] = previousYearsGifts.endDate.map{ endDate: String =>
          fixture.parseOldAndNewDatesFormats(endDate)
        }
        previousYearsGifts copy (
          startDate = optionStartDate , endDate = optionEndDate
        )
      }
    }
    ad copy ( giftsList = optionSeqPreviousYearsGifts)
  }

  def previousYearsGiftsAccessibility(element: PreviousYearsGifts)(implicit messages: Messages): String = {
    val messageFileSectionKey = "page.iht.application.gifts.sevenYears.values.valueOfGiftsAndExemptions.link.screenReader"
    val startDate = getOrException(element.startDate)
    val endDate = getOrException(element.endDate)
    val totalGifts = element.value.fold(BigDecimal(0))(identity)
    val totalExemptions = element.exemptions.fold(BigDecimal(0))(identity)
    val amountAddedToEstate = totalGifts - totalExemptions

    mapBigDecimalPair(element.value, element.exemptions,
      messages(s"$messageFileSectionKey.change", startDate, endDate, totalGifts, totalExemptions, amountAddedToEstate),
      messages(s"$messageFileSectionKey.change", startDate, endDate, totalGifts, totalExemptions, amountAddedToEstate),
      messages(s"$messageFileSectionKey.change", startDate, endDate, totalGifts, totalExemptions, amountAddedToEstate),
      messages(s"$messageFileSectionKey.change", startDate, endDate, totalGifts, totalExemptions, amountAddedToEstate))
  }

  def previousYearsGiftsAccessibilityTotals(totalPastYearsGifts: BigDecimal,
                                            totalExemptionsValue: BigDecimal,
                                            totalPastYearsGiftsValueExcludingExemptions: BigDecimal,
                                            elements: Seq[PreviousYearsGifts])(implicit messages: Messages): Option[(String, String, String)] = {
    val messageFileSectionKey = "page.iht.application.gifts.sevenYears.values.valueOfGiftsAndExemptions.total"
    val sortedGifts = elements.sortWith((a, b) => getOrException(a.startDate) < getOrException(b.startDate))
    val earliestDate = getOrException(sortedGifts.head.startDate)
    val latestDate = getOrException(sortedGifts.reverse.head.endDate)

    Some(
      (messages(s"$messageFileSectionKey.gifts.screenReader", earliestDate, latestDate, totalPastYearsGifts),
        messages(s"$messageFileSectionKey.exemptions.screenReader", earliestDate, latestDate, totalExemptionsValue),
        messages(s"$messageFileSectionKey.estate.screenReader", earliestDate, latestDate, totalPastYearsGiftsValueExcludingExemptions))
    )
  }
}
