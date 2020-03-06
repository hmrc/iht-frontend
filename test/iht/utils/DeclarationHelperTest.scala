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

package iht.utils

import iht.controllers.application.ApplicationControllerTest
import iht.models.application.debts.BasicEstateElementLiabilities
import iht.models.application.exemptions.BasicExemptionElement
import iht.testhelpers.CommonBuilder
import iht.testhelpers.CommonBuilder._
import org.joda.time.LocalDate
import uk.gov.hmrc.http.HeaderCarrier

/**
  * Created by david-beer on 21/11/16.
  */
class DeclarationHelperTest extends ApplicationControllerTest {

  implicit val hc = new HeaderCarrier

  before {
    createMockToGetApplicationDetails(mockIhtConnector)
    createMockToGetRealtimeRiskMessage(mockIhtConnector, None)
  }

  "declaration type" must {

    val appDetails = buildApplicationDetails

    "return declaration type as ValueLessThanNilRateBand  when total assets value is <=325 K after assets and gifts" +
      "with no exemption and no tnrb " in {
      implicit val fakeRequest = createFakeRequest()
      val appDetailsWithRequiredValues = buildApplicationDetailsWithAllAssets

      DeclarationHelper.getDeclarationType(appDetailsWithRequiredValues) must be (DeclarationReason.ValueLessThanNilRateBand)
    }

    "return declaration type as ValueLessThanNilRateBandAfterExemption when assets, gifts and debts value is " +
      "<= 325 k after exemptions and no tnrb" in {
      implicit val fakeRequest = createFakeRequest()

      val appDetailsWithRequiredValues = appDetails.copy(
        allAssets = Some(buildAllAssetsWithAllSectionsFilled),
        allGifts = Some(buildAllGiftsWithValues),
        giftsList = Some(buildGiftsList),
        allLiabilities = Some(buildAllLiabilitiesWithAllSectionsFilled.copy(funeralExpenses =
          Some(BasicEstateElementLiabilities(value = Some(BigDecimal(4000)), isOwned = Some(true))))),
        allExemptions = Some(buildAllExemptions.copy(
          charity = Some(BasicExemptionElement(Some(true)) ))),
        charities = Seq(charity, charity))

      DeclarationHelper.getDeclarationType(appDetailsWithRequiredValues) mustBe (DeclarationReason.ValueLessThanNilRateBandAfterExemption)
    }

    "return declaration type as ValueLessThanTransferredNilRateBand  when assets, gifts and debts value is " +
      "<=650 K and eligible for tnrb but no exemptions " in {
      implicit val fakeRequest = createFakeRequest()

      val appDetailsWithRequiredValues = appDetails.copy(
        allAssets = Some(buildAllAssetsWithAllSectionsFilled),
        allGifts = Some(buildAllGiftsWithValues),
        giftsList = Some(buildGiftsList),
        allLiabilities = Some(buildAllLiabilitiesWithAllSectionsFilled.copy(funeralExpenses =
          Some(BasicEstateElementLiabilities(value = Some(BigDecimal(4000)), isOwned = Some(true))))),
        increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility.copy(
          dateOfPreDeceased = Some(new LocalDate(1988, 12, 11))))
      )

      DeclarationHelper.getDeclarationType(appDetailsWithRequiredValues) mustBe (DeclarationReason.ValueLessThanTransferredNilRateBand)
    }

    "return declare type as ValueLessThanTransferredNilRateBandAfterExemption when assets, gifts and debts value is" +
      "<=650 K and eligible for tnrb and has exemptions" in {
      implicit val fakeRequest = createFakeRequest()

      val appDetailsWithRequiredValues = appDetails.copy(
        allAssets = Some(buildAllAssetsWithAllSectionsFilled),
        allGifts = Some(buildAllGiftsWithValues),
        giftsList = Some(buildGiftsList),
        allLiabilities = Some(buildAllLiabilitiesWithAllSectionsFilled.copy(funeralExpenses =
          Some(BasicEstateElementLiabilities(value = Some(BigDecimal(4000)), isOwned = Some(true))))),
        allExemptions = Some(buildAllExemptions.copy(
          charity = Some(BasicExemptionElement(Some(true)) ))),
        charities = Seq(charity, charity),
        increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility.copy(
          dateOfPreDeceased = Some(new LocalDate(1988, 12, 11))))
      )

      DeclarationHelper.getDeclarationType(appDetailsWithRequiredValues) mustBe DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption
    }
  }

}
