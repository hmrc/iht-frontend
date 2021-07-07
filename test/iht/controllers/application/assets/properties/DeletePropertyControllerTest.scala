/*
 * Copyright 2021 HM Revenue & Customs
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
import iht.models.application.ApplicationDetails
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.CommonHelper
import iht.views.html.application.asset.properties.delete_property_confirm
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

/**
 * Created by yasar on 22/06/15.
 */
class DeletePropertyControllerTest extends ApplicationControllerTest {


  // Implicit objects required by play framework.
  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier

  protected abstract class TestController extends FrontendController(mockControllerComponents) with DeletePropertyController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val deletePropertyConfirmView: delete_property_confirm = app.injector.instanceOf[delete_property_confirm]
  }

  def deletePropertyController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector
  }

  def deletePropertyControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector
  }

  "deleteProperty controller" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = deletePropertyControllerNotAuthorised.onPageLoad("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on Submit if the user is not logged in" in {
      val result = deletePropertyControllerNotAuthorised.onSubmit("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val firstProperty = CommonBuilder.property
      val applicationDetails = ApplicationDetails(propertyList = List(firstProperty))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = deletePropertyController.onPageLoad("1")(createFakeRequest())
      status(result) must be (OK)
      contentAsString(result) must include(messagesApi("page.iht.application.propertyDetails.deleteProperty.title"))
    }

    "respond with error if property not found" in {
      val firstProperty = CommonBuilder.property
      val applicationDetails = ApplicationDetails(propertyList = List(firstProperty))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = deletePropertyController.onPageLoad("") (createFakeRequest())
      status(result) must be (INTERNAL_SERVER_ERROR)
    }

    "delete the chosen property successfully and return to exemptions page" in {
      val firstProperty = CommonBuilder.property
      val applicationDetails = ApplicationDetails(propertyList = List(firstProperty))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = deletePropertyController.onSubmit(firstProperty.id.getOrElse("1"))(createFakeRequest())
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.PropertiesOverviewController.onPageLoad().url,TestHelper.AssetsPropertiesAddPropertyID)))
    }

    "respond with error if there is a problem performing the delete, cannot save deletion" in {
      val firstProperty = CommonBuilder.property
      val applicationDetails = ApplicationDetails(propertyList = List(firstProperty))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true,
        saveAppDetailsObject = None)

      val result = deletePropertyController.onSubmit(firstProperty.id.getOrElse("1"))(createFakeRequest())
      status(result) must be (INTERNAL_SERVER_ERROR)
    }

    "intercept RuntimeException if there is a problem performing the delete, cannot load original data" in {
      val firstProperty = CommonBuilder.property
      val applicationDetails = ApplicationDetails(propertyList = List(firstProperty))
     
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true,
        getAppDetailsObject = None)

      intercept[RuntimeException] {
        val result = deletePropertyController.onSubmit(firstProperty.id.getOrElse("1"))(createFakeRequest())
        status(result) must be (INTERNAL_SERVER_ERROR)
      }
    }
  }
}
