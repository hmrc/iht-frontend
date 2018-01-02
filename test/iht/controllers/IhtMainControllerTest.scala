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
import iht.testhelpers.MockFormPartialRetriever
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

class SessionManagementControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def ihtMainController = new SessionManagementController {
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  implicit val request = FakeRequest()

  "iht main controller" must {
    "sign out correctly" in {

      val result = ihtMainController.signOut()(request)
      status(result) shouldBe OK
    }
  }
}
