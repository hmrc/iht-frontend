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

package iht.controllers.registration

import iht.config.AppConfig
import iht.connector.CachingConnector
import iht.testhelpers.MockFormPartialRetriever
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class DuplicateRegistrationControllerTest extends RegistrationControllerTest{
  // Create controller object and pass in mock.

  protected abstract class TestController extends FrontendController(mockControllerComponents) with DuplicateRegistrationController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def duplicateRegistrationController = new TestController {
    override val authConnector = mockAuthConnector

    override val cachingConnector = mock[CachingConnector]
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def duplicateRegistrationControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector

    override val cachingConnector = mock[CachingConnector]
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val ihtReference = "XX121212"
  // Perform tests.
  "DuplicateRegistrationController" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = duplicateRegistrationControllerNotAuthorised.onPageLoad(ihtReference)(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val result = duplicateRegistrationController.onPageLoad(ihtReference)(createFakeRequest(authRetrieveNino = false))
      status(result) mustBe(200)
    }

    "respond with correct page" in {
      val result = duplicateRegistrationController.onPageLoad(ihtReference)(createFakeRequest(authRetrieveNino = false))
      contentAsString(result) must include(messagesApi("page.iht.registration.duplicateRegistration.title"))
    }

    "respond with a reference number" in {
      duplicateRegistrationController.onPageLoad(ihtReference)(createFakeRequest(authRetrieveNino = false))
    }

    "respond with not implemented" in {
      val result = duplicateRegistrationController.onSubmit(createFakeRequest(authRetrieveNino = false))
      status(result) mustBe(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url))
    }
  }
}
