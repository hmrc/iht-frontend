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

package iht.controllers.application.declaration

import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.Constants
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import iht.testhelpers.MockObjectBuilder.{createMockToGetProbateDetailsFromCache, createMockToGetRegDetailsFromCache, createMockToStoreSingleValueInCache}
import org.mockito.ArgumentMatchers._
import play.api.test.FakeHeaders
import play.api.test.Helpers.{OK, SEE_OTHER, redirectLocation}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.partials.FormPartialRetriever
import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.Constants
import iht.controllers.application.ApplicationControllerTest
import iht.controllers.estateReports.YourEstateReportsController
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import iht.testhelpers.MockObjectBuilder._
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.http.HeaderCarrier

class ProbateApplicationFormDetailsControllerTest extends ApplicationControllerTest {
  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier
  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def probateApplicationFormDetailsController = new ProbateApplicationFormDetailsController{
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector()

    def ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def probateApplicationFormDetailsControllerNotAuthorised = new ProbateApplicationFormDetailsController{
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=false)

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
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to estate overview of no probate details in cache" in {

      createMockToGetRegDetailsFromCache(mockCachingConnector, Option(registrationDetails))
      createMockToGetProbateDetailsFromCache(mockCachingConnector, None)

      val result = probateApplicationFormDetailsController.onPageLoad()(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url))
    }

    "load the page" in {


      createMockToGetProbateDetailsFromCache(mockCachingConnector)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Option(registrationDetails))

      createMockToStoreSingleValueInCache(
        cachingConnector = mockCachingConnector,
        singleValueFormKey = same(Constants.PDFIHTReference),
        singleValueReturn = CommonBuilder.DefaultIHTReference)

      val result = probateApplicationFormDetailsController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      probateApplicationFormDetailsController.onPageLoad(createFakeRequest()))
  }
}
