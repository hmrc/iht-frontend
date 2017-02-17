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
import iht.models.application.assets.InsurancePolicy
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.ContentChecker
import iht.utils.CommonHelper
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._

/**
 *
 * Created by Yasar Acar on 18/02/16.
 *
 */
class InsurancePolicyDetailsFinalGuidanceControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def insurancePolicyDetailsFinalGuidanceController = new InsurancePolicyDetailsFinalGuidanceController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def insurancePolicyDetailsFinalGuidanceControllerNotAuthorised = new InsurancePolicyDetailsFinalGuidanceController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
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
      val result = insurancePolicyDetailsFinalGuidanceControllerNotAuthorised.onPageLoad()(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result).get should be(loginUrl)
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

      val result = insurancePolicyDetailsFinalGuidanceController.onPageLoad()(createFakeRequest())
      status(result) shouldBe OK

      ContentChecker.stripLineBreaks(contentAsString(result)) should include(messagesApi("page.iht.application.insurance.policies.section7.guidance",
                                            CommonHelper.getDeceasedNameOrDefaultString(registrationDetails)))
      contentAsString(result) should include(messagesApi("page.iht.application.insurance.policies.section7.guidance2"))
    }
  }

  "InsurancePolicyDetailsFinalGuidanceController: giftsPageRedirect" must {

    "return first gifts guidance page given gifts guidance not seen" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = registrationDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val initialGiftsQuestionAnswerOption = None
      val seenGiftsGuidance = false

      val result = insurancePolicyDetailsFinalGuidanceController.giftsPageRedirect(initialGiftsQuestionAnswerOption, seenGiftsGuidance)(createFakeRequest())

      result should be (Some(iht.controllers.application.gifts.guidance.routes.WhatIsAGiftController.onPageLoad()))
    }

    "return initial gifts question page given gifts guidance seen but initial gifts question not answered" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = registrationDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val initialGiftsQuestionAnswerOption = None
      val seenGiftsGuidance = true

      val result = insurancePolicyDetailsFinalGuidanceController.giftsPageRedirect(initialGiftsQuestionAnswerOption, seenGiftsGuidance)(createFakeRequest())

      result should be (Some(iht.controllers.application.gifts.routes.GivenAwayController.onPageLoad()))
    }

    "return initial gifts question page given gifts guidance seen and initial gifts question answered no" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = registrationDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val initialGiftsQuestionAnswerOption = Some(false)
      val seenGiftsGuidance = true

      val result = insurancePolicyDetailsFinalGuidanceController.giftsPageRedirect(initialGiftsQuestionAnswerOption, seenGiftsGuidance)(createFakeRequest())

      result should be (Some(iht.controllers.application.gifts.routes.GivenAwayController.onPageLoad()))
    }

    "return gifts overview page when gifts guidance seen and initial question answered yes" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = registrationDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val initialGiftsQuestionAnswerOption = Some(true)
      val seenGiftsGuidance = true

      val result = insurancePolicyDetailsFinalGuidanceController.giftsPageRedirect(initialGiftsQuestionAnswerOption, seenGiftsGuidance)(createFakeRequest())

      result should be (Some(iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad()))
    }
  }
}
