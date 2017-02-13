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

package iht.controllers.application.gifts

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.testhelpers.CommonBuilder
import iht.testhelpers.CommonBuilder._
import iht.testhelpers.MockObjectBuilder._
import iht.utils.CommonHelper
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._

/**
 * Created by Vineet Tyagi on 14/01/16.
 */

class GivenAwayControllerTest  extends ApplicationControllerTest{

  val mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  val allGifts=CommonBuilder.buildAllGifts
  val applicationDetails=CommonBuilder.buildApplicationDetails copy (allGifts = Some(allGifts))
  val regDetails = CommonBuilder.buildRegistrationDetails copy (
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

  def givenAwayController = new GivenAwayController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def givenAwayControllerNotAuthorised = new GivenAwayController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  "GivenAwayController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = givenAwayController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = givenAwayControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      setUpMocks(applicationDetails)
      val result = givenAwayController.onPageLoad (createFakeRequest())
      status(result) shouldBe OK
    }

    "display the guidance on the page" in {
      implicit val messages: Messages = app.injector.instanceOf[Messages]
      val applicationDetails = CommonBuilder.buildApplicationDetails

      setUpMocks(applicationDetails)

      val result = givenAwayController.onPageLoad (createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include (Messages("page.iht.application.gifts.lastYears.givenAway.p2",
                                                          CommonHelper.getDeceasedNameOrDefaultString(regDetails)))
    }

    "save application and go to Gifts Overview page on submit if answered Yes" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
                                          allGifts= Some(CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true))))

      setUpMocks(applicationDetails)
      val withGivenAwayValue = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true))

      val filledGivenAwayForm = giftsGivenAwayForm.fill(withGivenAwayValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledGivenAwayForm.data.toSeq: _*)

      val result = givenAwayController.onSubmit (request)
      status(result) shouldBe SEE_OTHER
    }

    "save application and go to Gifts Overview page on submit if answered No" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
                                            allGifts= Some(CommonBuilder.buildAllGifts.copy(isGivenAway = Some(false))))

      setUpMocks(applicationDetails)
      val withGivenAwayValue = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true))

      val filledGivenAwayForm = giftsGivenAwayForm.fill(withGivenAwayValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledGivenAwayForm.data.toSeq: _*)

      val result = givenAwayController.onSubmit (request)
      status(result) shouldBe SEE_OTHER
    }

    "save application, reset 7 years gifts values to none and go to Gifts Overview page on submit if answered No" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
                                            allGifts= Some(CommonBuilder.buildAllGifts.copy(isGivenAway = Some(false),
                                              isReservation = Some(false), isToTrust = Some(false),
                                              isGivenInLast7Years = Some(false))),
                                            giftsList = CommonBuilder.buildGiftsList)

      setUpMocks(applicationDetails)
      val withGivenAwayValue = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(false))

      val filledGivenAwayForm = giftsGivenAwayForm.fill(withGivenAwayValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledGivenAwayForm.data.toSeq: _*)

      val result = givenAwayController.onSubmit (request)
      status(result) shouldBe SEE_OTHER

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val expectedAppDetails = applicationDetails.copy(giftsList = None)

      capturedValue shouldBe expectedAppDetails
    }

    "display error if user submit the page without selecting the answer " in {

      val withGivenAwayValue = CommonBuilder.buildAllGifts.copy(isGivenAway = None)

      val filledGivenAwayForm = giftsGivenAwayForm.fill(withGivenAwayValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledGivenAwayForm.data.toSeq: _*)

      val result = givenAwayController.onSubmit()(request)
      status(result) should be (BAD_REQUEST)
    }
  }
}
