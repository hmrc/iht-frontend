/*
 * Copyright 2022 HM Revenue & Customs
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

import iht.config.AppConfig
import iht.controllers.auth.IhtBaseController
import iht.utils.IhtSection
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.sign_out

import scala.concurrent.Future

class SessionManagementControllerImpl @Inject()(val authConnector: AuthConnector,
                                                val signOutView: sign_out,
                                                implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents)
  extends FrontendController(cc) with SessionManagementController

trait SessionManagementController extends IhtBaseController with I18nSupport {
  override lazy val ihtSection: IhtSection.Value = IhtSection.Application
  val signOutView: sign_out

  def signOut: Action[AnyContent] = Action.async {
    implicit request => { // False positive warning. Workaround: scala/bug#11175 -Ywarn-unused:params false positive
      Future.successful(Ok(signOutView()).withNewSession)
    }
  }

  def keepAlive: Action[AnyContent] = authorisedForIht {
    implicit request => { // False positive warning. Workaround: scala/bug#11175 -Ywarn-unused:params false positive
      Future.successful(Ok("OK"))
    }
  }
}
