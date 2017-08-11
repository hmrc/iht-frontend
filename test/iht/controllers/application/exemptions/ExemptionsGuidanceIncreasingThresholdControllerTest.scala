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

package iht.controllers.application.exemptions

import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.Constants
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import org.mockito.Matchers._
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

/**
 * Created by jon on 23/07/15.
 */

class ExemptionsGuidanceIncreasingThresholdControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  // Create controller object and pass in mock.
  def controller = new ExemptionsGuidanceIncreasingThresholdController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val isWhiteListEnabled = false
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def controllerNotAuthorised = new ExemptionsGuidanceIncreasingThresholdController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val isWhiteListEnabled = false
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val finalDestinationUrl = "url"

  private def setupMocks = {
    val applicationDetails = CommonBuilder.buildApplicationDetails

    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetails),
      regDetails = registrationDetails,
      getAppDetails = true,
      storeAppDetailsInCache = true,
      saveAppDetails = true)

    createMockToGetSingleValueFromCache(mockCachingConnector,
      same(Constants.ExemptionsGuidanceContinueUrlKey), Some(finalDestinationUrl))

  }

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC123")
    )

  "Exemptions Guidance Increasing Threshold Controller" must {

    "redirect to ida login page on page load when user is not logged in" in {
      setupMocks

      val result = controllerNotAuthorised.onPageLoad("anIhtReference")(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "redirect to ida login page on submit when user is not logged in" in {
      setupMocks

      val result = controllerNotAuthorised.onSubmit("")(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "respond with OK on page load and the correct page is loaded" in {
      setupMocks

      val result = controller.onPageLoad("anIhtReference")(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.title"))
    }

    "redirect to whatever page it comes from on submit" in {
      setupMocks

      val result = controller.onSubmit("")(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(finalDestinationUrl))
    }
  }
}
