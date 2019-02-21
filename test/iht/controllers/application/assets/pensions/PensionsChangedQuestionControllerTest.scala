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

package iht.controllers.application.assets.pensions

import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

class PensionsChangedQuestionControllerTest extends ApplicationControllerTest{

  def pensionsChangedQuestionController = new PensionsChangedQuestionController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def pensionsChangedQuestionControllerNotAuthorised = new PensionsChangedQuestionController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "PensionsChangedQuestionController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = pensionsChangedQuestionControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = pensionsChangedQuestionController.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = pensionsChangedQuestionController.onPageLoad (createFakeRequest())
      status(result) mustBe (OK)
    }

    "save application and go to Pensions overview page on submit when No chosen" in {
      val privatePension = CommonBuilder.buildPrivatePensionExtended.copy(isChanged = Some(false))
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(privatePension = Some(privatePension))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val filledPensionsChangedQuestionForm = pensionsChangedQuestionForm.fill(privatePension)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPensionsChangedQuestionForm.data.toSeq: _*)

      val result = pensionsChangedQuestionController.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be
          (Some(routes.PensionsOverviewController.onPageLoad().url))
    }

    "save application and go to Kick out page on submit when Yes chosen" in {
      val privatePension = CommonBuilder.buildPrivatePensionExtended.copy(isChanged = Some(true))
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(privatePension = Some(privatePension))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val filledPensionsChangedQuestionForm = pensionsChangedQuestionForm.fill(privatePension)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPensionsChangedQuestionForm.data.toSeq: _*)

      val result = pensionsChangedQuestionController.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be (Some(iht.controllers.application.routes.KickoutAppController.onPageLoad().url))
    }

    "respond with bad request when incorrect value are entered on the page" in {
     implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

     createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = pensionsChangedQuestionController.onSubmit (fakePostRequest)
      status(result) mustBe (BAD_REQUEST)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      pensionsChangedQuestionController.onPageLoad(createFakeRequest()))
  }
}
