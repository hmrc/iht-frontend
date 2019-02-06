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

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.assets.InsurancePolicy
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder, TestHelper, ContentChecker}
import iht.utils.DeceasedInfoHelper
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

/**
 *
 * Created by Yasar Acar on 18/02/16.
 *
 */
class InsurancePolicyOverviewControllerTest extends ApplicationControllerTest {



  def insurancePolicyOverviewController = new InsurancePolicyOverviewController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC123"))

  lazy val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(registrationDetails)

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
      when(mockCachingConnector.getRegistrationDetails(any(), any()))
        .thenReturn(Future.successful(Some(registrationDetails)))
      when(mockIhtConnector.getApplication(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(applicationDetails)))
      val result = insurancePolicyOverviewController.onPageLoad(createFakeRequest())
      status(result) mustBe OK

      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("iht.estateReport.insurancePolicies.jointlyHeld.question", deceasedName))
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("iht.estateReport.assets.insurancePolicies.totalValueOfDeceasedsShare", deceasedName))
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("iht.estateReport.insurancePolicies.ownName.question", deceasedName))
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("iht.estateReport.assets.insurancePolicies.totalValueOwnedAndPayingOut", deceasedName))

      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("iht.estateReport.insurancePolicies.premiumsNotPayingOut.question", deceasedName))
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("iht.estateReport.insurancePolicies.overLimitNotOwnEstate.question", deceasedName))
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("iht.estateReport.assets.insurancePolicies.buyAnnuity.question", deceasedName))
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("page.iht.application.assets.insurance.policies.overview.other.question4", deceasedName))
    }

    "respond with OK and correct question1 text on page load if deceased not married" in {
      val deceasedDetailsTemp = CommonBuilder.buildDeceasedDetails copy (maritalStatus=Some(TestHelper.MaritalStatusSingle))
      when(mockCachingConnector.getRegistrationDetails(any(), any()))
        .thenReturn(Future.successful(Some(registrationDetails copy (deceasedDetails = Some(deceasedDetailsTemp)))))
      when(mockIhtConnector.getApplication(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(applicationDetails)))
      val result = insurancePolicyOverviewController.onPageLoad(createFakeRequest())
      status(result) mustBe OK
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("iht.estateReport.insurancePolicies.premiumsNotPayingOut.question", deceasedName))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      insurancePolicyOverviewController.onPageLoad(createFakeRequest()))

  }
}
