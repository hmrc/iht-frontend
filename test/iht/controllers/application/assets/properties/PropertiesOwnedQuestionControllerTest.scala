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

package iht.controllers.application.assets.properties

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.controllers.application.assets.routes.AssetsOverviewController
import iht.forms.ApplicationForms._
import iht.models.application.assets.Properties
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import play.api.test.Helpers._
import iht.models.application.ApplicationDetails

class PropertiesOwnedQuestionControllerTest extends ApplicationControllerTest{

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def setUpTests(applicationDetails: Option[ApplicationDetails] = None) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = applicationDetails,
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  def propertiesOwnedQuestionController = new PropertiesOwnedQuestionController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def propertiesOwnedQuestionControllerNotAuthorised = new PropertiesOwnedQuestionController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "PropertiesOwnedQuestionController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = propertiesOwnedQuestionControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = propertiesOwnedQuestionController.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      setUpTests(Some(applicationDetails))

      val result = propertiesOwnedQuestionController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
    }

    "save application and go to Asset Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(properties = Some(Properties(Some(false))))))

      setUpTests(Some(applicationDetails))

      val filledPropertiesForm = propertiesForm.fill(Properties(Some(false)))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPropertiesForm.data.toSeq: _*)

      val result = propertiesOwnedQuestionController.onSubmit (request)
      status(result) shouldBe (SEE_OTHER)
      redirectLocation(result) should be (Some(AssetsOverviewController.onPageLoad().url))
    }

    "respond with bad request when incorrect value are entered on the page" in {
     implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

     createMockToGetExistingRegDetailsFromCache(mockCachingConnector)

      val result = propertiesOwnedQuestionController.onSubmit (fakePostRequest)
      status(result) shouldBe BAD_REQUEST
    }

    "save application and go to Property list page on submit where no assets previously saved" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = None)

      setUpTests(Some(applicationDetails))

      val filledPropertiesForm = propertiesForm.fill(Properties(Some(true)))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPropertiesForm.data.toSeq: _*)

      val result = propertiesOwnedQuestionController.onSubmit (request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be (Some(routes.PropertyDetailsOverviewController.onPageLoad().url))
    }

    "save application and go to Property list page on submit where kickout outstanding" in {
      val property = CommonBuilder.buildProperty.copy(value = Some(10000000))
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(properties = Some(Properties(Some(true))))), propertyList = List(property))

      setUpTests(Some(applicationDetails))

      val filledPropertiesForm = propertiesForm.fill(Properties(Some(true)))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPropertiesForm.data.toSeq: _*)

      val result = propertiesOwnedQuestionController.onSubmit (request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be (Some(iht.controllers.application.routes.KickoutController.onPageLoad().url))
    }
  }

}
