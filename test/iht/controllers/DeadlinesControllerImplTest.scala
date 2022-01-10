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

import iht.connector.CachingConnector
import iht.views.ViewTestHelper
import iht.views.html.{deadlines_application, deadlines_registration}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

class DeadlinesControllerImplTest extends ViewTestHelper {
  val mockCachingConnector = mock[CachingConnector]
  val mockFormPartialRetriever = mock[FormPartialRetriever]
  val mockMessagesControllerComponents = mockControllerComponents
  val mockAppConfig = appConfig
  val fakeRequest = FakeRequest()
  val deadlinesApplicationView: deadlines_application = app.injector.instanceOf[deadlines_application]
  val deadlinesRegistrationView: deadlines_registration = app.injector.instanceOf[deadlines_registration]

  val deadlinesControllerImpl = new DeadlinesControllerImpl(mockCachingConnector, mockMessagesControllerComponents,
    deadlinesApplicationView, deadlinesRegistrationView, mockAppConfig)

  "DeadlinesControllerImpl#onPageLoadReistration" must {
    "serve the deadlines_registration view" in {
      val result = deadlinesControllerImpl.onPageLoadRegistration(fakeRequest)
      status(result) mustBe OK
    }
  }

  "DeadlinesControllerImpl#onPageLoadApplication" must {
    "serve the deadlines_registration view" in {
      val result = deadlinesControllerImpl.onPageLoadApplication(fakeRequest)
      status(result) mustBe OK
    }
  }
}
