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

package iht.controllers.application.assets.trusts

import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.assets.HeldInTrust
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.TestHelper._
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import iht.utils.CommonHelper
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

class TrustsOwnedQuestionControllerTest extends ApplicationControllerTest{




  def trustsOwnedQuestionController = new TrustsOwnedQuestionController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def trustsOwnedQuestionControllerNotAuthorised = new TrustsOwnedQuestionController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "HeldInTrustQuestionController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = trustsOwnedQuestionControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = trustsOwnedQuestionController.onSubmit(createFakeRequest(isAuthorised = false))
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

      val result = trustsOwnedQuestionController.onPageLoad (createFakeRequest())
      status(result) mustBe (OK)
    }

    "save application and go to held in trust overview page on submit when No chosen" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val filledHeldInTrustForm = trustsOwnedQuestionForm.fill(HeldInTrust(None, None, Some(false)))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledHeldInTrustForm.data.toSeq: _*)

      val result = trustsOwnedQuestionController.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
      //redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.AssetsOverviewController.onPageLoad().url, AppSectionHeldInTrustID)))
    }

    "save application and go to held in trust next page page on submit when Yes chosen" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val filledHeldInTrustForm = trustsOwnedQuestionForm.fill(HeldInTrust(None, None, Some(true)))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledHeldInTrustForm.data.toSeq: _*)

      val result = trustsOwnedQuestionController.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.TrustsOverviewController.onPageLoad.url, AssetsTrustsBenefitedID)))
    }

    "respond with bad request when incorrect value are entered on the page" in {
     implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

     createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = trustsOwnedQuestionController.onSubmit (fakePostRequest)
      status(result) mustBe (BAD_REQUEST)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      trustsOwnedQuestionController.onPageLoad(createFakeRequest()))
  }
}
