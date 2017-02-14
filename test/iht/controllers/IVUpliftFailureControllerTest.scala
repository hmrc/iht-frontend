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

  "IVUpliftFailureController" must {
    "go to 2fa failure page if no journey id" in {
      val result = controller.showNotAuthorisedApplication(None)(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(Messages("page.iht.iv.failure.2fa.title"))
    }

    "Go to the failed matching page when the appropriate IV verification result is received" in {
      when(mockIdentityVerificationConnector.identityVerificationResponse(any())(any()))
          .thenReturn(Future.successful(IdentityVerificationResult.FailedMatching))
      val result = controller.showNotAuthorisedApplication(Some(""))(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(Messages("page.iht.iv.failure.failedMatching.title"))
    }
  }
}
