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

package iht.controllers.application.declaration

import iht.config.AppConfig
import iht.constants.Constants
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import org.mockito.ArgumentMatchers._
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeHeaders
import play.api.test.Helpers.{OK, SEE_OTHER, redirectLocation, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class ProbateApplicationFormDetailsControllerTest extends ApplicationControllerTest {
  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier

  protected abstract class TestController extends FrontendController(mockControllerComponents) with ProbateApplicationFormDetailsController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def probateApplicationFormDetailsController = new TestController{
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    def ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def probateApplicationFormDetailsControllerNotAuthorised = new TestController{
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    def ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    deceasedDateOfDeath=Some(CommonBuilder.buildDeceasedDateOfDeath),
    ihtReference=Some(CommonBuilder.DefaultNino))


  "ProbateApplicationFormDetailsController" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = probateApplicationFormDetailsControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to estate overview of no probate details in cache" in {

      createMockToGetRegDetailsFromCache(mockCachingConnector, Option(registrationDetails))
      createMockToGetProbateDetailsFromCache(mockCachingConnector, None)

      val result = probateApplicationFormDetailsController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url))
    }

    "load the page" in {


      createMockToGetProbateDetailsFromCache(mockCachingConnector)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Option(registrationDetails))

      createMockToStoreSingleValueInCache(
        cachingConnector = mockCachingConnector,
        singleValueFormKey = same(Constants.PDFIHTReference),
        singleValueReturn = CommonBuilder.DefaultIHTReference)

      val result = probateApplicationFormDetailsController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be(OK)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      probateApplicationFormDetailsController.onPageLoad(createFakeRequest(authRetrieveNino = false)))
  }
}
