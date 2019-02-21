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

package iht.controllers

import iht.connector.IdentityVerificationConnector
import iht.controllers.application.ApplicationControllerTest
import iht.models.enums.IdentityVerificationResult
import iht.models.enums.IdentityVerificationResult.IdentityVerificationResult
import iht.testhelpers.MockFormPartialRetriever
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class IVUpliftFailureControllerTest extends ApplicationControllerTest {
  implicit val hc = new HeaderCarrier
  val mockIdentityVerificationConnector = mock[IdentityVerificationConnector]

  def controller = new IVUpliftFailureController {
    override val identityVerificationConnector = mockIdentityVerificationConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }


  def iVUpliftFailureBehaviour(showFailure: Option[String] => Future[Result]) = {

    def ivFailure(failureName: String, ivResult: IdentityVerificationResult,
                  titleMessagesKey: String, expectedStatus: Int) = {
      s"go to the $failureName page upon receipt of iv verification result of " + ivResult in {
        when(mockIdentityVerificationConnector.identityVerificationResponse(any())(any()))
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

    behave like ivFailure("failed matching", IdentityVerificationResult.FailedMatching, "page.iht.iv.failure.failedMatching.failureReason", FORBIDDEN)

    behave like ivFailure("incomplete", IdentityVerificationResult.Incomplete, "page.iht.iv.failure.incomplete.heading", UNAUTHORIZED)

    behave like ivFailure("insufficient evidence", IdentityVerificationResult.InsufficientEvidence, "page.iht.iv.failure.insufficientEvidence.failureReason", UNAUTHORIZED)

    behave like ivFailure("locked out", IdentityVerificationResult.LockedOut, "page.iht.iv.failure.lockedOut.heading", UNAUTHORIZED)

    behave like ivFailure("precondition failed", IdentityVerificationResult.PreconditionFailed, "page.iht.iv.failure.preconditionFailed.heading", FORBIDDEN)

    behave like ivFailure("technical issue", IdentityVerificationResult.TechnicalIssue, "page.iht.iv.failure.technicalIssue.heading", INTERNAL_SERVER_ERROR)

    behave like ivFailure("timeout", IdentityVerificationResult.Timeout, "page.iht.iv.failure.timeout.heading", UNAUTHORIZED)

    behave like ivFailure("user aborted", IdentityVerificationResult.UserAborted, "page.iht.iv.failure.userAborted.failureReason", UNAUTHORIZED)

    behave like ivFailure("unexpected IV failure result", IdentityVerificationResult.Success, "page.iht.iv.failure.technicalIssue.heading", INTERNAL_SERVER_ERROR)
  }

  "showNotAuthorisedRegistration" must {
    behave like iVUpliftFailureBehaviour(x => controller.showNotAuthorisedRegistration(x)(createFakeRequest()))
  }

  "showNotAuthorisedApplication" must {
    behave like iVUpliftFailureBehaviour(x => controller.showNotAuthorisedApplication(x)(createFakeRequest()))
  }
}
