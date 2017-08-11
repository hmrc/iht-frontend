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

package iht.controllers.registration

import iht.connector.CachingConnector
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import iht.utils._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

class CompletedRegistrationControllerTest extends RegistrationControllerTest {
  val requestWithHeaders=FakeRequest().withHeaders(("referer",referrerURL),("host",host))

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  def completedRegistrationController = new CompletedRegistrationController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val isWhiteListEnabled = false
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def completedRegistrationControllerNotAuthorised = new CompletedRegistrationController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val isWhiteListEnabled = false
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val ihtReference = "AB123456"
  // Perform tests.
  "CompletedRegistrationController" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = completedRegistrationControllerNotAuthorised.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val registrationDetails=CommonBuilder.buildRegistrationDetails copy(ihtReference = Some(""))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = completedRegistrationController.onPageLoad()(createFakeRequest())
      status(result) shouldBe(200)
    }

    "respond with correct page" in {
      import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy(ihtReference = Some(""))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      val result = completedRegistrationController.onPageLoad()(createFakeRequest())

      contentAsString(result) should include(messagesApi("iht.registration.complete"))
    }

    "respond with a reference number" in {
      val registrationDetails=CommonBuilder.buildRegistrationDetails copy(ihtReference = Some(ihtReference))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val formattedIhtRef = formattedIHTReference(ihtReference)
      val result = completedRegistrationController.onPageLoad()(createFakeRequest())
      contentAsString(result) should include(formattedIhtRef)
    }

    "respond with not implemented" in {
      val result = completedRegistrationController.onSubmit(createFakeRequest())
      status(result) shouldBe(SEE_OTHER)
      redirectLocation(result) should be(Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url))
    }

    "display the valid content on the page" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy(ihtReference = Some(""))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      val result = completedRegistrationController.onPageLoad()(createFakeRequest())

      contentAsString(result) should include (messagesApi("page.iht.registration.completedRegistration.ref.text"))
      contentAsString(result) should include (messagesApi("page.iht.registration.completedRegistration.p1"))
      contentAsString(result) should include (messagesApi("page.iht.registration.completedRegistration.p2"))
    }

    "respond with redirect to application overview when no registration details found in cache" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, None)
      val result = completedRegistrationController.onPageLoad()(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) shouldBe Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url)
    }

  }
}
