/*
 * Copyright 2020 HM Revenue & Customs
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
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class AllExemptionsTest extends UnitSpec with MockitoSugar {
  def populatePartnerExemption = {
    PartnerExemption(isAssetForDeceasedPartner = Some(false),
      isPartnerHomeInUK = None,
      firstName = None,
      lastName = None,
      dateOfBirth = None,
      nino = None,
      totalAssets = None
    )
  }

  def populateModelWithPartnerExemption = {
    val partner = Some(populatePartnerExemption)
    val charityList = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = Some(false)))
    val qualifyingBodies = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = Some(false)))
    val exemptions = CommonBuilder.buildAllExemptions.copy(partner, charityList, qualifyingBodies)

    CommonBuilder.buildApplicationDetails.copy(allExemptions = Some(exemptions))
  }

  def populateModelWithoutPartnerExemption = {
    val charityList = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = Some(false)))
    val qualifyingBodies = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = Some(false)))
    val exemptions = CommonBuilder.buildAllExemptions.copy(None, charityList, qualifyingBodies)

    CommonBuilder.buildApplicationDetails.copy(allExemptions = Some(exemptions))
  }

  def populateModelWithCharityExemption = {
    val charityList = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = Some(true)))
    val qualifyingBodies = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = Some(false)))
    val exemptions = CommonBuilder.buildAllExemptions.copy(None, charityList, qualifyingBodies)

    CommonBuilder.buildApplicationDetails.copy(allExemptions = Some(exemptions))
  }

  "AllExemptions isExemptionsSectionCompletedWithNoValue" must {
    "return true if completed with no value" in {
      populateModelWithPartnerExemption.allExemptions.map(_
        .isExemptionsSectionCompletedWithNoValue) shouldBe Some(true)
    }

    "return false if not completed" in {
      populateModelWithoutPartnerExemption.allExemptions.map(_
        .isExemptionsSectionCompletedWithNoValue) shouldBe Some(false)
    }
  }

  "AllExemptions isExemptionsSectionCompletedWithoutPartnerExemptionWithNoValue" must {
    "return true if completed without partner exemption with no value" in {
      populateModelWithoutPartnerExemption.allExemptions.map(_
        .isExemptionsSectionCompletedWithoutPartnerExemptionWithNoValue) shouldBe Some(true)
    }

    "return false if not completed without partner exemption with no value" in {
      populateModelWithCharityExemption.allExemptions.map(_
        .isExemptionsSectionCompletedWithoutPartnerExemptionWithNoValue) shouldBe Some(false)
    }
  }

  "PartnerExemption isComplete" must {
    "return true if complete" in {
      populatePartnerExemption.isComplete shouldBe Some(true)
    }
    "return false if not complete" in {
      populatePartnerExemption.copy(
        isAssetForDeceasedPartner = Some(true),
        isPartnerHomeInUK = None
      ).isComplete shouldBe Some(false)
    }
  }

  "PartnerExemption name" must {
    "return name if defined" in {
      populatePartnerExemption.copy(
        firstName = Some("a"),
        lastName = Some("b")
      ).name shouldBe Some("a b")
    }

    "return none if not defined" in {
      populatePartnerExemption.copy(
        firstName = None,
        lastName = None
      ).name shouldBe None
    }
  }
}
