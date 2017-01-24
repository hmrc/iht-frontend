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
import iht.controllers.application.EstateController
import iht.controllers.{ControllerHelper, IhtConnectors}
import iht.metrics.Metrics
import iht.utils.CommonHelper
import iht.utils.GiftsHelper._
import scala.concurrent.Future

/**
  * Created by vineet on 11/04/16.
  */
object SevenYearsGiftsValuesController extends SevenYearsGiftsValuesController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait SevenYearsGiftsValuesController extends EstateController {

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request =>
        cachingConnector.storeSingleValue(ControllerHelper.lastQuestionUrl,
          routes.SevenYearsGiftsValuesController.onPageLoad().toString())
        withApplicationDetails { rd =>
          ad =>
            CommonHelper.getOrException(rd.deceasedDateOfDeath.map(ddod =>
              Future.successful(Ok(iht.views.html.application.gift.seven_years_gift_values(
                ad.giftsList.fold(createPreviousYearsGiftsLists(ddod.dateOfDeath))(identity),
                rd,
                ad.totalPastYearsGiftsValueExcludingExemptions,
                ad.totalPastYearsGifts,
                ad.totalPastYearsGiftsExemptions,
                ad.totalPastYearsGiftsExemptionsOption.isDefined,
                ad.totalPastYearsGiftsValueExcludingExemptionsOption.isDefined)))
            ))
        }
  }
}
