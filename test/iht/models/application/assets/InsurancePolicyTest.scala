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

package iht.models.application.assets

import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by vineet on 03/11/16.
  */
class InsurancePolicyTest extends UnitSpec with MockitoSugar{

  "isComplete" must {

    "returns Some(true) if InsurancePolicy is complete" in {
      val insurancePolicy = CommonBuilder.buildInsurancePolicy.copy(isAnnuitiesBought = Some(false),
        isInsurancePremiumsPayedForSomeoneElse = Some(false), value = Some(1000), shareValue = Some(1000),
        policyInDeceasedName = Some(false), isJointlyOwned = Some(false), isInTrust = Some(false),
        moreThanMaxValue = Some(true))

      insurancePolicy.isComplete shouldBe Some(true)
    }

    "returns Some(false) if InsurancePolicy is not complete" in {
      val insurancePolicy = CommonBuilder.buildInsurancePolicy.copy(None,
        Some(true), Some(BigDecimal(100)), Some(BigDecimal(100)), Some(true), Some(true), Some(true), Some(true))

      insurancePolicy.isComplete shouldBe Some(false)
    }

    "returns None if every field is None in InsurancePolicy" in {
      val insurancePolicy = CommonBuilder.buildInsurancePolicy
      insurancePolicy.isComplete shouldBe empty
    }
  }

}
