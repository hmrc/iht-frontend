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

package iht.controllers.filter

import iht.config.{IhtFormPartialRetriever, FrontendAuthConnector}
import iht.connector.{CachingConnector, IhtConnector}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.{UnauthorisedAction, FrontendController}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.partials.FormPartialRetriever
import scala.concurrent.Future

/**
  * Created by adwelly on 21/10/2016.
  */

object AgentController extends AgentController {
  val cachingConnector = CachingConnector
  val ihtConnector = IhtConnector
  val authConnector: AuthConnector = FrontendAuthConnector
}

trait AgentController extends FrontendController {
  def cachingConnector: CachingConnector
  def ihtConnector: IhtConnector

  implicit val formPartialRetriever: FormPartialRetriever = IhtFormPartialRetriever

  def onPageLoad = UnauthorisedAction.async {
    implicit request => {
      Future.successful(Ok(iht.views.html.filter.agent_view()))
    }
  }
}
