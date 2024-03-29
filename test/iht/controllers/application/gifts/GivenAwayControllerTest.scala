/*
 * Copyright 2022 HM Revenue & Customs
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
import iht.models.application.ApplicationDetails
import iht.testhelpers.CommonBuilder
import iht.testhelpers.CommonBuilder._
import iht.views.html.application.gift.given_away
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class GivenAwayControllerTest extends ApplicationControllerTest {

  val allGifts = CommonBuilder.buildAllGifts
  val applicationDetails = CommonBuilder.buildApplicationDetails copy (allGifts = Some(allGifts))
  val regDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(buildDeceasedDetails),
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
    ihtReference = Some("AbC123"))

  def setUpMocks(appDetails: ApplicationDetails) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      regDetails = regDetails,
      appDetails = Some(appDetails),
      getAppDetails = true,
      storeAppDetailsInCache = true,
      saveAppDetails = true)
  }

  protected abstract class TestController extends FrontendController(mockControllerComponents) with GivenAwayController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val givenAwayView: given_away = app.injector.instanceOf[given_away]
  }

  def givenAwayController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def givenAwayControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }


  "GivenAwayController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = givenAwayController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = givenAwayControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      setUpMocks(applicationDetails)
      val result = givenAwayController.onPageLoad(createFakeRequest())
      status(result) mustBe OK
    }

    "save application and go to Gifts Overview page on submit if answered Yes" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allGifts = Some(CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true))))

      setUpMocks(applicationDetails)
      val withGivenAwayValue = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true))

      val filledGivenAwayForm = giftsGivenAwayForm.fill(withGivenAwayValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledGivenAwayForm.data.toSeq: _*).withMethod("POST")

      val result = givenAwayController.onSubmit(request)
      status(result) mustBe SEE_OTHER
    }

    "save application and go to Gifts Overview page on submit if answered No" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allGifts = Some(CommonBuilder.buildAllGifts.copy(isGivenAway = Some(false))))

      setUpMocks(applicationDetails)
      val withGivenAwayValue = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true))

      val filledGivenAwayForm = giftsGivenAwayForm.fill(withGivenAwayValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledGivenAwayForm.data.toSeq: _*).withMethod("POST")

      val result = givenAwayController.onSubmit(request)
      status(result) mustBe SEE_OTHER
    }

    "save application, reset 7 years gifts values to none and go to Gifts Overview page on submit if answered No" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allGifts = Some(CommonBuilder.buildAllGifts.copy(isGivenAway = Some(false),
          isReservation = Some(false), isToTrust = Some(false),
          isGivenInLast7Years = Some(false))),
        giftsList = Some(CommonBuilder.buildGiftsList))

      setUpMocks(applicationDetails)
      val withGivenAwayValue = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(false))

      val filledGivenAwayForm = giftsGivenAwayForm.fill(withGivenAwayValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledGivenAwayForm.data.toSeq: _*).withMethod("POST")

      val result = givenAwayController.onSubmit(request)
      status(result) mustBe SEE_OTHER

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val expectedAppDetails = applicationDetails.copy(giftsList = None)

      capturedValue mustBe expectedAppDetails
    }

    "display error if user submit the page without selecting the answer " in {

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allGifts = Some(CommonBuilder.buildAllGifts.copy(isGivenAway = Some(false),
          isReservation = Some(false), isToTrust = Some(false),
          isGivenInLast7Years = Some(false))),
        giftsList = Some(CommonBuilder.buildGiftsList))

      setUpMocks(applicationDetails)

      val withGivenAwayValue = CommonBuilder.buildAllGifts.copy(isGivenAway = None)

      val filledGivenAwayForm = giftsGivenAwayForm.fill(withGivenAwayValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledGivenAwayForm.data.toSeq: _*).withMethod("POST")

      val result = givenAwayController.onSubmit()(request)
      status(result) must be(BAD_REQUEST)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      givenAwayController.onPageLoad(createFakeRequest()))
  }
}
