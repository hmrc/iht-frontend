/*
 * Copyright 2016 HM Revenue & Customs
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

package iht.controllers.application.gifts.guidance

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationController
import iht.controllers.{ControllerHelper, IhtConnectors}
import iht.models.RegistrationDetails
import iht.utils._
import play.api.mvc.Call

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
 * Created by james on 22/01/16.
 */
object GiftsGivenAwayController extends GiftsGivenAwayController with IhtConnectors

trait GiftsGivenAwayController extends ApplicationController {

  def cachingConnector : CachingConnector
  def ihtConnector : IhtConnector

  def onPageLoad() = authorisedForIht {
    implicit user => implicit request => {

      val lastQuestionUrl: Option[String] = Await.result(cachingConnector.getSingleValue(ControllerHelper.lastQuestionUrl), Duration.Inf)

      lazy val optionMessageKey = lastQuestionUrl.map(url=> ControllerHelper.messageKeyForLastQuestionURL(url))

      Future.successful(Ok(iht.views.html.application.gift.guidance.gifts_given_away(CommonHelper
        .getOrExceptionNoIHTRef(cachingConnector.getExistingRegistrationDetails.ihtReference),
        lastQuestionUrl,optionMessageKey,optionMessageKey)
        ))
    }
  }

  def onSubmit() = authorisedForIht {
    implicit user => implicit request => {
      val regDetails: RegistrationDetails = cachingConnector.getExistingRegistrationDetails

      val applicationDetailsFuture = ihtConnector.getApplication(CommonHelper.getNino(user),
        CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
        regDetails.acknowledgmentReference)

      val futureResult = applicationDetailsFuture.map{ oad => {
            val ff = oad.map{ ad =>
              Redirect(ad.allGifts.fold[Call](iht.controllers.application.gifts.routes.GivenAwayController.onPageLoad())(_ =>
                iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad()))
            }
            CommonHelper.getOrException(ff)
          }
        }
      futureResult
    }
  }
}
