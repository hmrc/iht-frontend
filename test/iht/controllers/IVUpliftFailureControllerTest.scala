/*
 * Copyright 2021 HM Revenue & Customs
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
import iht.controllers.application.ApplicationControllerTest
import iht.models.enums.IdentityVerificationResult
import iht.models.enums.IdentityVerificationResult.IdentityVerificationResult
import iht.views.html.iv.failurepages._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class IVUpliftFailureControllerTest extends ApplicationControllerTest {
  implicit val hc = new HeaderCarrier
  val mockIdentityVerificationConnector = mock[IdentityVerificationConnector]

  override implicit val messages: Messages = mockControllerComponents.messagesApi.preferred(Seq(Lang.defaultLang)).messages
  protected abstract class TestController extends FrontendController(mockControllerComponents) with IVUpliftFailureController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def controller = new TestController {
    override val identityVerificationConnector = mockIdentityVerificationConnector
    override val genericView: generic = app.injector.instanceOf[generic]
    override val preconditionFailedView: precondition_failed = app.injector.instanceOf[precondition_failed]
    override val lockedOutView: locked_out = app.injector.instanceOf[locked_out]
    override val timeoutView: timeout = app.injector.instanceOf[timeout]
    override val technicalIssueView: technical_issue = app.injector.instanceOf[technical_issue]
    override val failure2faView: failure_2fa = app.injector.instanceOf[failure_2fa]
  }


  def iVUpliftFailureBehaviour(showFailure: Option[String] => Future[Result]) = {

    def ivFailure(failureName: String, ivResult: IdentityVerificationResult,
                  titleMessagesKey: String, expectedStatus: Int) = {
      s"go to the $failureName page upon receipt of iv verification result of " + ivResult in {
        when(mockIdentityVerificationConnector.identityVerificationResponse(any())(any(), any()))
          .thenReturn(Future.successful(ivResult))
        val result = showFailure(Some(""))
        status(result) must be(expectedStatus)
        contentAsString(result) must include(messagesApi(titleMessagesKey))
      }
    }

    "go to 2fa failure page if no journey id" in {
      val result = showFailure(None)
      status(result) must be(UNAUTHORIZED)
      contentAsString(result) must include(messagesApi("page.iht.iv.failure.2fa.heading"))
    }

    behave like ivFailure("failed matching", IdentityVerificationResult.FailedMatching, "page.iht.iv.failure.couldNotConfirmIdentity", FORBIDDEN)

    behave like ivFailure("incomplete", IdentityVerificationResult.Incomplete, "page.iht.iv.failure.couldNotConfirmIdentity", UNAUTHORIZED)

    behave like ivFailure("insufficient evidence", IdentityVerificationResult.InsufficientEvidence, "page.iht.iv.failure.couldNotConfirmIdentity", UNAUTHORIZED)

    behave like ivFailure("locked out", IdentityVerificationResult.LockedOut, "page.iht.iv.failure.couldNotConfirmIdentity", UNAUTHORIZED)

    behave like ivFailure("precondition failed", IdentityVerificationResult.PreconditionFailed, "page.iht.iv.failure.preconditionFailed.heading", FORBIDDEN)

    behave like ivFailure("technical issue", IdentityVerificationResult.TechnicalIssue, "page.iht.iv.failure.technicalIssue.heading", INTERNAL_SERVER_ERROR)

    behave like ivFailure("timeout", IdentityVerificationResult.Timeout, "page.iht.iv.failure.timeout.heading", UNAUTHORIZED)

    behave like ivFailure("user aborted", IdentityVerificationResult.UserAborted, "page.iht.iv.failure.couldNotConfirmIdentity", UNAUTHORIZED)

    behave like ivFailure("unexpected IV failure result", IdentityVerificationResult.Success, "page.iht.iv.failure.technicalIssue.heading", INTERNAL_SERVER_ERROR)
  }

  "showNotAuthorisedRegistration" must {
    behave like iVUpliftFailureBehaviour(x => controller.showNotAuthorisedRegistration(x)(createFakeRequest()))
  }

  "showNotAuthorisedApplication" must {
    behave like iVUpliftFailureBehaviour(x => controller.showNotAuthorisedApplication(x)(createFakeRequest()))
  }
}
