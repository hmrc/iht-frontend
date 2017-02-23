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

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationController
import iht.controllers.{ControllerHelper, IhtConnectors}
import iht.utils.CommonHelper
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
 * Created by james on 21/01/16.
 */
object ClaimingExemptionsController extends ClaimingExemptionsController with IhtConnectors

trait ClaimingExemptionsController extends ApplicationController {

  def cachingConnector : CachingConnector
  def ihtConnector : IhtConnector

  def onPageLoad() = authorisedForIht {
    implicit user => implicit request => {

      val lastQuestionUrl: Option[String] = Await.result(cachingConnector.getSingleValue(ControllerHelper.lastQuestionUrl), Duration.Inf)

      Future.successful(Ok(iht.views.html.application.gift.guidance.claiming_exemptions(CommonHelper
        .getOrExceptionNoIHTRef(cachingConnector.getExistingRegistrationDetails.ihtReference),
        lastQuestionUrl,
        Some("site.backToLastQuestion.values.link"),
        Some("site.backToLastQuestion.values.link"))))
    }
  }
}
