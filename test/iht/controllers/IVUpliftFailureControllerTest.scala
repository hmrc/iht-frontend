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
import iht.controllers.application.ApplicationControllerTest
import iht.models.enums.IdentityVerificationResult
import iht.models.enums.IdentityVerificationResult.IdentityVerificationResult
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.mvc.Result
import play.api.test.Helpers.{OK, _}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class IVUpliftFailureControllerTest extends ApplicationControllerTest {
  implicit val hc = new HeaderCarrier
  val mockIdentityVerificationConnector = mock[IdentityVerificationConnector]

  def controller = new IVUpliftFailureController {
    override val identityVerificationConnector = mockIdentityVerificationConnector
  }


  def iVUpliftFailureBehaviour(showFailure: Option[String] => Future[Result]) = {
    def ivFailure(showFailure: Option[String] => Future[Result], failureName: String, ivResult: IdentityVerificationResult, titleMessagesKey: String) = {
      s"go to the $failureName page upon receipt of iv verification result of " + ivResult in {
        when(mockIdentityVerificationConnector.identityVerificationResponse(any())(any()))
          .thenReturn(Future.successful(ivResult))
        val result = showFailure(Some(""))
        status(result) should be(OK)
        contentAsString(result) should include(Messages(titleMessagesKey))
      }
    }

    "go to 2fa failure page if no journey id" in {
      val result = showFailure(None)
      status(result) should be(OK)
      contentAsString(result) should include(Messages("page.iht.iv.failure.2fa.title"))
    }

    behave like ivFailure(showFailure, "failed matching", IdentityVerificationResult.FailedMatching, "page.iht.iv.failure.failedMatching.title")

    behave like ivFailure(showFailure, "incomplete", IdentityVerificationResult.Incomplete, "error.problem")

    behave like ivFailure(showFailure, "insufficient evidence", IdentityVerificationResult.InsufficientEvidence, "error.problem")

    behave like ivFailure(showFailure, "locked out", IdentityVerificationResult.LockedOut, "page.iht.iv.failure.lockedOut.title")

    behave like ivFailure(showFailure, "precondition failed", IdentityVerificationResult.PreconditionFailed, "page.iht.iv.failure.preconditionFailed.title")

    behave like ivFailure(showFailure, "technical issue", IdentityVerificationResult.TechnicalIssue, "page.iht.iv.failure.technicalIssue.title")

    behave like ivFailure(showFailure, "timeout", IdentityVerificationResult.Timeout, "page.iht.iv.failure.timeout.title")

    behave like ivFailure(showFailure, "user aborted", IdentityVerificationResult.UserAborted, "page.iht.iv.failure.userAborted.title")

    behave like ivFailure(showFailure, "technical issue", IdentityVerificationResult.Success, "page.iht.iv.failure.technicalIssue.title")
  }

  "showNotAuthorisedRegistration" must {
    behave like iVUpliftFailureBehaviour(x => controller.showNotAuthorisedRegistration(x)(createFakeRequest()))
  }

  "showNotAuthorisedApplication" must {
    behave like iVUpliftFailureBehaviour(x => controller.showNotAuthorisedApplication(x)(createFakeRequest()))
  }
}
