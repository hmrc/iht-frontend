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

package iht.controllers.application.assets.properties

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.Properties
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.CommonHelper
import iht.views.html.application.asset.properties.properties_owned_question
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class PropertiesOwnedQuestionControllerTest extends ApplicationControllerTest{

  protected abstract class TestController extends FrontendController(mockControllerComponents) with PropertiesOwnedQuestionController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val propertiesOwnedQuestionView: properties_owned_question = app.injector.instanceOf[properties_owned_question]
  }

  def setUpTests(applicationDetails: Option[ApplicationDetails] = None) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = applicationDetails,
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  def propertiesOwnedQuestionController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def propertiesOwnedQuestionControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
//    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "PropertiesOwnedQuestionController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = propertiesOwnedQuestionControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = propertiesOwnedQuestionController.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      setUpTests(Some(applicationDetails))

      val result = propertiesOwnedQuestionController.onPageLoad (createFakeRequest())
      status(result) mustBe (OK)
    }

    "save application and go to Asset Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(properties = Some(Properties(Some(false))))))

      setUpTests(Some(applicationDetails))

      val filledPropertiesForm = propertiesForm.fill(Properties(Some(false)))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPropertiesForm.data.toSeq: _*).withMethod("POST")

      val result = propertiesOwnedQuestionController.onSubmit (request)
      status(result) mustBe (SEE_OTHER)

      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad.url,TestHelper.AppSectionPropertiesID))
      )
    }

    "respond with bad request when incorrect value are entered on the page" in {
     implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr")).withMethod("POST")

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
      createMockToGetApplicationDetails(mockIhtConnector)

      val result = propertiesOwnedQuestionController.onSubmit (fakePostRequest)
      status(result) mustBe BAD_REQUEST
    }

    "save application and go to Property list page on submit where no assets previously saved" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = None)

      setUpTests(Some(applicationDetails))

      val filledPropertiesForm = propertiesForm.fill(Properties(Some(true)))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPropertiesForm.data.toSeq: _*).withMethod("POST")

      val result = propertiesOwnedQuestionController.onSubmit (request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be (Some(routes.PropertyDetailsOverviewController.onPageLoad.url))
    }

    "save application and go to Property list page on submit where kickout outstanding" in {
      val property = CommonBuilder.buildProperty.copy(value = Some(10000000))
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(properties = Some(Properties(Some(true))))), propertyList = List(property))

      setUpTests(Some(applicationDetails))

      val filledPropertiesForm = propertiesForm.fill(Properties(Some(true)))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPropertiesForm.data.toSeq: _*).withMethod("POST")

      val result = propertiesOwnedQuestionController.onSubmit (request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be (Some(iht.controllers.application.routes.KickoutAppController.onPageLoad.url))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      propertiesOwnedQuestionController.onPageLoad(createFakeRequest()))
  }

}
