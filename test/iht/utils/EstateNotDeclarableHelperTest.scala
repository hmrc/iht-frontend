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
import iht.models.application.exemptions.{AllExemptions, BasicExemptionElement, PartnerExemption}
import iht.testhelpers.CommonBuilder

class EstateNotDeclarableHelperTest extends FakeIhtApp with EstateNotDeclarableHelper {
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "EstateNotDeclarableHelper" when {

    "isEstateOverGrossEstateLimit is called" must {

      "return true if the estate is over the gross estate limit" in {
        val appDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder.buildAllAssets.copy(
          money = Some(CommonBuilder.buildShareableBasicElement.copy(value = Some(2000000))))))
        assert(isEstateOverGrossEstateLimit(appDetails))
      }

      "return false if the estate is not over the gross estate limit" in {
        val appDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder.buildAllAssets.copy(
          money = Some(CommonBuilder.buildShareableBasicElement.copy(value = Some(2000))))))
        assert(!isEstateOverGrossEstateLimit(appDetails))
      }

    }

    "isEstateValueMoreThanTaxThresholdBeforeExemptionsStarted" must {

      "return true if the estate value is more than the tax threshold " +
        "before exemptions has been started" in {
        val appDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder.buildAllAssets.copy(
          money = Some(CommonBuilder.buildShareableBasicElement.copy(value = Some(326000))))))
        assert(isEstateValueMoreThanTaxThresholdBeforeExemptionsStarted(appDetails))
      }

      "return false if the estate value is not more than the tax threshold " +
        "before exemptions has been started" in {
        val appDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder.buildAllAssets.copy(
          money = Some(CommonBuilder.buildShareableBasicElement.copy(value = Some(20000))))))
        assert(!isEstateValueMoreThanTaxThresholdBeforeExemptionsStarted(appDetails))
      }

    }

    "isEstateValueMoreThanTaxThresholdBeforeTnrbStarted" must {

      "return true if the estate value is more than the tax threshold " +
        "before TNRB has been started" in {
        val regDetails = CommonBuilder.buildRegistrationDetails4
        val appDetails = CommonBuilder.buildApplicationDetails.copy(
          allAssets = Some(CommonBuilder.buildAllAssets.copy(
          money = Some(CommonBuilder.buildShareableBasicElement.copy(value = Some(326000))))),
          allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
            partner = Some(CommonBuilder.buildPartnerExemption)
          )),
        widowCheck = None)
        assert(isEstateValueMoreThanTaxThresholdBeforeTnrbStarted(appDetails, regDetails))
      }

      "return false if the estate value is not more than the tax threshold " +
        "before TNRB has been started" in {
        val regDetails = CommonBuilder.buildRegistrationDetails4
        val appDetails = CommonBuilder.buildApplicationDetails.copy(
          allAssets = Some(CommonBuilder.buildAllAssets.copy(
            money = Some(CommonBuilder.buildShareableBasicElement.copy(value = Some(326000))))),
          allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
            partner = Some(CommonBuilder.buildPartnerExemption.copy(totalAssets = Some(BigDecimal(26000))))
          )),
        widowCheck = None)
        assert(!isEstateValueMoreThanTaxThresholdBeforeTnrbStarted(appDetails, regDetails))
      }

    }

    "isEstateValueMoreThanTaxThresholdBeforeTnrbFinished" must {

      "return true if the estate value is more than the tax threshold " +
        "after TNRB has been started but before TNRB has been finished" in {
        val regDetails = CommonBuilder.buildRegistrationDetails4

        val appDetails = CommonBuilder.buildApplicationDetails.copy(
          allAssets = Some(CommonBuilder.buildAllAssets.copy(
            money = Some(CommonBuilder.buildShareableBasicElement.copy(value = Some(326000))))),
          allExemptions = Some(AllExemptions(
            partner =
              Some(PartnerExemption(
                isAssetForDeceasedPartner = Some(false),
                isPartnerHomeInUK = None,
                firstName = None,
                lastName = None,
                dateOfBirth = None,
                nino = None,
                totalAssets = None)),
            charity = Some(BasicExemptionElement(Some(false))),
            qualifyingBody = Some(BasicExemptionElement(Some(false))))),
        widowCheck = Some(CommonBuilder.buildWidowedCheck),
        increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility))

        assert(isEstateValueMoreThanTaxThresholdBeforeTnrbFinished(appDetails, regDetails))
      }

      "return false if the estate value is not more than the tax threshold " +
        "after TNRB has been started but before TNRB has been finished" in {
        val regDetails = CommonBuilder.buildRegistrationDetails4

        val appDetails = CommonBuilder.buildApplicationDetails.copy(
          allAssets = Some(CommonBuilder.buildAllAssets.copy(
            money = Some(CommonBuilder.buildShareableBasicElement.copy(value = Some(324000))))),
          allExemptions = Some(AllExemptions(
            partner =
              Some(PartnerExemption(
                isAssetForDeceasedPartner = Some(false),
                isPartnerHomeInUK = None,
                firstName = None,
                lastName = None,
                dateOfBirth = None,
                nino = None,
                totalAssets = None)),
            charity = Some(BasicExemptionElement(Some(false))),
            qualifyingBody = Some(BasicExemptionElement(Some(false))))),
          widowCheck = Some(CommonBuilder.buildWidowedCheck),
          increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility))

        assert(!isEstateValueMoreThanTaxThresholdBeforeTnrbFinished(appDetails, regDetails))
      }

    }

  }

}
