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

package iht.forms.application.tnrb

import iht.FakeIhtApp
import iht.forms.FormTestHelper
import iht.forms.TnrbForms._
import iht.models.application.tnrb.{TnrbEligibiltyModel, WidowCheck}
import iht.testhelpers.CommonBuilder

class TnrbFormsTest extends FormTestHelper with FakeIhtApp {
  private val sampleFirstName = CommonBuilder.firstNameGenerator
  private val sampleLastName = CommonBuilder.firstNameGenerator

  private def widowCheck(widowed: String,
                         day: String,
                         month: String,
                         year: String) = Map(
    "widowed" -> widowed,
    "dateOfPreDeceased.day" -> day,
    "dateOfPreDeceased.month" -> month,
    "dateOfPreDeceased.year" -> year
  )

  private lazy val completeWidowCheck = widowCheck("true", "01", "01", "2000")

  private def tnrbEligibility(
                               isPartnerLivingInUk: String,
                               isGiftMadeBeforeDeath: String,
                               isStateClaimAnyBusiness: String,
                               isPartnerGiftWithResToOther: String,
                               isPartnerBenFromTrust: String,
                               isEstateBelowIhtThresholdApplied: String,
                               isJointAssetPassed: String,
                               firstName: String,
                               lastName: String,
                               dateOfMarriageDay: String,
                               dateOfMarriageMonth: String,
                               dateOfMarriageYear: String,
                               dateOfPreDeceasedDay: String,
                               dateOfPreDeceasedMonth: String,
                               dateOfPreDeceasedYear: String
                             ) = Map(
    "isPartnerLivingInUk" -> isPartnerLivingInUk,
    "isGiftMadeBeforeDeath" -> isGiftMadeBeforeDeath,
    "isStateClaimAnyBusiness" -> isStateClaimAnyBusiness,
    "isPartnerGiftWithResToOther" -> isPartnerGiftWithResToOther,
    "isPartnerBenFromTrust" -> isPartnerBenFromTrust,
    "isEstateBelowIhtThresholdApplied" -> isEstateBelowIhtThresholdApplied,
    "isJointAssetPassed" -> isJointAssetPassed,
    "firstName" -> firstName,
    "lastName" -> lastName,
    "dateOfMarriage.day" -> dateOfMarriageDay,
    "dateOfMarriage.month" -> dateOfMarriageMonth,
    "dateOfMarriage.year" -> dateOfMarriageYear,
    "dateOfPreDeceased.day" -> dateOfPreDeceasedDay,
    "dateOfPreDeceased.month" -> dateOfPreDeceasedMonth,
    "dateOfPreDeceased.year" -> dateOfPreDeceasedYear
  )

  private lazy val completeTnrbEligibility = tnrbEligibility(
    "true", "true", "true", "true", "true", "true", "true", sampleFirstName, sampleLastName,
    "01", "01", "2000", "01", "01", "2000"
  )

  "deceasedWidowCheckQuestionForm" must {
    behave like yesNoQuestion[WidowCheck]("widowed",
      deceasedWidowCheckQuestionForm, _.widowed, "error.widowed.select")
  }

  "deceasedWidowCheckDateForm" must {
    behave like dateOfDeath(completeWidowCheck, deceasedWidowCheckDateForm, "dateOfPreDeceased")
  }

  "partnerLivingInUkForm" must {
    behave like yesNoQuestion[TnrbEligibiltyModel]("isPartnerLivingInUk",
      partnerLivingInUkForm, _.isPartnerLivingInUk, "error.isPartnerLivingInUk.select")
  }

  "giftMadeBeforeDeathForm" must {
    behave like yesNoQuestion[TnrbEligibiltyModel]("isGiftMadeBeforeDeath",
      giftMadeBeforeDeathForm, _.isGiftMadeBeforeDeath, "error.isGiftMadeBeforeDeath.select")
  }

  "partnerGiftWithResToOtherForm" must {
    behave like yesNoQuestion[TnrbEligibiltyModel]("isPartnerGiftWithResToOther",
      partnerGiftWithResToOtherForm, _.isPartnerGiftWithResToOther, "error.isPartnerGiftWithResToOther.select")
  }

  "estateClaimAnyBusinessForm" must {
    behave like yesNoQuestion[TnrbEligibiltyModel]("isStateClaimAnyBusiness",
      estateClaimAnyBusinessForm, _.isStateClaimAnyBusiness, "error.isStateClaimAnyBusiness.select")
  }

  "benefitFromTrustForm" must {
    behave like yesNoQuestion[TnrbEligibiltyModel]("isPartnerBenFromTrust",
      benefitFromTrustForm, _.isPartnerBenFromTrust, "error.isPartnerBenFromTrust.select")
  }

  "estatePassedToDeceasedOrCharityForm" must {
    behave like yesNoQuestion[TnrbEligibiltyModel]("isEstateBelowIhtThresholdApplied",
      estatePassedToDeceasedOrCharityForm, _.isEstateBelowIhtThresholdApplied, "error.isEstateBelowIhtThresholdApplied.select")
  }

  "jointAssetPassedForm" must {
    behave like yesNoQuestion[TnrbEligibiltyModel]("isJointAssetPassed",
      jointAssetPassedForm, _.isJointAssetPassed, "error.isJointAssetPassed.select")
  }

  "partnerNameForm" must {
    "give an error when the first name is not supplied" in {
      val expectedError = singleError("firstName", "error.firstName.give")
      val data = formData("firstName", "")
      checkForContainsError(partnerNameForm, data, expectedError)
    }

    "give an error when the first name is too long" in {
      val expectedError = singleError("firstName", "error.firstName.giveUsingXCharsOrLess")
      val data = formData("firstName", "a" * 100)
      checkForContainsError(partnerNameForm, data, expectedError)
    }
    "give no error when a valid first and last name is supplied" in {
      val data = formData("firstName", sampleFirstName, "lastName", sampleLastName)
      val boundModel = formWithNoError(partnerNameForm, data)
      boundModel.firstName mustBe Some(sampleFirstName)
      boundModel.lastName mustBe Some(sampleLastName)
    }

    "give an error when the last name is not supplied" in {
      val expectedError = singleError("lastName", "error.lastName.give")
      val data = formData("lastName", "")
      checkForContainsError(partnerNameForm, data, expectedError)
    }

    "give an error when the last name is too long" in {
      val expectedError = singleError("lastName", "error.lastName.giveUsingXCharsOrLess")
      val data = formData("lastName", "a" * 100)
      checkForContainsError(partnerNameForm, data, expectedError)
    }
  }

  "dateOfMarriageForm" must {
    behave like dateOfMarriage(completeTnrbEligibility, dateOfMarriageForm)
  }
}
