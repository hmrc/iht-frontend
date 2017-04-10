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

package iht.controllers.application.assets.pensions

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._

/**
 * Created by jennygj on 12/07/16.
 */

class PensionsOwnedQuestionControllerTest extends ApplicationControllerTest{

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def pensionsOwnedQuestionController = new PensionsOwnedQuestionController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def pensionsOwnedQuestionNotAuthorised = new PensionsOwnedQuestionController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "PensionsOwnedQuestionController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = pensionsOwnedQuestionNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = pensionsOwnedQuestionNotAuthorised.onSubmit(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = pensionsOwnedQuestionController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
    }

    "save application and go to Private Pensions Overview page when user selects yes and submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(privatePension = Some(CommonBuilder.buildPrivatePensionExtended.copy(isOwned = Some(true))))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val privatePensionOwned = CommonBuilder.buildPrivatePensionExtended.copy(isOwned = Some(true))

      val filledPrivatePensionForm = privatePensionForm.fill(privatePensionOwned)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPrivatePensionForm.data
        .toSeq: _*)

      val result = pensionsOwnedQuestionController.onSubmit(request)
      status(result) shouldBe (SEE_OTHER)
    }

    "save application and go to Assets Overview page when user selects no and submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = None)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val privatePensionOwned = CommonBuilder.buildPrivatePensionExtended.copy(isOwned = Some(false))

      val filledPrivatePensionForm = privatePensionForm.fill(privatePensionOwned)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPrivatePensionForm.data
        .toSeq: _*)

      val result = pensionsOwnedQuestionController.onSubmit(request)
      status(result) shouldBe (SEE_OTHER)
      redirectLocation(result) shouldBe
        Some(iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad().url)
    }

    "display validation message when incomplete form is submitted" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(privatePension = Some(CommonBuilder.buildPrivatePensionExtended.copy(isOwned = None)))))

      val formFill = privatePensionForm.fill(CommonBuilder.buildPrivatePensionExtended)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = pensionsOwnedQuestionController.onSubmit()(request)
      status(result) should be (BAD_REQUEST)
      contentAsString(result) should include (messagesApi("error.problem"))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      pensionsOwnedQuestionController.onPageLoad(createFakeRequest()))
  }
}
