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

import iht.config.{AppConfig, IhtFormPartialRetriever}
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class SessionTimeoutControllerImpl @Inject()(val formPartialRetriever: IhtFormPartialRetriever,
                                             val cc: MessagesControllerComponents,
                                             implicit val appConfig: AppConfig) extends FrontendController(cc) with SessionTimeoutController

trait SessionTimeoutController extends FrontendController with I18nSupport {
  implicit val appConfig: AppConfig
  implicit val formPartialRetriever: FormPartialRetriever

  def onRegistrationPageLoad: Action[AnyContent] = Action {
    implicit request => {
      Ok(iht.views.html.registration.timeout_registration())
    }
  }

  def onApplicationPageLoad: Action[AnyContent] = Action {
    implicit request => {
      Ok(iht.views.html.application.timeout_application())
    }
  }

  def onSaveAndExitPageLoad: Action[AnyContent] = Action {
    implicit request => {
      Ok(iht.views.html.estateReports.save_your_estate_report())
    }
  }
}
