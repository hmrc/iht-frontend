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

package iht.controllers

import iht.config.IhtFormPartialRetriever
import iht.connector.CachingConnector
import iht.views.html.{deadlines_application, deadlines_registration}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

/**
  * Created by yasar on 2/19/15.
  */

trait DeadlinesController extends FrontendController {
  implicit val formPartialRetriever: FormPartialRetriever = IhtFormPartialRetriever

  def cachingConnector: CachingConnector

  def onPageLoadRegistration = UnauthorisedAction.async {
    implicit request => {
      Future.successful(Ok(deadlines_registration.apply))
    }
  }

  def onPageLoadApplication = UnauthorisedAction.async {
    implicit request => {
      Future.successful(Ok(deadlines_application.apply))
    }
  }
}

object DeadlinesController extends DeadlinesController {
  val cachingConnector = CachingConnector
}