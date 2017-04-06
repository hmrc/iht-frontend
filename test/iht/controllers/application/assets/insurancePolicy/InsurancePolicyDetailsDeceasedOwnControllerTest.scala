/*
 * Copyright 2017 HM Revenue & Customs
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

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.InsurancePolicy
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils.CommonHelper
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import iht.testhelpers.ContentChecker
import iht.constants.Constants._
import iht.constants.IhtProperties._
import scala.concurrent.Future

/**
 *
 * Created by Yasar Acar on 18/02/16.
 *
 */
class InsurancePolicyDetailsDeceasedOwnControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def insurancePolicyDetailsDeceasedOwnController = new InsurancePolicyDetailsDeceasedOwnController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }
  def insurancePolicyDetailsDeceasedOwnControllerNotAuthorised = new InsurancePolicyDetailsDeceasedOwnController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
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
    when(mockIhtConnector.saveApplication(any(), any(), any())(any()))
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
      status(result) shouldBe (SEE_OTHER)
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
      status(result) shouldBe (SEE_OTHER)
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
      status(result) shouldBe (SEE_OTHER)
    }

    "respond with bad request when incorrect value are entered on the page" in {

      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = insurancePolicyDetailsDeceasedOwnController.onSubmit (fakePostRequest)
      status(result) shouldBe (BAD_REQUEST)
    }

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = insurancePolicyDetailsDeceasedOwnControllerNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result).get should be(loginUrl)
    }

    "respond with OK on page load" in {
      createMocks(applicationDetails)
      val result = insurancePolicyDetailsDeceasedOwnController.onPageLoad(createFakeRequest())
      status(result) should be (OK)
    }

    "redirect to correct page on submit" in {
      createMocks(applicationDetails)

      val filledForm = insurancePolicyDeceasedOwnQuestionForm.fill(insurancePolicyDetails)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      val result = insurancePolicyDetailsDeceasedOwnController.onSubmit (request)
      redirectLocation(result) should be (Some(iht.controllers.application.assets.insurancePolicy.routes.InsurancePolicyOverviewController.onPageLoad().url + "#" + InsurancePayingToDeceasedYesNoID))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      insurancePolicyDetailsDeceasedOwnController.onPageLoad(createFakeRequest()))
  }
}
