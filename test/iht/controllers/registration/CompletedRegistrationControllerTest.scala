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

package iht.controllers.registration

import iht.config.AppConfig
import iht.controllers.application.exemptions.qualifyingBody.QualifyingBodyDeleteConfirmController

import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import iht.utils._
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class CompletedRegistrationControllerTest extends RegistrationControllerTest {
  val requestWithHeaders=FakeRequest().withHeaders(("referer",referrerURL),("host",host))

  protected abstract class TestController extends FrontendController(mockControllerComponents) with CompletedRegistrationController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def completedRegistrationController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def completedRegistrationControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val ihtReference = "AB123456"
  // Perform tests.
  "CompletedRegistrationController" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = completedRegistrationControllerNotAuthorised.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val registrationDetails=CommonBuilder.buildRegistrationDetails copy(ihtReference = Some(""))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = completedRegistrationController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) mustBe(200)
    }

    "respond with correct page" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy(ihtReference = Some(""))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      val result = completedRegistrationController.onPageLoad()(createFakeRequest(authRetrieveNino = false))

      contentAsString(result) must include(messagesApi("iht.registration.complete"))
    }

    "respond with a reference number" in {
      val registrationDetails=CommonBuilder.buildRegistrationDetails copy(ihtReference = Some(ihtReference))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val formattedIhtRef = formattedIHTReference(ihtReference)
      val result = completedRegistrationController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      contentAsString(result) must include(formattedIhtRef)
    }

    "respond with not implemented" in {
      val result = completedRegistrationController.onSubmit(createFakeRequest(authRetrieveNino = false))
      status(result) mustBe(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url))
    }

    "display the valid content on the page" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy(ihtReference = Some(""))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      val result = completedRegistrationController.onPageLoad()(createFakeRequest(authRetrieveNino = false))

      contentAsString(result) must include (messagesApi("page.iht.registration.completedRegistration.ref.text"))
      contentAsString(result) must include (messagesApi("page.iht.registration.completedRegistration.p1"))
      contentAsString(result) must include (messagesApi("page.iht.registration.completedRegistration.p2"))
    }

    "respond with redirect to application overview when no registration details found in cache" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, None)
      val result = completedRegistrationController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) mustBe Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url)
    }

    "redirect to estate overview when IHT ref is equal to None" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy(ihtReference = None)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      val result = completedRegistrationController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) mustBe Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url)
    }

  }
}
