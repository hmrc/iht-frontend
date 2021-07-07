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

package iht.controllers.application.tnrb

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import iht.views.html.application.tnrb.tnrb_success
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class TnrbSuccessControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with TnrbSuccessController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit lazy val appConfig: AppConfig = mockAppConfig
    override val tnrbSuccessView: tnrb_success = app.injector.instanceOf[tnrb_success]
  }

  implicit val headerCarrier = FakeHeaders()
  implicit val request = FakeRequest()
  implicit val hc = new HeaderCarrier

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    deceasedDateOfDeath=Some(CommonBuilder.buildDeceasedDateOfDeath),
      ihtReference=Some("AI123456")
    )

  def tnrbSuccessController = new TestController {
    override val cachingConnector = mockCachingConnector
	  override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector
  }

  def tnrbSuccessControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector
  }

  "TnrbSuccessController" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = tnrbSuccessControllerNotAuthorised.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val buildWidowCheck = CommonBuilder.buildWidowedCheck
      val buildTnrbModel = CommonBuilder.buildTnrbEligibility
      val applicationDetails = CommonBuilder.buildApplicationDetails copy (widowCheck= Some(buildWidowCheck),
                                increaseIhtThreshold = Some(buildTnrbModel))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetApplicationDetails(mockIhtConnector, Some(applicationDetails))

      val result = tnrbSuccessController.onPageLoad()(createFakeRequest())
      status(result) must be (OK)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      tnrbSuccessController.onPageLoad(createFakeRequest()))
  }
}
