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
import iht.models.application.assets.InsurancePolicy
import iht.testhelpers.{CommonBuilder, TestHelper}
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
class InsurancePolicyOverviewControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def insurancePolicyOverviewController = new InsurancePolicyOverviewController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector

  }

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC123"))
  val allAssets = CommonBuilder.buildAllAssets copy (insurancePolicy = Some(InsurancePolicy(
    isAnnuitiesBought = Some(true),
    isInsurancePremiumsPayedForSomeoneElse = Some(true),
    value = Some(BigDecimal(7)),
    shareValue = Some(BigDecimal(8)),
    policyInDeceasedName = Some(true),
    isJointlyOwned = Some(true),
    isInTrust = Some(false),
    coveredByExemption = Some(true),
    sevenYearsBefore = Some(true),
    moreThanMaxValue = Some(false)
  )))

  val applicationDetails = CommonBuilder.buildApplicationDetails copy (allAssets = Some(allAssets))

  "InsurancePolicyOverviewController" must {
 
    "respond with OK and all questions on page load" in {
      when(mockCachingConnector.getExistingRegistrationDetails(any(), any()))
        .thenReturn(registrationDetails)
      when(mockIhtConnector.getApplication(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(applicationDetails)))
      val result = insurancePolicyOverviewController.onPageLoad(createFakeRequest())
      status(result) shouldBe OK

      contentAsString(result) should include(Messages("iht.estateReport.insurancePolicies.jointlyHeld.question"))
      contentAsString(result) should include(Messages("iht.estateReport.assets.insurancePolicies.totalValueOfDeceasedsShare"))
      contentAsString(result) should include(Messages("iht.estateReport.insurancePolicies.ownName.question"))
      contentAsString(result) should include(Messages("iht.estateReport.assets.insurancePolicies.totalValueOwnedAndPayingOut"))

      contentAsString(result) should include(Messages("iht.estateReport.insurancePolicies.premiumsNotPayingOut.question"))
      contentAsString(result) should include(Messages("iht.estateReport.insurancePolicies.overLimitNotOwnEstate.question"))
      contentAsString(result) should include(Messages("iht.estateReport.assets.insurancePolicies.buyAnnuity.question"))
      contentAsString(result) should include(Messages("page.iht.application.assets.insurance.policies.overview.other.question4"))
    }

    "respond with OK and correct question1 text on page load if deceased not married" in {
      val deceasedDetailsTemp = CommonBuilder.buildDeceasedDetails copy (maritalStatus=Some(TestHelper.MaritalStatusSingle))
      when(mockCachingConnector.getExistingRegistrationDetails(any(), any()))
        .thenReturn(registrationDetails copy (deceasedDetails = Some(deceasedDetailsTemp)))
      when(mockIhtConnector.getApplication(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(applicationDetails)))
      val result = insurancePolicyOverviewController.onPageLoad(createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("iht.estateReport.insurancePolicies.premiumsNotPayingOut.question"))
    }
  }
}
