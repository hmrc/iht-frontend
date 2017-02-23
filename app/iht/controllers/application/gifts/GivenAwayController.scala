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

import iht.controllers.application.EstateController
import iht.controllers.{ControllerHelper, IhtConnectors}
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.AllGifts
import iht.utils.{ApplicationStatus => AppStatus}
import iht.views.html.application.gift.given_away
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

/**
 *
 * Created by Vineet Tyagi on 14/01/16.
 *
 */
object GivenAwayController extends GivenAwayController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait GivenAwayController extends EstateController{

  def onPageLoad = authorisedForIht {

   implicit user => implicit request =>
     cachingConnector.storeSingleValue(ControllerHelper.lastQuestionUrl,
       iht.controllers.application.gifts.routes.GivenAwayController.onPageLoad().toString())

     estateElementOnPageLoad[AllGifts](giftsGivenAwayForm, given_away.apply, _.allGifts)
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], AllGifts) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, gifts) => {
          val updatedAllDetails = appDetails.copy(status=AppStatus.InProgress, allGifts = Some(appDetails.allGifts.fold
            (new AllGifts(isGivenAway = gifts.isGivenAway, None, None, None, None))
            (_.copy(isGivenAway = gifts.isGivenAway))))
          (updateApplicationDetailsWithUpdatedAllGifts(updatedAllDetails), None)
        }

      estateElementOnSubmit[AllGifts](giftsGivenAwayForm, given_away.apply, updateApplicationDetails, giftsRedirectLocation)
    }
  }

  /**
    * Resets all the AllGifts values to None when GivenAway question's answer is no
    */
  private def updateApplicationDetailsWithUpdatedAllGifts(appDetails: ApplicationDetails) ={
    val isGivenAway = appDetails.allGifts.flatMap(_.isGivenAway)
    val isReservation = appDetails.allGifts.flatMap(_.isReservation)
    val isToTrust = appDetails.allGifts.flatMap(_.isToTrust)
    val isGivenInLast7Years = appDetails.allGifts.flatMap(_.isGivenInLast7Years)
    val action = appDetails.allGifts.flatMap(_.action)

    isGivenAway.fold(appDetails) {
      givenAwayValue => if (!givenAwayValue) {
        appDetails copy(
          allGifts = Some(AllGifts(isGivenAway = Some(false),isReservation = isReservation, isToTrust = isToTrust,
            isGivenInLast7Years = isGivenInLast7Years, action = action)),
          giftsList = None)
      } else {
        appDetails
      }
    }
  }
}
