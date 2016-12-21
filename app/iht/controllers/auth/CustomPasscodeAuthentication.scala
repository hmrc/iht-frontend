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

package iht.controllers.auth

import play.api.Play
import play.api.Play.current
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.passcode.authentication.PasscodeAuthentication
import uk.gov.hmrc.passcode.authentication.PlayRequestTypes._
import uk.gov.hmrc.play.frontend.controller.UnauthorisedAction

/**
 * Created by yasar on 7/6/15.
 */
trait CustomPasscodeAuthentication extends PasscodeAuthentication {
  val whitelistEnabled: Boolean = Play.configuration.getBoolean(
    "passcodeAuthentication.enabled").getOrElse(false)

  def customAuthenticatedAction(body: PlayRequest): Action[AnyContent] = {
    if (whitelistEnabled) {
      PasscodeAuthenticatedAction(body)
    } else {
      UnauthorisedAction(body)
    }
  }

  def customAuthenticatedActionAsync(body: => AsyncPlayRequest) = {
    if (whitelistEnabled) {
      PasscodeAuthenticatedActionAsync(body)
    } else {
      UnauthorisedAction.async(body)
    }
  }
}
