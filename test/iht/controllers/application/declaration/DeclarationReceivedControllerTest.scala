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

package iht.controllers.application.declaration

import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.Constants
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import org.mockito.Matchers.same
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier


class DeclarationReceivedControllerTest extends ApplicationControllerTest {
  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier
  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def declarationReceivedController = new DeclarationReceivedController(messagesApi) {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector()
    override val isWhiteListEnabled = false
    def ihtConnector = mockIhtConnector
  }

  def declarationReceivedControllerNotAuthorised = new DeclarationReceivedController(messagesApi) {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val isWhiteListEnabled = false
    def ihtConnector = mockIhtConnector
  }

  "Declaration Received " must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = declarationReceivedControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
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

      val result = declarationReceivedController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      declarationReceivedController.onPageLoad(createFakeRequest()))
  }
}
