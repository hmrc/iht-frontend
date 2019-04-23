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

package iht.controllers.application.declaration

import iht.config.AppConfig
import iht.constants.Constants
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import org.mockito.ArgumentMatchers._
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever


class DeclarationReceivedControllerTest extends ApplicationControllerTest {
  implicit val headeDeclarationReceivedControllerrCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier

  protected abstract class TestController extends FrontendController(mockControllerComponents) with DeclarationReceivedController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def declarationReceivedController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    def ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def declarationReceivedControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    def ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "Declaration Received " must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = declarationReceivedControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "load the page" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
        deceasedDateOfDeath=Some(CommonBuilder.buildDeceasedDateOfDeath),
        ihtReference=Some(CommonBuilder.DefaultNino))

      createMockToGetProbateDetailsFromCache(mockCachingConnector)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Option(registrationDetails))

      createMockToStoreSingleValueInCache(
        cachingConnector = mockCachingConnector,
        singleValueFormKey = same(Constants.PDFIHTReference),
        singleValueReturn = CommonBuilder.DefaultIHTReference)

      val result = declarationReceivedController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be(OK)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      declarationReceivedController.onPageLoad(createFakeRequest(authRetrieveNino = false)))
  }
}
