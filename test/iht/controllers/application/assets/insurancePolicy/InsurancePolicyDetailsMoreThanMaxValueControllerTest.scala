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

package iht.controllers.application.assets.insurancePolicy

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.testhelpers.CommonBuilder
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_more_than_max_value
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class InsurancePolicyDetailsMoreThanMaxValueControllerTest extends ApplicationControllerTest{

  protected abstract class TestController extends FrontendController(mockControllerComponents) with InsurancePolicyDetailsMoreThanMaxValueController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val insurancePolicyDetailsMoreThanMaxValueView: insurance_policy_details_more_than_max_value = app.injector.instanceOf[insurance_policy_details_more_than_max_value]
  }

  def controller = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def controllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "InsurancePolicyDetailsMoreThanMaxValueController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = controllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = controller.onPageLoad (createFakeRequest())
      status(result) mustBe (OK)
    }

    "save application and go to annuities page on submit when user selects Yes" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val insuranceAnnuityValue = CommonBuilder.buildInsurancePolicy.copy(moreThanMaxValue=Some(false))

      val filledInsuranceForm = insurancePolicyMoreThanMaxForm.fill(insuranceAnnuityValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledInsuranceForm.data.toSeq: _*)

      val result = controller.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be (Some(routes.InsurancePolicyDetailsAnnuityController.onPageLoad.url))
    }

    "save application and go to annuities page on submit when the user had some data before" in {

      val allAssets = CommonBuilder.buildAllAssets copy (insurancePolicy = Some(CommonBuilder.buildCompleteInsurancePolicy))

      val applicationDetails = CommonBuilder.buildApplicationDetails copy (allAssets = Some(allAssets))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val insuranceAnnuityValue = CommonBuilder.buildInsurancePolicy.copy(moreThanMaxValue=Some(false))

      val filledInsuranceForm = insurancePolicyMoreThanMaxForm.fill(insuranceAnnuityValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledInsuranceForm.data.toSeq: _*)

      val result = controller.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be (Some(routes.InsurancePolicyDetailsAnnuityController.onPageLoad.url))
    }

    "respond with bad request when incorrect value are entered on the page" in {

      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = controller.onSubmit (fakePostRequest)
      status(result) mustBe (BAD_REQUEST)
    }

    "redirect to kickout page when yes selected on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val insuranceAnnuityValue = CommonBuilder.buildInsurancePolicy.copy(moreThanMaxValue=Some(true))

      val filledInsuranceForm = insurancePolicyMoreThanMaxForm.fill(insuranceAnnuityValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledInsuranceForm.data.toSeq: _*)

      val result = controller.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be (Some(iht.controllers.application.routes.KickoutAppController.onPageLoad().url))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      controller.onPageLoad(createFakeRequest()))

  }
}
