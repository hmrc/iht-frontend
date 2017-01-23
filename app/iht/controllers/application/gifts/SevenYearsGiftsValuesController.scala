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

package iht.controllers.application.gifts

import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.Constants._
import iht.constants.IhtProperties
import iht.controllers.application.EstateController
import iht.controllers.{ControllerHelper, IhtConnectors}
import iht.metrics.Metrics
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.{AllGifts, PreviousYearsGifts}
import iht.utils.CommonHelper
import org.joda.time.LocalDate
import iht.utils.CommonHelper._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

/**
  * Created by vineet on 11/04/16.
  */
object SevenYearsGiftsValuesController  extends SevenYearsGiftsValuesController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait SevenYearsGiftsValuesController extends EstateController {

  def cachingConnector : CachingConnector
  def ihtConnector : IhtConnector

  lazy val exceptionSaveTempApplication = "Unable to save temp application"
  lazy val exceptionNoGiftsList= "No gifts list found"

  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      cachingConnector.storeSingleValue(ControllerHelper.lastQuestionUrl,
        routes.SevenYearsGiftsValuesController.onPageLoad().toString())

      withApplicationDetails { rd => ad =>

        val giftsList = ad.giftsList.fold(createPreviousYearsGiftsLists(getOrException(rd.deceasedDateOfDeath).dateOfDeath))(identity)

        Future.successful(Ok(iht.views.html.application.gift.seven_years_gift_values(
          giftsList,
          rd,
          ad.totalPastYearsGiftsValueExcludingExemptions,
          ad.totalPastYearsGifts,
          ad.totalPastYearsGiftsExemptions,
          ad.totalPastYearsGiftsExemptionsOption.isDefined,
          ad.totalPastYearsGiftsValueExcludingExemptionsOption.isDefined)))
      }
  }

  /**
    * Update the ApplicationDetails
    */

  /**
    * Creates the list of gifts years
    */

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
      val startDateString: String = periodDate.getDayOfMonth() + " " + periodDate.monthOfYear().getAsText()
      val endDateString: String = endDate.getDayOfMonth + " " + endDate.monthOfYear().getAsText()

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
    previousYearsGifts(dateOfDeath).reverse
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
        sevenYearsPriorDate.getDayOfMonth + " " + sevenYearsPriorDate.monthOfYear.getAsText +" " + sevenYearsPriorDate.getYear
      case _ => s"$startDateString ${periodDate.getYear - (noOfIteration - 1)}"
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
      case 1 => s"${dateOfDeath.getDayOfMonth} ${dateOfDeath.monthOfYear.getAsText} ${dateOfDeath.getYear}"
      case _ => s"$endDateString ${periodDate.getYear - (noOfIteration - 2)}"
    }
  }
}
