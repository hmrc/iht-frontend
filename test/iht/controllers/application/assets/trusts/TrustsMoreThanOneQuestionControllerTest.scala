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

package iht.controllers.application.assets.trusts

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.assets.HeldInTrust
import iht.testhelpers.TestHelper._
import iht.testhelpers.{CommonBuilder, ContentChecker}
import iht.utils.CommonHelper
import iht.views.html.application.asset.trusts.trusts_more_than_one_question
import play.api.i18n.{Lang, Messages}
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class TrustsMoreThanOneQuestionControllerTest extends ApplicationControllerTest{
  override implicit val messages: Messages = mockControllerComponents.messagesApi.preferred(Seq(Lang.defaultLang)).messages
  protected abstract class TestController extends FrontendController(mockControllerComponents) with TrustsMoreThanOneQuestionController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val trustsMoreThanOneQuestionView: trusts_more_than_one_question = app.injector.instanceOf[trusts_more_than_one_question]
  }

  def trustsMoreThanOneQuestionController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def trustsMoreThanOneQuestionControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
//    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "TrustsMoreThanOneQuestionController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = trustsMoreThanOneQuestionControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = trustsMoreThanOneQuestionController.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      val regDetails = CommonBuilder.buildRegistrationDetails1
      val deceasedName = regDetails.deceasedDetails.map(_.name).fold("")(identity)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = regDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = trustsMoreThanOneQuestionController.onPageLoad (createFakeRequest())
      status(result) mustBe (OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include (messagesApi("iht.estateReport.assets.trusts.moreThanOne.question", deceasedName))
    }

    "save application and go to held in trust overview page on submit when user selects No" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val filledHeldInTrustForm = trustsMoreThanOneQuestionForm.fill(HeldInTrust(Some(false), None, None))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledHeldInTrustForm.data.toSeq: _*).withMethod("POST")

      val result = trustsMoreThanOneQuestionController.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.TrustsOverviewController.onPageLoad.url, AssetsTrustsMultipleID)))
    }

    "save application and go to held in trust overview page on submit when user selects No and " +
      "there is no other assets " in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = None)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val filledHeldInTrustForm = trustsMoreThanOneQuestionForm.fill(HeldInTrust(Some(true), None, None))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledHeldInTrustForm.data.toSeq: _*).withMethod("POST")

      val result = trustsMoreThanOneQuestionController.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be (Some(iht.controllers.application.routes.KickoutAppController.onPageLoad.url))
    }


    "save application and go to kick out page on submit  when user selects Yes" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val filledHeldInTrustForm = trustsMoreThanOneQuestionForm.fill(HeldInTrust(Some(true), None, None))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledHeldInTrustForm.data.toSeq: _*).withMethod("POST")

      val result = trustsMoreThanOneQuestionController.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be (Some(iht.controllers.application.routes.KickoutAppController.onPageLoad.url))
    }

    "respond with bad request when incorrect value are entered on the page" in {
     implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr")).withMethod("POST")

     createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = trustsMoreThanOneQuestionController.onSubmit (fakePostRequest)
      status(result) mustBe (BAD_REQUEST)
    }

    "respond with bad request and correct error message when no answer is selected" in {
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("", "")).withMethod("POST")

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = trustsMoreThanOneQuestionController.onSubmit (fakePostRequest)
      status(result) mustBe (BAD_REQUEST)
      contentAsString(result) must include(messagesApi("error.assets.heldInTrust.moreThanOne.select",
        CommonBuilder.buildDeceasedDetails.name))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      trustsMoreThanOneQuestionController.onPageLoad(createFakeRequest()))

  }
}
