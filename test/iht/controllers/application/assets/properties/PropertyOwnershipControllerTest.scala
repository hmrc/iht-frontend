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
import iht.testhelpers.{CommonBuilder, ContentChecker, TestHelper}
import iht.utils.CommonHelper
import iht.views.html.application.asset.properties.property_ownership
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future


class PropertyOwnershipControllerTest extends ApplicationControllerTest {

  val regDetails = CommonBuilder.buildRegistrationDetails1
  val deceasedName = regDetails.deceasedDetails.map(_.name).fold("")(identity)

  def setUpTests(applicationDetails: Option[ApplicationDetails] = None) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      regDetails = regDetails,
      appDetails = applicationDetails,
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  protected abstract class TestController extends FrontendController(mockControllerComponents) with PropertyOwnershipController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val propertyOwnershipView: property_ownership = app.injector.instanceOf[property_ownership]
  }

  def propertyOwnershipController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector
  }

  def propertyOwnershipControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector
  }

  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier

  "PropertyOwnership controller" must {

    "redirect to ida login page on PageLoad if the user is not logged in" in {
      val result = propertyOwnershipControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = propertyOwnershipControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond ok on page load" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property))

      setUpTests(Some(applicationDetails))

      val result = propertyOwnershipController.onPageLoad(createFakeRequest(authRetrieveNino = false))
      status(result) must be (OK)
    }

    "display the correct title on page" in {
      val regDetails = CommonBuilder.buildRegistrationDetails1
      val deceasedName = regDetails.deceasedDetails.map(_.name).fold("")(identity)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Future.successful(Some(regDetails)))

      val result = propertyOwnershipController.onPageLoad(createFakeRequest(authRetrieveNino = false))
      status(result) must be (OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include (messagesApi("iht.estateReport.assets.howOwnedByDeceased", deceasedName))
    }

    "display the correct title on page in edit mode" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property))

      setUpTests(Some(applicationDetails))

      val result = propertyOwnershipController.onEditPageLoad("1")(createFakeRequest())
      status(result) must be (OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include (messagesApi("iht.estateReport.assets.howOwnedByDeceased", deceasedName))
    }

    "respond with INTERNAL_SERVER_ERROR on page load in edit mode when application details could not be retrieved" in {
      setUpTests()

      val result = propertyOwnershipController.onEditPageLoad("1")(createFakeRequest())
      status(result) must be (INTERNAL_SERVER_ERROR)
    }

    "respond with RuntimeException on page load in edit mode when matched property is not found" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property.copy(id = Some("1"))))

      setUpTests(Some(applicationDetails))

      intercept[RuntimeException] {
        await(propertyOwnershipController.onEditPageLoad("2")(createFakeRequest()))
      }
    }

    "redirect to PropertyDetails overview page on submit" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List())

      val formFill = typeOfOwnershipForm.fill(
                            CommonBuilder.buildProperty.copy(typeOfOwnership = TestHelper.TypesOfOwnershipDeceasedOnly))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*).withMethod("POST")

      setUpTests(Some(applicationDetails))

      val result = propertyOwnershipController.onSubmit()(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.PropertyDetailsOverviewController.onEditPageLoad("1").url, TestHelper.AssetsPropertiesPropertyOwnershipID)))
    }

    "redirect to PropertyDetails overview page on submit in edit mode" in {
      val propertyId = "1"

      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.buildProperty.copy(id = Some(propertyId),
          typeOfOwnership = TestHelper.PropertyTypeDeceasedHome,
          value = Some(1234))))

      val formFill = typeOfOwnershipForm.fill(CommonBuilder.buildProperty.copy(
                                                  typeOfOwnership = TestHelper.TypesOfOwnershipDeceasedOnly))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*).withMethod("POST")

      setUpTests(Some(applicationDetails))

      val result = propertyOwnershipController.onEditSubmit(propertyId)(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.PropertyDetailsOverviewController.onEditPageLoad(propertyId).url, TestHelper.AssetsPropertiesPropertyOwnershipID)))
    }

    "respond with BAD_REQUEST on submit when request is malformed" in {
      val propertyId = "1"
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.buildProperty.copy(id = Some(propertyId),
          typeOfOwnership = TestHelper.PropertyTypeDeceasedHome,
          value = Some(1234))))

      val formFill = typeOfOwnershipForm.fill(CommonBuilder.buildProperty.copy(typeOfOwnership = None))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*).withMethod("POST")

      setUpTests(Some(applicationDetails))

      val result = propertyOwnershipController.onEditSubmit(propertyId)(request)
      status(result) must be (BAD_REQUEST)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      propertyOwnershipController.onPageLoad(createFakeRequest(authRetrieveNino = false)))
  }

}
