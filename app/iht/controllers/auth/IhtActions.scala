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

package iht.controllers.auth

import iht.utils.{AuthHelper, IhtSection}
import play.api.Play

import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.passcode.authentication.PasscodeAuthentication
import uk.gov.hmrc.play.frontend.auth._

import scala.concurrent.Future

trait IhtActions extends Actions with CustomPasscodeAuthentication {
  private type AsyncPlayUserRequest = AuthContext => (Request[AnyContent] => Future[Result])

  private def grantAccessIfOnWhitelist(body: AsyncPlayUserRequest)(implicit authContext: AuthContext, request: Request[AnyContent]): Future[Result] = {
    withVerifiedPasscode {
      body(authContext)(request)
    }
  }

  protected val ihtSection: IhtSection.Value

  val isWhiteListEnabled: Boolean = Play.configuration.getBoolean("passcodeAuthentication.enabled").getOrElse(false)
  private lazy val ihtCompositePageVisibilityPredicate = AuthHelper.getIhtCompositePageVisibilityPredicate(ihtSection)
  lazy val ihtRegime = AuthHelper.getIhtTaxRegime(ihtSection)

  def authorisedForIht(body: AsyncPlayUserRequest): Action[AnyContent] = {
    AuthorisedFor(
      taxRegime = ihtRegime,
      pageVisibility = ihtCompositePageVisibilityPredicate
    ).async {
      implicit authContext =>
        implicit request =>
          if (isWhiteListEnabled) {
            grantAccessIfOnWhitelist(body)
          } else {
            body(authContext)(request)
          }
    }
  }
}
