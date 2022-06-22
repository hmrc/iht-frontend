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
import iht.models.application.assets.InsurancePolicy
import iht.testhelpers.{CommonBuilder, ContentChecker}
import iht.utils.DeceasedInfoHelper
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_final_guidance
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class InsurancePolicyDetailsFinalGuidanceControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with InsurancePolicyDetailsFinalGuidanceController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val insurancePolicyDetailsFinalGuidanceView: insurance_policy_details_final_guidance = app.injector.instanceOf[insurance_policy_details_final_guidance]
  }

  def insurancePolicyDetailsFinalGuidanceController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def insurancePolicyDetailsFinalGuidanceControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
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
    isInTrust = Some(true),
    coveredByExemption = Some(true),
    sevenYearsBefore = Some(true),
    moreThanMaxValue = Some(false)
  )))

  val applicationDetails = CommonBuilder.buildApplicationDetails copy (allAssets = Some(allAssets))

  "InsurancePolicyDetailsFinalGuidanceController" must {
    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = insurancePolicyDetailsFinalGuidanceControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result).get must be(loginUrl)
    }

    "respond with OK and all content on page load" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = registrationDetails,
        appDetails = Some(applicationDetails),
        singleValue = Some("false"),
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true,
        getSingleValueFromCache = true)

      val result = insurancePolicyDetailsFinalGuidanceController.onPageLoad(createFakeRequest())
      status(result) mustBe OK

      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("page.iht.application.insurance.policies.section7.guidance",
                                            DeceasedInfoHelper.getDeceasedNameOrDefaultString(registrationDetails)))
      contentAsString(result) must include(messagesApi("page.iht.application.insurance.policies.section7.guidance2"))
    }
  }

  "InsurancePolicyDetailsFinalGuidanceController: giftsPageRedirect" must {
    "return initial gifts question page given initial gifts question not answered" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = registrationDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val initialGiftsQuestionAnswerOption = None

      val result = insurancePolicyDetailsFinalGuidanceController.giftsPageRedirect(initialGiftsQuestionAnswerOption)

      result must be (iht.controllers.application.gifts.routes.GivenAwayController.onPageLoad)
    }

    "return initial gifts question page given initial gifts question answered no" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = registrationDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val initialGiftsQuestionAnswerOption = Some(false)

      val result = insurancePolicyDetailsFinalGuidanceController.giftsPageRedirect(initialGiftsQuestionAnswerOption)

      result must be (iht.controllers.application.gifts.routes.GivenAwayController.onPageLoad)
    }

    "return gifts overview page when initial question answered yes" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = registrationDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val initialGiftsQuestionAnswerOption = Some(true)

      val result = insurancePolicyDetailsFinalGuidanceController.giftsPageRedirect(initialGiftsQuestionAnswerOption)

      result must be (iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      insurancePolicyDetailsFinalGuidanceController.onPageLoad(createFakeRequest()))

  }
}
