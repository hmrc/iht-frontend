/*
 * Copyright 2019 HM Revenue & Customs
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

import iht.config.{AppConfig, FrontendAuthConnector, IhtFormPartialRetriever}
import iht.connector.IhtConnectors
import iht.controllers.auth.IhtBaseController
import iht.utils.IhtSection
import javax.inject.Inject
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by yasar on 7/6/15.
 */
class SessionManagementControllerImpl @Inject()() extends SessionManagementController

trait SessionManagementController extends IhtBaseController with IhtConnectors {
  override lazy val ihtSection = IhtSection.Application

  implicit val formPartialRetriever: FormPartialRetriever = IhtFormPartialRetriever

  def signOut = UnauthorisedAction.async {
    implicit request => {
      Future.successful(Ok(iht.views.html.sign_out()).withNewSession)
    }
  }

  def keepAlive = authorisedForIht {
    implicit request => {
      Future.successful(Ok("OK"))
    }
  }
}
