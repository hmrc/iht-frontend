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

package iht.controllers.auth

import iht.config.ApplicationConfig
import iht.utils.{AuthHelper, IhtSection}
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthProviders, AuthorisationException, AuthorisedFunctions, ConfidenceLevel, InsufficientConfidenceLevel}
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

trait IhtBaseController extends FrontendController with AuthorisedFunctions with AuthHelper {
  private type AsyncPlayRequest = Request[AnyContent] => Future[Result]
  private type AsyncPlayUserRequest[A] = A => Request[AnyContent] => Future[Result]

  protected val ihtSection: IhtSection.Value
  protected lazy val confidenceLevel: Int = ApplicationConfig.ivUpliftConfidenceLevel

  private lazy val predicate: Predicate = AuthProviders(GovernmentGateway) and AffinityGroup.Individual and ConfidenceLevel.fromInt(confidenceLevel).get
  private def handleAuthErrors(implicit request: Request[_]): PartialFunction[Throwable, Result] = {
    case e: InsufficientConfidenceLevel =>
      Logger.info(s"Insufficient confidence level user attempting to access ${request.path} redirecting to IV uplift : ${e.getMessage}")
      redirectToIV
    case e: AuthorisationException =>
      Logger.info(s"unauthenticated user attempting to access ${request.path} redirecting to login : ${e.getMessage}")
      redirectToLogin
  }

  def redirectToLogin: Result = {
    val loginUrl: String = getIhtSignInUrl
    val continueUrl: String = getIhtContinueUrl(ihtSection)

    Redirect(loginUrl, Map(
      "continue" -> Seq(continueUrl),
      "origin" -> Seq(appName)
    ))
  }

  def redirectToIV: Result = Redirect(getIVUrlForFailedConfidenceLevel(ihtSection, confidenceLevel), Map(
    "origin" -> Seq(appName)
  ))

  def authorisedForIht(body: AsyncPlayRequest): Action[AnyContent] = Action.async { implicit request =>
    authorised(predicate) {
      body(request)
    } recover handleAuthErrors
  }

  def authorisedForIhtWithRetrievals[A](retrieval: Retrieval[A])(body: AsyncPlayUserRequest[A]): Action[AnyContent] = Action.async { implicit request =>
    authorised(predicate).retrieve(retrieval) { user =>
      body(user)(request)
    } recover handleAuthErrors
  }
}
