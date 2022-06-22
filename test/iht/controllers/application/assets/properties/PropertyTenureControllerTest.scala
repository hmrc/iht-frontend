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
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.CommonHelper
import iht.views.html.application.asset.properties.property_tenure
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class PropertyTenureControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with PropertyTenureController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val propertyTenureView: property_tenure = app.injector.instanceOf[property_tenure]
  }

  def setUpTests(applicationDetails: Option[ApplicationDetails] = None) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = applicationDetails,
      getAppDetails = true,
      saveAppDetails= true,
      storeAppDetailsInCache = true)
  }

  def propertyTenureController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector
  }

  def propertyTenureControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector
  }

  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier

  "PropertyTenure controller" must {

    "redirect to ida login page on PageLoad if the user is not logged in" in {
      val result = propertyTenureControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = propertyTenureControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond ok on page load" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property))

      setUpTests(Some(applicationDetails))
      
      val result = propertyTenureController.onPageLoad(createFakeRequest(authRetrieveNino = false))
      status(result) must be (OK)
    }

    "display the correct title on page" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector)

      val result = propertyTenureController.onPageLoad(createFakeRequest(authRetrieveNino = false))
      status(result) must be (OK)
      contentAsString(result) must include (messagesApi("iht.estateReport.assets.properties.freeholdOrLeasehold"))
    }

    "display the correct title on page in edit mode" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property))

      setUpTests(Some(applicationDetails))

      val result = propertyTenureController.onEditPageLoad("1")(createFakeRequest())
      status(result) must be (OK)
      contentAsString(result) must include (messagesApi("iht.estateReport.assets.properties.freeholdOrLeasehold"))
    }

    "respond with RuntimeException on edit page load if propery is not found in propertyList" in {
      val id: String = "1"
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList =
                                                                    List(CommonBuilder.property.copy(id = Some(id))))
      setUpTests(Some(applicationDetails))

      intercept[RuntimeException] {
        await(propertyTenureController.onEditPageLoad("2")(createFakeRequest()))
      }
    }

    "respond with Internal_Server_Error in Edit mode when applicationDetails could not be retrieved" in {
      setUpTests()

      val result = propertyTenureController.onEditPageLoad("1")(createFakeRequest())
      status(result) must be (INTERNAL_SERVER_ERROR)
    }

    "respond with BAD_RQUEST on submit when request is malformed" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.copy(propertyList = List())
      val formFill = propertyTenureForm.fill(CommonBuilder.buildProperty.copy(tenure = None))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      setUpTests(Some(applicationDetails))

      val result = propertyTenureController.onSubmit()(request)
      status(result) must be (BAD_REQUEST)
    }

    "redirect to PropertyDetails overview page on submit" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List())

      val formFill = propertyTenureForm.fill(CommonBuilder.buildProperty.copy(tenure = TestHelper.TenureFreehold))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      setUpTests(Some(applicationDetails))

      val result = propertyTenureController.onSubmit()(request)

      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(iht.controllers.application.assets.properties.routes.PropertyDetailsOverviewController.onEditPageLoad("1").url, TestHelper.AssetsPropertiesTenureID)))
    }

    "redirect to PropertyDetails overview page on submit in edit mode" in {
      val propertyId = "1"

      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.buildProperty.copy(id = Some(propertyId),
          tenure = TestHelper.TenureFreehold,
          value = Some(1234))))

      val formFill = propertyTenureForm.fill(CommonBuilder.buildProperty.copy(tenure = TestHelper.TenureFreehold))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      setUpTests(Some(applicationDetails))

      val result = propertyTenureController.onEditSubmit(propertyId)(request)

      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(iht.controllers.application.assets.properties.routes.PropertyDetailsOverviewController.onEditPageLoad(propertyId).url, TestHelper.AssetsPropertiesTenureID)))
    }
  }
}
