/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.MockObjectBuilder.createMockToGetRegDetailsFromCache
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

class IVWayfinderControllerTest extends ApplicationControllerTest {

  val mockCachingConnector: CachingConnector = mock[CachingConnector]

  def ivWayfinderController = new IVWayfinderController {
    val mockIhtConnector: IhtConnector = mock[IhtConnector]
    val cachingConnector: CachingConnector = mockCachingConnector
    val authConnector = createFakeAuthConnector(true)

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  implicit val request = FakeRequest()

  "IV Wayfinder login-pass" must {

    "respond with a 303 when login-pass hasn't got an auth connection" in {
      val result = ivWayfinderController.loginPass(request)

      status(result) shouldBe 303
    }

    "respond with a 200 when login-pass page is served" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy (ihtReference = Some(""))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = ivWayfinderController.loginPass()(createFakeRequest())

      status(result) shouldBe 200
    }

  }

  "IV Wayfinder verification-pass" must {

    "respond with a 303 when verification-pass hasn't got an auth connection" in {
      val result = ivWayfinderController.verificationPass(request)

      status(result) shouldBe 303
    }

    "respond with a 200 when verification-pass page is served" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy (ihtReference = Some(""))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = ivWayfinderController.verificationPass()(createFakeRequest())

      status(result) shouldBe 200
    }

  }
}

