/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesControllerComponents, _}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.{ExecutionContext, Future}

class IVUpliftFailureControllerImpl @Inject()(val formPartialRetriever: FormPartialRetriever,
                                              val identityVerificationConnector: IdentityVerificationConnector,
                                              implicit val appConfig: AppConfig,
                                              val cc: MessagesControllerComponents) extends FrontendController(cc) with IVUpliftFailureController

trait IVUpliftFailureController extends FrontendController with I18nSupport {
  val identityVerificationConnector: IdentityVerificationConnector
  implicit val formPartialRetriever: FormPartialRetriever
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

  private def showNotAuthorised(oJourneyId: Option[String], tryAgainRoute: String)(implicit request: Request[AnyContent]): Future[Result] = {
    import IdentityVerificationResult.{PreconditionFailed => PreconditionFailedIV, _}

    oJourneyId.map { journeyId =>
      identityVerificationConnector.identityVerificationResponse(journeyId).map {
        case FailedMatching =>       Forbidden(failed_matching(tryAgainRoute))
        case PreconditionFailedIV => Forbidden(precondition_failed())
        case InsufficientEvidence => Unauthorized(insufficient_evidence(tryAgainRoute))
        case LockedOut =>            Unauthorized(locked_out())
        case Timeout =>              Unauthorized(timeout(tryAgainRoute))
        case Incomplete =>           Unauthorized(incomplete(tryAgainRoute))
        case UserAborted =>          Unauthorized(user_aborted(tryAgainRoute))
        case TechnicalIssue =>       InternalServerError(technical_issue(tryAgainRoute))
        case other =>
                                     Logger.error(s"Unknown identityVerificationResult ($other)")
                                     InternalServerError(technical_issue(tryAgainRoute))
      }
    }.getOrElse {
      Future.successful(Unauthorized(failure_2fa(tryAgainRoute))) // 2FA returns no journeyId
    }.map(_.withNewSession)
  }

}
