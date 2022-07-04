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

package iht.controllers.application.assets.insurancePolicy


import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.InsurancePolicy

import iht.testhelpers.CommonBuilder
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_joint

import scala.concurrent.Future

class InsurancePolicyDetailsJointControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with InsurancePolicyDetailsJointController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val insurancePolicyDetailsJointView: insurance_policy_details_joint = app.injector.instanceOf[insurance_policy_details_joint]
  }

  def insurancePolicyDetailsJointController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def insurancePolicyDetailsJointControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC123"))

  val insurancePolicyDetails = InsurancePolicy(
    isAnnuitiesBought = Some(false),
    isInsurancePremiumsPayedForSomeoneElse = Some(true),
    value = Some(BigDecimal(7)),
    shareValue = Some(BigDecimal(8)),
    policyInDeceasedName = Some(true),
    isJointlyOwned = Some(true),
    isInTrust = Some(false),
    coveredByExemption = Some(true),
    sevenYearsBefore = Some(true),
    moreThanMaxValue = Some(false)
  )

  val allAssets = CommonBuilder.buildAllAssets copy (insurancePolicy = Some(insurancePolicyDetails))

  val applicationDetails = CommonBuilder.buildApplicationDetails copy (allAssets = Some(allAssets))

  private def createMocks(applicationDetails: ApplicationDetails) = {
    when(mockCachingConnector.getRegistrationDetails(any(), any()))
      .thenReturn(Future.successful(Some(registrationDetails)))
    when(mockIhtConnector.getApplication(any(), any(), any())(any()))
      .thenReturn(Future.successful(Some(applicationDetails)))
    when(mockCachingConnector.storeApplicationDetails(any())(any(), any()))
      .thenReturn(Future.successful(Some(applicationDetails)))
    when(mockIhtConnector.saveApplication(any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(Some(applicationDetails)))
  }

  "InsurancePolicyDetailsJointController" must {

    "save application and go to Insurance Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(insurancePolicy = Some(insurancePolicyDetails))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val insuranceJointValue = CommonBuilder.buildInsurancePolicy.copy(isJointlyOwned = Some(true), shareValue = Some(20))

      val filledInsuranceForm = insurancePolicyJointQuestionForm.fill(insuranceJointValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledInsuranceForm.data.toSeq: _*).withMethod("POST")

      val result = insurancePolicyDetailsJointController.onSubmit(request)
      status(result) mustBe (SEE_OTHER)
    }

    "save application and go to Insurance Overview page on submit where no assets previously saved" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = None)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val insuranceJointValue = CommonBuilder.buildInsurancePolicy.copy(isJointlyOwned = Some(true), shareValue = Some(20))

      val filledInsuranceForm = insurancePolicyJointQuestionForm.fill(insuranceJointValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledInsuranceForm.data.toSeq: _*).withMethod("POST")

      val result = insurancePolicyDetailsJointController.onSubmit(request)
      status(result) mustBe (SEE_OTHER)
    }

    "save application and go to Insurance Overview page on submit where answer as no" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = None)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val insuranceJointValue = CommonBuilder.buildInsurancePolicy.copy(isJointlyOwned = Some(false))

      val filledInsuranceForm = insurancePolicyJointQuestionForm.fill(insuranceJointValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledInsuranceForm.data.toSeq: _*).withMethod("POST")

      val result = insurancePolicyDetailsJointController.onSubmit(request)
      status(result) mustBe (SEE_OTHER)
    }

    "respond with bad request when incorrect value are entered on the page" in {

      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr")).withMethod("POST")

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = insurancePolicyDetailsJointController.onSubmit(fakePostRequest)
      status(result) mustBe (BAD_REQUEST)
    }

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = insurancePolicyDetailsJointControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result).get must be(loginUrl)
    }

    "respond with OK on page load" in {
      createMocks(applicationDetails)
      val result = insurancePolicyDetailsJointController.onPageLoad(createFakeRequest())
      status(result) must be(OK)
    }

    "redirect to correct page on submit" in {
      createMocks(applicationDetails)

      val filledForm = insurancePolicyJointQuestionForm.fill(insurancePolicyDetails)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*).withMethod("POST")

      val result = insurancePolicyDetailsJointController.onSubmit(request)
      redirectLocation(result) must be(Some(iht.controllers.application.assets.insurancePolicy.routes.InsurancePolicyOverviewController.onPageLoad.url + "#" + mockAppConfig.InsuranceJointlyHeldYesNoID))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      insurancePolicyDetailsJointController.onPageLoad(createFakeRequest()))

  }
}
