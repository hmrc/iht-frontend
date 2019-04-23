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

package iht.controllers.application.assets.properties

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.controllers.application.exemptions.ExemptionsGuidanceController
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails

import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever, TestHelper}
import iht.utils.CommonHelper
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by jennygj on 17/06/16.
 */
class PropertyTypeControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with PropertyTypeController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def setUpTests(applicationDetails: Option[ApplicationDetails] = None) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = applicationDetails,
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  def propertyTypeController = new TestController {
    val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def propertyTypeControllerNotAuthorised = new TestController {
    val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "PropertyTypeController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = propertyTypeControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = propertyTypeControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      setUpTests(Some(applicationDetails))

      val result = propertyTypeController.onPageLoad(createFakeRequest(authRetrieveNino = false))
      status(result) must be(OK)
    }

    "display the correct title on page load" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector)

      val result = propertyTypeController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be (OK)
      contentAsString(result) must include (messagesApi("iht.estateReport.assets.properties.whatKind.question"))
    }

    "respond with OK on edit page load" in {
      val id: String = "1"
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List(CommonBuilder.property))
      setUpTests(Some(applicationDetails))

      val result = propertyTypeController.onEditPageLoad(id)(createFakeRequest())
      status(result) must be(OK)
    }

    "respond with BAD_REQUEST on submit if request is malformed" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List())
      val formFill = propertyTypeForm.fill(CommonBuilder.buildProperty.copy(propertyType = None))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      setUpTests(Some(applicationDetails))

      val result = propertyTypeController.onSubmit()(request)
      status(result) must be (BAD_REQUEST)
    }

    "save application and go to property overview page in non-edit mode on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List())
      val formFill = propertyTypeForm.fill(CommonBuilder.buildProperty.copy(propertyType = TestHelper.PropertyTypeDeceasedHome))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      setUpTests(Some(applicationDetails))

      val result = propertyTypeController.onSubmit()(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.PropertyDetailsOverviewController.onEditPageLoad("1").url, TestHelper.AssetsPropertiesPropertyKindID)))
    }

    "add property to property list should add new property to list if property doesn't exist " in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List())
      setUpTests(Some(applicationDetails))

      val property1 = CommonBuilder.property
      val property2WithoutId = CommonBuilder.property.copy(id = None, value = Some(BigDecimal(2)))
      val property2 = CommonBuilder.property.copy(id = Some("2"), value = Some(BigDecimal(2)))
      val propertyList = List(property1)
      val propertyListNew = List(property1, property2)
      val result = propertyTypeController.addPropertyToPropertyList(property2WithoutId, propertyList)

      result must equal ((propertyListNew, "2"))
    }

    "save application and go to property overview page edit mode on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List())
      val formFill = propertyTypeForm.fill(CommonBuilder.buildProperty.copy(propertyType = TestHelper.PropertyTypeDeceasedHome))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      setUpTests(Some(applicationDetails))

      val result = propertyTypeController.onEditSubmit("1")(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.PropertyDetailsOverviewController.onEditPageLoad("1").url, TestHelper.AssetsPropertiesPropertyKindID)))
    }

    "respond with exception on edit page load where property id does not exist" in {
      val id: String = "15542"
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List(CommonBuilder.property))
      setUpTests(Some(applicationDetails))

      a[RuntimeException] mustBe thrownBy {
        Await.result(propertyTypeController.onEditPageLoad(id)(createFakeRequest()), Duration.Inf)
      }
    }

    "save application and go to property overview page in edit mode on submit where user add new property" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List(CommonBuilder.property,
        CommonBuilder.property.copy(id = Some("2"))))
      val formFill = propertyTypeForm.fill(CommonBuilder.buildProperty.copy(propertyType = TestHelper.PropertyTypeDeceasedHome))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      setUpTests(Some(applicationDetails))

      val result = propertyTypeController.onEditSubmit("2")(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.PropertyDetailsOverviewController.onEditPageLoad("2").url, TestHelper.AssetsPropertiesPropertyKindID)))
    }

    "respond with InternalServerError on edit page load where no application details" in {
      val id: String = "1"
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List(CommonBuilder.property))

      setUpTests()

      val result = propertyTypeController.onEditPageLoad(id)(createFakeRequest())
      status(result) must be(INTERNAL_SERVER_ERROR)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      propertyTypeController.onPageLoad(createFakeRequest(authRetrieveNino = false)))
  }
}
