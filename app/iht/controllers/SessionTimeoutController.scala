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

import iht.config.IhtFormPartialRetriever
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.{FrontendController, UnauthorisedAction}
import uk.gov.hmrc.play.partials.FormPartialRetriever

class SessionTimeoutControllerImpl @Inject()(val messagesApi: MessagesApi,
                                             val formPartialRetriever: IhtFormPartialRetriever) extends SessionTimeoutController

trait SessionTimeoutController extends FrontendController with I18nSupport {

  implicit val formPartialRetriever: FormPartialRetriever

  def onRegistrationPageLoad = UnauthorisedAction {
    implicit request => {
      Ok(iht.views.html.registration.timeout_registration())
    }
  }

  def onApplicationPageLoad = UnauthorisedAction {
    implicit request => {
      Ok(iht.views.html.application.timeout_application())
    }
  }

  def onSaveAndExitPageLoad = UnauthorisedAction {
    implicit request => {
      Ok(iht.views.html.estateReports.save_your_estate_report())
    }
  }
}
