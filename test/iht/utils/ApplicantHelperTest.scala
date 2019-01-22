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

package iht.utils

import iht.FakeIhtApp
import iht.testhelpers._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec


class ApplicantHelperTest extends FakeIhtApp with MockitoSugar {

  "Predicate isApplicantApplyingForProbateQuestionAnswered returns true when applicant probate " +
    "question answered" in {
    ApplicantHelper.isApplicantApplyingForProbateQuestionAnswered(CommonBuilder.buildRegistrationDetails copy(
        applicantDetails = Some(CommonBuilder.buildApplicantDetails)
      ), "") mustBe true
  }

  "Predicate isApplicantApplyingForProbateQuestionAnswered returns false when applicant probate " +
    "question is not answered" in {
    ApplicantHelper.isApplicantApplyingForProbateQuestionAnswered(CommonBuilder.buildRegistrationDetails copy(
      applicantDetails = Some(CommonBuilder.buildApplicantDetails.copy(isApplyingForProbate = None))
      ), "") mustBe false
  }

  "Predicate isThereAnApplicantProbateLocation returns true when there is an applicant probate location " in {
    ApplicantHelper.isThereAnApplicantProbateLocation(CommonBuilder.buildRegistrationDetails copy(
      applicantDetails = Some(CommonBuilder.buildApplicantDetails)
      ), "") mustBe true
  }

  "Predicate isThereAnApplicantProbateLocation returns false when there is no applicant probate" +
    " location selected " in {
    ApplicantHelper.isThereAnApplicantProbateLocation(CommonBuilder.buildRegistrationDetails copy(
      applicantDetails = Some(CommonBuilder.buildApplicantDetails.copy(country = None))
      ), "") mustBe false
  }

  "Predicate isThereAnApplicantPhoneNo returns true when there is an applicant phone no " in {
    ApplicantHelper.isThereAnApplicantPhoneNo(CommonBuilder.buildRegistrationDetails copy(
      applicantDetails = Some(CommonBuilder.buildApplicantDetails)
      ), "") mustBe true
  }

  "Predicate isThereAnApplicantPhoneNo returns false when there is no applicant phone no " in {
    ApplicantHelper.isThereAnApplicantPhoneNo(CommonBuilder.buildRegistrationDetails copy(
      applicantDetails = Some(CommonBuilder.buildApplicantDetails.copy(phoneNo = None))
      ), "") mustBe false
  }

  "Predicate isApplicantOthersApplyingForProbateQuestionAnsweredYes returns true when" +
    " applicant others applying for probate question answered" in {
    ApplicantHelper.isApplicantOthersApplyingForProbateQuestionAnsweredYes(
      CommonBuilder.buildRegistrationDetails copy(
      areOthersApplyingForProbate = Some(true)
      ), "") mustBe true
  }

  "Predicate isApplicantOthersApplyingForProbateQuestionAnsweredYes returns false when" +
    " applicant others applying for probate question is not answered" in {
    ApplicantHelper.isApplicantOthersApplyingForProbateQuestionAnsweredYes(
      CommonBuilder.buildRegistrationDetails copy(
      areOthersApplyingForProbate = None
      ), "") mustBe false
  }

  "Predicate isApplicantOthersApplyingForProbateQuestionAnsweredYes returns false when" +
    " applicant others applying for probate question is value is selected as No" in {
    ApplicantHelper.isApplicantOthersApplyingForProbateQuestionAnsweredYes(
      CommonBuilder.buildRegistrationDetails copy(
      areOthersApplyingForProbate = Some(false)
      ), "") mustBe false
  }
}
