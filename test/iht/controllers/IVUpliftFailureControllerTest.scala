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
import play.api.test.Helpers.{OK, _}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class IVUpliftFailureControllerTest extends ApplicationControllerTest {
  implicit val hc = new HeaderCarrier
  val mockIdentityVerificationConnector = mock[IdentityVerificationConnector]
  def controller = new IVUpliftFailureController {
    override val identityVerificationConnector = mockIdentityVerificationConnector
  }

  def ivFailureApplication(failureName: String, ivResult: IdentityVerificationResult, titleMessagesKey:String) = {
    s"Go to the $failureName page when the appropriate IV verification result is received" in {
      when(mockIdentityVerificationConnector.identityVerificationResponse(any())(any()))
        .thenReturn(Future.successful(ivResult))
      val result = controller.showNotAuthorisedApplication(Some(""))(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(Messages(titleMessagesKey))
    }
  }

  def ivFailureRegistration(failureName: String, ivResult: IdentityVerificationResult, titleMessagesKey:String) = {
    s"Go to the $failureName page when the appropriate IV verification result is received" in {
      when(mockIdentityVerificationConnector.identityVerificationResponse(any())(any()))
        .thenReturn(Future.successful(ivResult))
      val result = controller.showNotAuthorisedRegistration(Some(""))(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(Messages(titleMessagesKey))
    }
  }

  "showNotAuthorisedApplication" must {
    "go to 2fa failure page if no journey id" in {
      val result = controller.showNotAuthorisedApplication(None)(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(Messages("page.iht.iv.failure.2fa.title"))
    }

    behave like ivFailureApplication("failed matching", IdentityVerificationResult.FailedMatching, "page.iht.iv.failure.failedMatching.title")

    behave like ivFailureApplication("incomplete", IdentityVerificationResult.Incomplete, "error.problem")

    behave like ivFailureApplication("insufficient evidence", IdentityVerificationResult.InsufficientEvidence, "error.problem")

    behave like ivFailureApplication("locked out", IdentityVerificationResult.LockedOut, "page.iht.iv.failure.lockedOut.title")

    behave like ivFailureApplication("precondition failed", IdentityVerificationResult.PreconditionFailed, "page.iht.iv.failure.preconditionFailed.title")

    behave like ivFailureApplication("technical issue", IdentityVerificationResult.TechnicalIssue, "page.iht.iv.failure.technicalIssue.title")

    behave like ivFailureApplication("timeout", IdentityVerificationResult.Timeout, "page.iht.iv.failure.timeout.title")

    behave like ivFailureApplication("user aborted", IdentityVerificationResult.UserAborted, "page.iht.iv.failure.userAborted.title")
  }

  "showNotAuthorisedRegistration" must {
    "go to 2fa failure page if no journey id" in {
      val result = controller.showNotAuthorisedRegistration(None)(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(Messages("page.iht.iv.failure.2fa.title"))
    }

    behave like ivFailureRegistration("failed matching", IdentityVerificationResult.FailedMatching, "page.iht.iv.failure.failedMatching.title")

    behave like ivFailureRegistration("incomplete", IdentityVerificationResult.Incomplete, "error.problem")

    behave like ivFailureRegistration("insufficient evidence", IdentityVerificationResult.InsufficientEvidence, "error.problem")

    behave like ivFailureRegistration("locked out", IdentityVerificationResult.LockedOut, "page.iht.iv.failure.lockedOut.title")

    behave like ivFailureRegistration("precondition failed", IdentityVerificationResult.PreconditionFailed, "page.iht.iv.failure.preconditionFailed.title")

    behave like ivFailureRegistration("technical issue", IdentityVerificationResult.TechnicalIssue, "page.iht.iv.failure.technicalIssue.title")

    behave like ivFailureRegistration("timeout", IdentityVerificationResult.Timeout, "page.iht.iv.failure.timeout.title")

    behave like ivFailureRegistration("user aborted", IdentityVerificationResult.UserAborted, "page.iht.iv.failure.userAborted.title")
  }
}
