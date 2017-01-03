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

import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}

/**
 * Created by yasar on 2/19/15.
 */
object SessionTimeoutController extends SessionTimeoutController{
}

trait SessionTimeoutController extends FrontendController{

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
}
