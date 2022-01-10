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
import iht.connector.IdentityVerificationConnector
import iht.models.enums.IdentityVerificationResult
import iht.views.html.iv.failurepages._
import javax.inject.Inject
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesControllerComponents, _}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.{ExecutionContext, Future}

class IVUpliftFailureControllerImpl @Inject()(val formPartialRetriever: FormPartialRetriever,
                                              val identityVerificationConnector: IdentityVerificationConnector,
                                              implicit val appConfig: AppConfig,
                                              val genericView: generic,
                                              val preconditionFailedView: precondition_failed,
                                              val lockedOutView: locked_out,
                                              val timeoutView: timeout,
                                              val technicalIssueView: technical_issue,
                                              val failure2faView: failure_2fa,
                                              val cc: MessagesControllerComponents) extends FrontendController(cc) with IVUpliftFailureController

trait IVUpliftFailureController extends FrontendController with I18nSupport with Logging {
  val identityVerificationConnector: IdentityVerificationConnector
  implicit val appConfig: AppConfig

  val cc: MessagesControllerComponents
  implicit lazy val ec: ExecutionContext = cc.executionContext

  lazy val ivUrlApplication: String = iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url
  lazy val ivUrlRegistration: String = iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onPageLoad().url

  def showNotAuthorisedApplication(journeyId: Option[String]) : Action[AnyContent] = Action.async {implicit request =>
    showNotAuthorised(journeyId, ivUrlApplication)
  }

  def showNotAuthorisedRegistration(journeyId: Option[String]) : Action[AnyContent] = Action.async {implicit request =>
    showNotAuthorised(journeyId, ivUrlRegistration)
  }

  val genericView: generic
  val preconditionFailedView: precondition_failed
  val lockedOutView: locked_out
  val timeoutView: timeout
  val technicalIssueView: technical_issue
  val failure2faView: failure_2fa
  private def showNotAuthorised(oJourneyId: Option[String], tryAgainRoute: String)(implicit request: Request[AnyContent]): Future[Result] = {
    import IdentityVerificationResult.{PreconditionFailed => PreconditionFailedIV, _}

    oJourneyId.map { journeyId =>
      identityVerificationConnector.identityVerificationResponse(journeyId).map {
        case FailedMatching =>       Forbidden(genericView(tryAgainRoute))
        case PreconditionFailedIV => Forbidden(preconditionFailedView())
        case FailedIV =>             Forbidden(genericView(tryAgainRoute))
        case InsufficientEvidence => Unauthorized(genericView(tryAgainRoute))
        case LockedOut =>            Unauthorized(lockedOutView())
        case Timeout =>              Unauthorized(timeoutView(tryAgainRoute))
        case Incomplete =>           Unauthorized(genericView(tryAgainRoute))
        case UserAborted =>          Unauthorized(genericView(tryAgainRoute))
        case TechnicalIssue =>       InternalServerError(technicalIssueView(tryAgainRoute))
        case other =>
                                     logger.error(s"Unknown identityVerificationResult ($other)")
                                     InternalServerError(technicalIssueView(tryAgainRoute))
      }
    }.getOrElse {
      Future.successful(Unauthorized(failure2faView(tryAgainRoute))) // 2FA returns no journeyId
    }.map(_.withNewSession)
  }

}
