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

import iht.connector.IdentityVerificationConnector
import iht.models.enums.IdentityVerificationResult
import iht.views.html.iv.failurepages._
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}
import play.api.i18n.Messages.Implicits._



import scala.concurrent.Future

/**
  * Created by yasar on 2/19/15.
  */
object IVUpliftFailureController extends IVUpliftFailureController{
}

trait IVUpliftFailureController extends FrontendController{

  val identityVerificationConnector: IdentityVerificationConnector = IdentityVerificationConnector

  val ivUrlApplication = iht.controllers.home.routes.IhtHomeController.onPageLoad().url
  val ivUrlRegistration =iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onPageLoad().url

  def showNotAuthorisedApplication(journeyId: Option[String]) : Action[AnyContent] = UnauthorisedAction.async {implicit request =>
    Logger.debug(message = "Entered showNotAuthorisedApplication with journeyId " + journeyId)
    val result = journeyId map { id =>
      val identityVerificationResult = identityVerificationConnector.identityVerificationResponse(id)
      Logger.debug(message = "Obtained identityVerificationResult is " + identityVerificationResult)
      identityVerificationResult map {
        case IdentityVerificationResult.FailedMatching => failed_matching()
        case IdentityVerificationResult.InsufficientEvidence => insufficient_evidence(ivUrlApplication)
        case IdentityVerificationResult.TechnicalIssue => technical_issue(ivUrlApplication)
        case IdentityVerificationResult.LockedOut => locked_out()
        case IdentityVerificationResult.Timeout => timeout(ivUrlApplication)
        case IdentityVerificationResult.Incomplete => incomplete()
        case IdentityVerificationResult.PreconditionFailed => precondition_failed()
        case IdentityVerificationResult.UserAborted => user_aborted(ivUrlApplication)
        case ivr =>
          Logger.error("Unknown identityVerificationResult (" + ivr + ")" )
          technical_issue(ivUrlApplication)
      }
    } getOrElse {
      Future.successful(failure_2fa(ivUrlApplication)) // 2FA returns no journeyId
    }

    result.map {
      Ok(_).withNewSession
    }
  }

  def showNotAuthorisedRegistration(journeyId: Option[String]) : Action[AnyContent] = UnauthorisedAction.async {implicit request =>
    Logger.debug(message = "Entered showNotAuthorisedApplication with journeyId " + journeyId)
    val result = journeyId map { id =>
      val identityVerificationResult = identityVerificationConnector.identityVerificationResponse(id)
      Logger.debug(message = "Obtained identityVerificationResult is " + identityVerificationResult)
      identityVerificationResult map {
        case IdentityVerificationResult.FailedMatching => failed_matching()
        case IdentityVerificationResult.InsufficientEvidence => insufficient_evidence(ivUrlRegistration)
        case IdentityVerificationResult.TechnicalIssue => technical_issue(ivUrlRegistration)
        case IdentityVerificationResult.LockedOut => locked_out()
        case IdentityVerificationResult.Timeout => timeout(ivUrlRegistration)
        case IdentityVerificationResult.Incomplete => incomplete()
        case IdentityVerificationResult.PreconditionFailed => precondition_failed()
        case IdentityVerificationResult.UserAborted => user_aborted(ivUrlRegistration)
        case ivr =>
          Logger.error("Unknown identityVerificationResult (" + ivr + ")" )
          technical_issue(ivUrlRegistration)
      }
    } getOrElse {
      Future.successful(failure_2fa(ivUrlRegistration)) // 2FA returns no journeyId
    }

    result.map {
      Ok(_).withNewSession
    }
  }

}
