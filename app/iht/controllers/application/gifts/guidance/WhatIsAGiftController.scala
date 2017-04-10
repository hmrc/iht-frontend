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

package iht.controllers.application.gifts.guidance

import iht.connector.{CachingConnector, IhtConnector, IhtConnectors}
import iht.controllers.application.ApplicationController
import iht.controllers.ControllerHelper
import iht.utils.CommonHelper
import play.Logger
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by james on 21/01/16.
  */
object WhatIsAGiftController extends WhatIsAGiftController with IhtConnectors

trait WhatIsAGiftController extends ApplicationController {

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad() = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { registrationDetails =>
          val lastQuestionUrl: Option[String] = Await.result(cachingConnector.getSingleValue(ControllerHelper.lastQuestionUrl), Duration.Inf)

          for {
            applicationDetails <- ihtConnector.getApplication(CommonHelper.getNino(user),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(appDetails) => {
                val ihtReference = CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference)
                lazy val optionMessageKey = lastQuestionUrl.map(url => ControllerHelper.messageKeyForLastQuestionURL(url))

                cachingConnector.storeSingleValue(ControllerHelper.GiftsGuidanceSeen, true.toString)

                Ok(iht.views.html.application.gift.guidance.what_is_a_gift(ihtReference,
                  lastQuestionUrl,
                  optionMessageKey,
                  optionMessageKey))

              }
              case _ => {
                Logger.warn("No Application Details found. Redirecting to Internal Server Error")
                InternalServerError("No Application Details Found")
              }
            }
          }
        }
      }
  }
}
