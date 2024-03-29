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

package iht.controllers.application.assets.vehicles

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper._
import iht.utils.CommonHelper
import iht.views.html.application.asset.vehicles.vehicles_jointly_owned
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class VehiclesJointlyOwnedControllerTest extends ApplicationControllerTest {

  def setUpTests(applicationDetails: ApplicationDetails) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetails),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  protected abstract class TestController extends FrontendController(mockControllerComponents) with VehiclesJointlyOwnedController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val vehiclesJointlyOwnedView: vehicles_jointly_owned = app.injector.instanceOf[vehicles_jointly_owned]
  }

  def vehiclesJointlyOwnedController = new TestController {
    val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def vehiclesJointlyOwnedControllerNotAuthorised = new TestController {
    val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "VehiclesJointlyOwnedController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = vehiclesJointlyOwnedControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = vehiclesJointlyOwnedControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      setUpTests(applicationDetails)

      val result = vehiclesJointlyOwnedController.onPageLoad(createFakeRequest())
      status(result) must be(OK)
    }

    "save application and go to vehicles overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      val formFill = vehiclesJointlyOwnedForm.fill(CommonBuilder.buildShareableBasicElementExtended.copy(
        shareValue = Some(1000), isOwnedShare = Some(true)))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*).withMethod("POST")
      setUpTests(applicationDetails)

      val result = vehiclesJointlyOwnedController.onSubmit()(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.VehiclesOverviewController.onPageLoad.url, AssetsVehiclesSharedID)))
    }

    "wipe out the vehicles value if user selects No, save application and go to vehicles overview page on submit" in {
      val vehiclesJointlyOwned = CommonBuilder.buildShareableBasicElementExtended.copy(
        shareValue = Some(1000), isOwnedShare = Some(false))

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder.buildAllAssets.copy(
                                vehicles = Some(vehiclesJointlyOwned))))
      val formFill = vehiclesJointlyOwnedForm.fill(vehiclesJointlyOwned)

      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*).withMethod("POST")
      setUpTests(applicationDetails)

      val result = vehiclesJointlyOwnedController.onSubmit()(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.VehiclesOverviewController.onPageLoad.url, AssetsVehiclesSharedID)))

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val expectedAppDetails = applicationDetails.copy(allAssets = applicationDetails.allAssets.map(_.copy(
        vehicles = Some(CommonBuilder.buildShareableBasicElementExtended.copy(shareValue = None,
                                                                              isOwnedShare = Some(false))))))

      capturedValue mustBe expectedAppDetails
    }
    "display validation message when form is submitted with no values entered" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      implicit val request = createFakeRequest()

      setUpTests(applicationDetails)

      val result = vehiclesJointlyOwnedController.onSubmit()(request)
      status(result) must be (BAD_REQUEST)
      contentAsString(result) must include (messagesApi("error.problem"))
    }

    "redirect to overview when form is submitted with answer yes and a value entered" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      implicit val request = createFakeRequest().withFormUrlEncodedBody(("isOwnedShare", "true"), ("shareValue", "233")).withMethod("POST")

      setUpTests(applicationDetails)

      val result = vehiclesJointlyOwnedController.onSubmit()(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.VehiclesOverviewController.onPageLoad.url, AssetsVehiclesSharedID)))
    }

    "respond with bad request when incorrect value are entered on the page" in {
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("shareValue", "utytyyterrrrrrrrrrrrrr")).withMethod("POST")

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = vehiclesJointlyOwnedController.onSubmit (fakePostRequest)
      status(result) mustBe (BAD_REQUEST)
    }

    "display the correct title on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      setUpTests(applicationDetails)

      val result = vehiclesJointlyOwnedController.onPageLoad(createFakeRequest())
      status(result) must be (OK)
      contentAsString(result) must include (messagesApi("page.iht.application.assets.vehicles.jointly.owned.title"))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      vehiclesJointlyOwnedController.onPageLoad(createFakeRequest()))
  }
}
