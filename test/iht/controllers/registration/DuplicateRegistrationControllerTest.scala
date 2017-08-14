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
import iht.testhelpers.MockFormPartialRetriever
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

class DuplicateRegistrationControllerTest extends RegistrationControllerTest{
  // Create controller object and pass in mock.
  def duplicateRegistrationController = new DuplicateRegistrationController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)

    override val cachingConnector = mock[CachingConnector]
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def duplicateRegistrationControllerNotAuthorised = new DuplicateRegistrationController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)

    override val cachingConnector = mock[CachingConnector]
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val ihtReference = "XX121212"
  // Perform tests.
  "DuplicateRegistrationController" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = duplicateRegistrationControllerNotAuthorised.onPageLoad(ihtReference)(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val result = duplicateRegistrationController.onPageLoad(ihtReference)(createFakeRequest())
      status(result) shouldBe(200)
    }

    "respond with correct page" in {
      import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
      val result = duplicateRegistrationController.onPageLoad(ihtReference)(createFakeRequest())
      contentAsString(result) should include(messagesApi("page.iht.registration.duplicateRegistration.title"))
    }

    "respond with a reference number" in {
      val result = duplicateRegistrationController.onPageLoad(ihtReference)(createFakeRequest())
    }

    "respond with not implemented" in {
      val result = duplicateRegistrationController.onSubmit(createFakeRequest())
      status(result) shouldBe(SEE_OTHER)
      redirectLocation(result) should be(Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url))
    }
  }
}
