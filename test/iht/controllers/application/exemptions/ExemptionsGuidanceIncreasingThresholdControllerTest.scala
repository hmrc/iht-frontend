/*
 * Copyright 2020 HM Revenue & Customs
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

import iht.config.AppConfig
import iht.constants.Constants
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import org.mockito.ArgumentMatchers._
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

/**
 * Created by jon on 23/07/15.
 */

class ExemptionsGuidanceIncreasingThresholdControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with ExemptionsGuidanceIncreasingThresholdController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  // Create controller object and pass in mock.
  def controller = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def controllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector

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

      val result = controllerNotAuthorised.onPageLoad("anIhtReference")(createFakeRequest(isAuthorised = false, authRetrieveNino = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "redirect to ida login page on submit when user is not logged in" in {
      setupMocks

      val result = controllerNotAuthorised.onSubmit("")(createFakeRequest(isAuthorised = false, authRetrieveNino = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "respond with OK on page load and the correct page is loaded" in {
      setupMocks

      val result = controller.onPageLoad("anIhtReference")(createFakeRequest(authRetrieveNino = false))
      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("page.iht.application.exemptions.guidance.increasing.threshold.title"))
    }

    "redirect to whatever page it comes from on submit" in {
      setupMocks

      val result = controller.onSubmit("")(createFakeRequest(authRetrieveNino = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(finalDestinationUrl))
    }
  }
}
