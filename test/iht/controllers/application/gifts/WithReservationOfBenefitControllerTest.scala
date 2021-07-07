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

package iht.controllers.application.gifts

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.testhelpers.CommonBuilder
import iht.views.html.application.gift.with_reservation_of_benefit
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController


class WithReservationOfBenefitControllerTest  extends ApplicationControllerTest{

  protected abstract class TestController extends FrontendController(mockControllerComponents) with WithReservationOfBenefitController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val withReservationOfBenefitView: with_reservation_of_benefit = app.injector.instanceOf[with_reservation_of_benefit]
  }

  def withReservationOfBenefitController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def withReservationOfBenefitControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  val allGifts=CommonBuilder.buildAllGifts
  val applicationDetails=CommonBuilder.buildApplicationDetails copy (allGifts = Some(allGifts))

  "WithReservationOfBenefitController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = withReservationOfBenefitController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = withReservationOfBenefitControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        storeAppDetailsInCache = true,
        saveAppDetails = true)

      val result = withReservationOfBenefitController.onPageLoad (createFakeRequest())
      status(result) mustBe OK
    }

    "save application and go to Gifts Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allGifts= Some(CommonBuilder.buildAllGifts))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        storeAppDetailsInCache = true,
        saveAppDetails = true)

      val withReservationValue = CommonBuilder.buildAllGifts.copy(isReservation = Some(false))

      val filledBusinessInterestsForm = giftWithReservationFromBenefitForm.fill(withReservationValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledBusinessInterestsForm.data.toSeq: _*)

      val result = withReservationOfBenefitController.onSubmit (request)
      status(result) mustBe SEE_OTHER
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      withReservationOfBenefitController.onPageLoad(createFakeRequest()))
  }
}
