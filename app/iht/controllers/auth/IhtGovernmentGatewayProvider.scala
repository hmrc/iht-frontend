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

import iht.config.ApplicationConfig
import iht.controllers.routes
import play.api.mvc.Request
import play.api.mvc.Results._
import uk.gov.hmrc.play.frontend.auth.GovernmentGateway

import scala.concurrent.Future

/**
  * Created by yasar on 22/09/16.
  */
trait IhtGovernmentGatewayProvider extends GovernmentGateway{
  override val additionalLoginParameters: Map[String, Seq[String]] = Map("accountType" -> Seq("individual"))
  override val defaultTimeoutSeconds = ApplicationConfig.timeOutSeconds
}

object GovernmentGatewayProviderForRegistration extends IhtGovernmentGatewayProvider {
  override def handleSessionTimeout(implicit request: Request[_]): Future[FailureResult] =
    Future.successful(Redirect(routes.SessionTimeoutController.onRegistrationPageLoad()))
  override def loginURL: String = ApplicationConfig.ggSignInUrl
  override def continueURL: String = ApplicationConfig.postSignInRedirectUrlRegistration
}

object GovernmentGatewayProviderForApplication extends IhtGovernmentGatewayProvider {
  override def handleSessionTimeout(implicit request: Request[_]): Future[FailureResult] =
    Future.successful(Redirect(routes.SessionTimeoutController.onApplicationPageLoad()))
  override def loginURL: String = ApplicationConfig.ggSignInUrl
  override def continueURL: String = ApplicationConfig.postSignInRedirectUrlApplication
}
