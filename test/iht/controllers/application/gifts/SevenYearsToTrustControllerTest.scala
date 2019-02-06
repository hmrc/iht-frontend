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

package iht.controllers.application.gifts

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import play.api.test.Helpers._
import iht.constants.Constants._
import iht.constants.IhtProperties._
import uk.gov.hmrc.play.partials.FormPartialRetriever

/**
 *
 * Created by Vineet Tyagi on 14/01/16.
 *l
 */
class SevenYearsToTrustControllerTest  extends ApplicationControllerTest{



  def sevenYearsToTrustController = new SevenYearsToTrustController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def sevenYearsToTrustControllerNotAuthorised = new SevenYearsToTrustController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val allGifts=CommonBuilder.buildAllGifts
  val applicationDetails=CommonBuilder.buildApplicationDetails copy (allGifts = Some(allGifts))

  "SevenYearsToTrustController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = sevenYearsToTrustController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = sevenYearsToTrustControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
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

      val result = sevenYearsToTrustController.onPageLoad (createFakeRequest())
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

      val withSevenYearsToTrustValue = CommonBuilder.buildAllGifts.copy(isToTrust = Some(false))

      val filledSevenYearsToTrustForm = giftSevenYearsToTrustForm.fill(withSevenYearsToTrustValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledSevenYearsToTrustForm.data.toSeq: _*)

      val result = sevenYearsToTrustController.onSubmit (request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be (Some(iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad().url + "#" + GiftsSevenYearsQuestionID2))
    }

    "display error if user submit the page without selecting the answer " in {
      createMockToGetRegDetailsFromCache(mockCachingConnector)

      val withSevenYearsToTrustValue = CommonBuilder.buildAllGifts.copy(isToTrust = None)

      val filledSevenYearsToTrustForm = giftSevenYearsToTrustForm.fill(withSevenYearsToTrustValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledSevenYearsToTrustForm.data.toSeq: _*)

      val result = sevenYearsToTrustController.onSubmit()(request)
      status(result) must be (BAD_REQUEST)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      sevenYearsToTrustController.onPageLoad(createFakeRequest()))
  }
}
