/*
 * Copyright 2016 HM Revenue & Customs
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
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._

import scala.concurrent.Future

/**
 *
 * Created by Yasar Acar on 18/02/16.
 *
 */
class InsurancePolicyDetailsAnnuityControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def insurancePolicyDetailsAnnuityController = new InsurancePolicyDetailsAnnuityController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }
  def insurancePolicyDetailsAnnuityControllerNotAuthorised = new InsurancePolicyDetailsAnnuityController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC123"))

  val insurancePolicyDetailsKickOut = InsurancePolicy(
    isAnnuitiesBought = Some(true),
    isInsurancePremiumsPayedForSomeoneElse = Some(true),
    value = Some(BigDecimal(7)),
    shareValue = Some(BigDecimal(8)),
    policyInDeceasedName = Some(true),
    isJointlyOwned = Some(true),
    isInTrust = Some(true),
    coveredByExemption = Some(true),
    sevenYearsBefore = Some(true),
    moreThanMaxValue = Some(false)
  )

  val insurancePolicyDetailsAnnuityFalse = InsurancePolicy(
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

  val allAssets = CommonBuilder.buildAllAssets copy (insurancePolicy = Some(insurancePolicyDetailsAnnuityFalse))

  val applicationDetails = CommonBuilder.buildApplicationDetails copy (allAssets = Some(allAssets))

  private def createMocks(applicationDetails: ApplicationDetails) = {
    when(mockCachingConnector.getExistingRegistrationDetails(any(), any()))
      .thenReturn(registrationDetails)
    when(mockIhtConnector.getApplication(any(), any(), any())(any()))
      .thenReturn(Future.successful(Some(applicationDetails)))
    when(mockCachingConnector.storeApplicationDetails(any())(any(), any()))
      .thenReturn(Future.successful(Some(applicationDetails)))
    when(mockCachingConnector.getExistingRegistrationDetails(any(), any()))
      .thenReturn(registrationDetails)
    when(mockIhtConnector.saveApplication(any(), any(), any())(any()))
      .thenReturn(Future.successful(Some(applicationDetails)))
  }

  "InsurancePolicyDetailsAnnuityController" must {

    "save application and go to Insurance Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(insurancePolicy = Some(insurancePolicyDetailsAnnuityFalse))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val insuranceAnnuityValue = CommonBuilder.buildInsurancePolicy.copy(isAnnuitiesBought=Some(true))

      val filledInsuranceForm = insurancePolicyForm.fill(insuranceAnnuityValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledInsuranceForm.data.toSeq: _*)

      val result = insurancePolicyDetailsAnnuityController.onSubmit (request)
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

      val insuranceAnnuityValue = CommonBuilder.buildInsurancePolicy.copy(isAnnuitiesBought=Some(true))

      val filledInsuranceForm = insurancePolicyForm.fill(insuranceAnnuityValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledInsuranceForm.data.toSeq: _*)

      val result = insurancePolicyDetailsAnnuityController.onSubmit (request)
      status(result) shouldBe (SEE_OTHER)
    }

    "respond with bad request when incorrect value are entered on the page" in {

      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetExistingRegDetailsFromCache(mockCachingConnector)

      val result = insurancePolicyDetailsAnnuityController.onSubmit (fakePostRequest)
      status(result) shouldBe (BAD_REQUEST)
    }

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = insurancePolicyDetailsAnnuityControllerNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result).get should be(loginUrl)
    }

    "respond with OK on page load" in {
      createMocks(applicationDetails)
      val result = insurancePolicyDetailsAnnuityController.onPageLoad(createFakeRequest())
      status(result) should be (OK)
    }

    "display a question on the page" in {
      createMocks(applicationDetails)
      val result = insurancePolicyDetailsAnnuityController.onPageLoad(createFakeRequest())
      contentAsString(result) should include(Messages("iht.estateReport.assets.insurancePolicies.buyAnnuity.question"))
    }

    "display a yes radio button on page" in {
      createMocks(applicationDetails)
      val result = insurancePolicyDetailsAnnuityController.onPageLoad(createFakeRequest())
      contentAsString(result) should include(Messages("iht.yes"))
    }

    "display a no radio button on page" in {
      createMocks(applicationDetails)
      val result = insurancePolicyDetailsAnnuityController.onPageLoad(createFakeRequest())
      contentAsString(result) should include(Messages("iht.no"))
    }

    "redirect to correct page when no selected on submit" in {
      createMocks(applicationDetails)

      val filledForm = insurancePolicyForm.fill(insurancePolicyDetailsAnnuityFalse)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      val result = insurancePolicyDetailsAnnuityController.onSubmit (request)
      redirectLocation(result) should be (Some(
        iht.controllers.application.assets.insurancePolicy.routes.InsurancePolicyDetailsInTrustController.onPageLoad().url))
    }

    "redirect to kickout page when yes selected on submit" in {
      createMocks(applicationDetails)

      when(mockIhtConnector.saveApplication(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(applicationDetails)))

      val filledForm = insurancePolicyForm.fill(insurancePolicyDetailsKickOut)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      val result = insurancePolicyDetailsAnnuityController.onSubmit (request)
      redirectLocation(result) should be (Some(iht.controllers.application.routes.KickoutController.onPageLoad().url))
    }
  }
}
