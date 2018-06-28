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

package iht.controllers.application.tnrb

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import iht.testhelpers.MockObjectBuilder._
import play.api.i18n.MessagesApi
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

/**
 * Created by Vineet Tyagi on 21/04/15.
 *
 * Test Class for iht.controllers.application.TnrbEligibiltyController
 *
 */
class TnrbSuccessControllerTest extends ApplicationControllerTest {

  // Implicit objects required by play framework.

  implicit val headerCarrier = FakeHeaders()
  implicit val request = FakeRequest()
  implicit val hc = new HeaderCarrier

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    deceasedDateOfDeath=Some(CommonBuilder.buildDeceasedDateOfDeath),
      ihtReference=Some("AI123456")
    )

  // Mock the CachingConnector
  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def tnrbSuccessController = new TnrbSuccessController {
    override val cachingConnector = mockCachingConnector
	  override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=true)

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def tnrbSuccessControllerNotAuthorised = new TnrbSuccessController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=false)

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "TnrbSuccessController" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = tnrbSuccessControllerNotAuthorised.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val buildWidowCheck = CommonBuilder.buildWidowedCheck
      val buildTnrbModel = CommonBuilder.buildTnrbEligibility
      val applicationDetails = CommonBuilder.buildApplicationDetails copy (widowCheck= Some(buildWidowCheck),
                                increaseIhtThreshold = Some(buildTnrbModel))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetApplicationDetails(mockIhtConnector, Some(applicationDetails))

      val result = tnrbSuccessController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      tnrbSuccessController.onPageLoad(createFakeRequest()))
  }
}
