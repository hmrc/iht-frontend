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

package iht.models.application.exemptions

import iht.FakeIhtApp
import iht.config.AppConfig
import iht.testhelpers.CommonBuilder
import org.scalatestplus.mockito.MockitoSugar


class PartnerExemptionTest extends FakeIhtApp with MockitoSugar {

  implicit val mockAppConfig = app.injector.instanceOf[AppConfig]

  "isComplete" must {

    "return Some(true) when PartnerExemption is complete" in {
      val partnerExemption = CommonBuilder.buildPartnerExemption

      partnerExemption.isComplete mustBe  Some(true)
    }

    "return Some(true) when AssetForDeceasedPartner is selected as No" in {
      val partnerExemption = CommonBuilder.buildPartnerExemption.copy(
        isAssetForDeceasedPartner = Some(false))

      partnerExemption.isComplete mustBe  Some(true)
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

      partnerExemption.isComplete mustBe  Some(false)
    }

    "return None when all the fields are None" in {
      val partnerExemption = CommonBuilder.buildPartnerExemption.copy(None, None, None ,None, None, None, None)
      partnerExemption.isComplete mustBe  empty
    }

    "return None when AssetForDeceasedPartner is None" in {
      val partnerExemption = CommonBuilder.buildPartnerExemption.copy(isAssetForDeceasedPartner = None)
      partnerExemption.isComplete mustBe  empty
    }
  }

  "name" must {

    "return the name as option value when first and last name are entered" in {
      val firstName  = "first"
      val lastName = "last"

      val partnerExemption = CommonBuilder.buildPartnerExemption.copy(
        firstName = Some(firstName), lastName = Some(lastName))

      partnerExemption.name mustBe  Some(firstName+" "+lastName)
    }

    "return None when first and last name are not entered" in {
      val partnerExemption = CommonBuilder.buildPartnerExemption.copy(
        firstName = None, lastName = None)

      partnerExemption.name mustBe  empty
    }
  }

  "ninoFormatted" must {

    "return a properly formatted Nino as an option" in {
      val nino = "aa 12 34 56 b"

      val partnerExemption = CommonBuilder.buildPartnerExemption.copy(
        nino = Some(nino))

      partnerExemption.ninoFormatted mustBe  Some("AA123456B")
    }

    "return None when nino is not entered" in {
      val partnerExemption = CommonBuilder.buildPartnerExemption.copy(nino = None)

      partnerExemption.ninoFormatted mustBe  None
    }
  }
}
