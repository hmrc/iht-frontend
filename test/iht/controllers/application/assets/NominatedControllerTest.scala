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

package iht.controllers.application.assets

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._

import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class NominatedControllerTest extends ApplicationControllerTest{

  def nominatedController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def nominatedControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  protected abstract class TestController extends FrontendController(mockControllerComponents) with NominatedController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  "NominatedController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = nominatedControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = nominatedControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
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

      val result = nominatedController.onPageLoad (createFakeRequest())
      status(result) mustBe (OK)
    }

    "save application and go to Asset Overview page on submit when user selects Yes" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(businessInterest = Some(CommonBuilder.buildBasicElement.copy(
        value = Some(20), isOwned = Some(true))))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val nominatedValue = CommonBuilder.buildBasicElement.copy(value=Some(20), isOwned = Some(true))

      val filledNominatedForm = businessInterestForm.fill(nominatedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledNominatedForm.data.toSeq: _*)

      val result = nominatedController.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
    }

    "save application and go to Asset Overview page on submit when user selects No" in {
      val nominatedAsset = CommonBuilder.buildBasicElement.copy(value = Some(20), isOwned = Some(false))

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(nominated = Some(nominatedAsset))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val filledNominatedForm = businessInterestForm.fill(nominatedAsset)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledNominatedForm.data.toSeq: _*)

      val result = nominatedController.onSubmit (request)
      status(result) mustBe (SEE_OTHER)

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val expectedAppDetails = applicationDetails.copy(allAssets = applicationDetails.allAssets.map(_.copy(
        nominated = Some(CommonBuilder.buildBasicElement.copy(value = None, isOwned = Some(false))))))

      capturedValue mustBe expectedAppDetails
    }

    "respond with bad request when incorrect value are entered on the page" in {
     implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = nominatedController.onSubmit (fakePostRequest)
      status(result) mustBe (BAD_REQUEST)
    }

    "save application and go to Asset Overview page on submit where no assets previously saved" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = None)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val nominatedValue = CommonBuilder.buildBasicElement.copy(value=Some(20),  isOwned = Some(true))

      val filledNominatedForm = businessInterestForm.fill(nominatedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledNominatedForm.data.toSeq: _*)

      val result = nominatedController.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      nominatedController.onPageLoad(createFakeRequest()))
  }

}
