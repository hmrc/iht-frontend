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

package iht.controllers.application.assets.vehicles

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails

import iht.testhelpers.TestHelper._
import iht.testhelpers.{CommonBuilder, ContentChecker, MockFormPartialRetriever}
import iht.utils._
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

/**
 * Created by jennygj on 17/06/16.
 */

class VehiclesDeceasedOwnControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with VehiclesDeceasedOwnController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  lazy val regDetails = CommonBuilder.buildRegistrationDetails copy (
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails), ihtReference = Some("AbC123"))

  lazy val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)

  def setUpTests(applicationDetails: ApplicationDetails) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetails),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  def vehiclesDeceasedOwnController = new TestController {
    val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def vehiclesDeceasedOwnControllerNotAuthorised = new TestController {
    val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }



  "VehiclesDeceasedOwnController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = vehiclesDeceasedOwnControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = vehiclesDeceasedOwnControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      setUpTests(applicationDetails)

      val result = vehiclesDeceasedOwnController.onPageLoad(createFakeRequest())
      status(result) must be(OK)
    }

    "save application and go to vehicles overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List())
      val formFill = vehiclesFormOwn.fill(CommonBuilder.buildShareableBasicElementExtended.copy(
        value = Some(100), shareValue = None, isOwned = Some(true), isOwnedShare = None))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)
      setUpTests(applicationDetails)

      val result = vehiclesDeceasedOwnController.onSubmit()(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.VehiclesOverviewController.onPageLoad.url, AssetsVehiclesOwnID)))
    }

    "wipe out the vehicles value if user selects No, save application and go to vehicles overview page on submit" in {

      val  vehicleOwned = CommonBuilder.buildShareableBasicElementExtended.copy(value = Some(100), isOwned = Some(false))

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder.buildAllAssets.copy(
                                                vehicles = Some(vehicleOwned))))

      val formFill = vehiclesFormOwn.fill(vehicleOwned)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)
      setUpTests(applicationDetails)

      val result = vehiclesDeceasedOwnController.onSubmit()(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.VehiclesOverviewController.onPageLoad.url, AssetsVehiclesOwnID)))

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val expectedAppDetails = applicationDetails.copy(allAssets = applicationDetails.allAssets.map(_.copy(
        vehicles = Some(CommonBuilder.buildShareableBasicElementExtended.copy(value = None,
          isOwned = Some(false))))))

      capturedValue mustBe expectedAppDetails
    }

    "display validation message when form is submitted with no values entered" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      implicit val request = createFakeRequest()

      setUpTests(applicationDetails)

      val result = vehiclesDeceasedOwnController.onSubmit()(request)
      status(result) must be (BAD_REQUEST)
      contentAsString(result) must include (messagesApi("error.problem"))
    }

    "redirect to overview when form is submitted with answer yes and a value entered" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      implicit val request = createFakeRequest().withFormUrlEncodedBody(("isOwned", "true"), ("value", "233"))

      setUpTests(applicationDetails)

      val result = vehiclesDeceasedOwnController.onSubmit()(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.VehiclesOverviewController.onPageLoad.url, AssetsVehiclesOwnID)))
    }

    "respond with bad request when incorrect value are entered on the page" in {
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = vehiclesDeceasedOwnController.onSubmit (fakePostRequest)
      status(result) mustBe (BAD_REQUEST)
    }

    "display the correct title on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      setUpTests(applicationDetails)

      val result = vehiclesDeceasedOwnController.onPageLoad()(createFakeRequest())
      status(result) must be (OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include (messagesApi("iht.estateReport.assets.vehiclesOwned", deceasedName))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      vehiclesDeceasedOwnController.onPageLoad(createFakeRequest()))
  }
}
