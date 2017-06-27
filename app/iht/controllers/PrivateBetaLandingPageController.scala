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

import javax.inject.Singleton

import iht.config.FrontendAuthConnector
import iht.controllers.auth.CustomPasscodeAuthentication
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.passcode.authentication.{PasscodeAuthenticationProvider, PasscodeVerificationConfig}

import scala.concurrent.Future

@Singleton
class PrivateBetaLandingPageController extends FrontendController  with CustomPasscodeAuthentication {

  /**
   * Redirection from an unauthorised action is necessary because otherwise *something* in the tax
   * platform clears the token (p) from the request object, for some unknown reason. So we create a
   * copy of the token and pass it into the authenticated action twice, since the first one will be
   * cleared.
 *
   * @param p Whitelisting token
   */
  def passcode(p: Option[String]) = UnauthorisedAction {
    implicit request => {
      p match {
        case Some(token) => Redirect(routes.PrivateBetaLandingPageController.showLandingPageWithPasscode(p.getOrElse(""), p))
        case None => Redirect(routes.PrivateBetaLandingPageController.showLandingPage())
      }
    }
  }

  def showLandingPageWithPasscode(passcodeCopy:String, p:Option[String]) = customAuthenticatedActionAsync {
    implicit request => {
      Future.successful(Ok(iht.views.html.private_beta_landing_page(p)))
    }
  }

  def showLandingPage = customAuthenticatedActionAsync {
    implicit request => {
      Future.successful(Ok(iht.views.html.private_beta_landing_page(None)))
    }
  }

  def start(p:Option[String]) = customAuthenticatedActionAsync {
    implicit request => {
      Future.successful(Redirect(iht.controllers.filter.routes.FilterController.onPageLoad))
    }
  }
}
