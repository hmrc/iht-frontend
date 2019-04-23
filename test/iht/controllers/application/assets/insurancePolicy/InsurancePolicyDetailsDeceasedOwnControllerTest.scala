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

package iht.controllers.application.assets.insurancePolicy


import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.InsurancePolicy

import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class InsurancePolicyDetailsDeceasedOwnControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with InsurancePolicyDetailsDeceasedOwnController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def insurancePolicyDetailsDeceasedOwnController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }
  def insurancePolicyDetailsDeceasedOwnControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
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

  "InsurancePolicyDetailsDeceasedOwnController" must {

    "save application and go to Insurance Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(insurancePolicy = Some(insurancePolicyDetails))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val insuranceDeceasedOwnValue = CommonBuilder.buildInsurancePolicy.copy(policyInDeceasedName=Some(true),value=Some(20))

      val filledInsuranceForm = insurancePolicyDeceasedOwnQuestionForm.fill(insuranceDeceasedOwnValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledInsuranceForm.data.toSeq: _*)

      val result = insurancePolicyDetailsDeceasedOwnController.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
    }

    "save application and go to Insurance Overview page on submit where no assets previously saved" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = None)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val insuranceDeceasedOwnValue = CommonBuilder.buildInsurancePolicy.copy(policyInDeceasedName=Some(true),value=Some(20))

      val filledInsuranceForm = insurancePolicyDeceasedOwnQuestionForm.fill(insuranceDeceasedOwnValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledInsuranceForm.data.toSeq: _*)

      val result = insurancePolicyDetailsDeceasedOwnController.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
    }

    "save application and go to Insurance Overview page on submit where answer as no" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = None)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val insuranceDeceasedOwnValue = CommonBuilder.buildInsurancePolicy.copy(policyInDeceasedName=Some(false))

      val filledInsuranceForm = insurancePolicyDeceasedOwnQuestionForm.fill(insuranceDeceasedOwnValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledInsuranceForm.data.toSeq: _*)

      val result = insurancePolicyDetailsDeceasedOwnController.onSubmit (request)
      status(result) mustBe (SEE_OTHER)
    }

    "respond with bad request when incorrect value are entered on the page" in {

      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = insurancePolicyDetailsDeceasedOwnController.onSubmit (fakePostRequest)
      status(result) mustBe (BAD_REQUEST)
    }

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = insurancePolicyDetailsDeceasedOwnControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result).get must be(loginUrl)
    }

    "respond with OK on page load" in {
      createMocks(applicationDetails)
      val result = insurancePolicyDetailsDeceasedOwnController.onPageLoad(createFakeRequest())
      status(result) must be (OK)
    }

    "redirect to correct page on submit" in {
      createMocks(applicationDetails)

      val filledForm = insurancePolicyDeceasedOwnQuestionForm.fill(insurancePolicyDetails)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      val result = insurancePolicyDetailsDeceasedOwnController.onSubmit (request)
      redirectLocation(result) must be (Some(iht.controllers.application.assets.insurancePolicy.routes.InsurancePolicyOverviewController.onPageLoad().url + "#" + mockAppConfig.InsurancePayingToDeceasedYesNoID))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      insurancePolicyDetailsDeceasedOwnController.onPageLoad(createFakeRequest()))
  }
}
