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

package iht.models.application.exemptions

import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by vineet on 06/11/16.
  */
class PartnerExemptionTest extends UnitSpec with MockitoSugar{

  "isComplete" must {

    "return Some(true) when PartnerExemption is complete" in {
      val partnerExemption = CommonBuilder.buildPartnerExemption

      partnerExemption.isComplete shouldBe Some(true)
    }

    "return Some(true) when AssetForDeceasedPartner is selected as No" in {
      val partnerExemption = CommonBuilder.buildPartnerExemption.copy(
        isAssetForDeceasedPartner = Some(false))

      partnerExemption.isComplete shouldBe Some(true)
    }

    "return Some(false) when AssetForDeceasedPartner is selected as Yes and all other questions have not been answered" in {
      val partnerExemption = CommonBuilder.buildPartnerExemption.copy(
        isAssetForDeceasedPartner = Some(true),
        isPartnerHomeInUK = None,
        firstName = None,
        lastName = None,
        dateOfBirth = None,
        nino = None,
        totalAssets = None
      )

      partnerExemption.isComplete shouldBe Some(false)
    }

    "return None when all the fields are None" in {
      val partnerExemption = CommonBuilder.buildPartnerExemption.copy(None, None, None ,None, None, None, None)
      partnerExemption.isComplete shouldBe empty
    }

    "return None when AssetForDeceasedPartner is None" in {
      val partnerExemption = CommonBuilder.buildPartnerExemption.copy(isAssetForDeceasedPartner = None)
      partnerExemption.isComplete shouldBe empty
    }
  }

  "name" must {

    "return the name as option value when first and last name are entered" in {
      val firstName  = "first"
      val lastName = "last"

      val partnerExemption = CommonBuilder.buildPartnerExemption.copy(
        firstName = Some(firstName), lastName = Some(lastName))

      partnerExemption.name shouldBe Some(firstName+" "+lastName)
    }

    "return None when first and last name are not entered" in {
      val partnerExemption = CommonBuilder.buildPartnerExemption.copy(
        firstName = None, lastName = None)

      partnerExemption.name shouldBe empty
    }
  }
}
