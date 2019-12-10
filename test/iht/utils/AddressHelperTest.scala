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
import iht.config.AppConfig
import iht.testhelpers._
import org.scalatestplus.mockito.MockitoSugar

class AddressHelperTest extends FakeIhtApp with MockitoSugar {
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "Predicate isThereAnApplicantAddress returns true when there is an applicant address " in {
    AddressHelper.isThereAnApplicantAddress(CommonBuilder.buildRegistrationDetails copy(
      applicantDetails = Some(CommonBuilder.buildApplicantDetails)
      ), "") mustBe true
  }

  "Predicate isThereAnApplicantAddress returns false when there is no applicant address " in {
    AddressHelper.isThereAnApplicantAddress(CommonBuilder.buildRegistrationDetails copy(
      applicantDetails = Some(CommonBuilder.buildApplicantDetails.copy(ukAddress = None))
      ), "") mustBe false
  }
}
