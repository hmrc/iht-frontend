/*
 * Copyright 2018 HM Revenue & Customs
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

package iht.controllers.application

import iht.connector.{CachingConnector, IhtConnector}
import iht.models.application.assets._
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.utils.{ApplicationStatus, KickOutReason}
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.http.HeaderCarrier

class EstateControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]
  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC123"))

  def estateController = new EstateController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "EstateController" must {
    "update application details with correct kickout reason and status for TrustsMoreThanOne" in {
      CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.TrustsMoreThanOne) foreach { ad =>
        val result = estateController.updateKickout(registrationDetails=registrationDetails, applicationDetails=ad)(createFakeRequest(), new HeaderCarrier)
        result.status shouldBe ApplicationStatus.KickOut
        result.kickoutReason shouldBe Some(KickOutReason.TrustsMoreThanOne)
      }
    }

    "update insurance policy correctly" in {
      val insurancePolicy1 = InsurancePolicy(
        isAnnuitiesBought = Some(true),
        isInsurancePremiumsPayedForSomeoneElse = Some(false),
        value = Some(BigDecimal(34)),
        shareValue = Some(BigDecimal(44)),
        policyInDeceasedName = Some(true),
        isJointlyOwned = Some(false),
        isInTrust = Some(true),
        coveredByExemption = Some(false),
        sevenYearsBefore = Some(true),
        moreThanMaxValue = Some(false)
      )

      val insurancePolicy2 = InsurancePolicy(
        isAnnuitiesBought = None,
        isInsurancePremiumsPayedForSomeoneElse = Some(true),
        value = Some(BigDecimal(66)),
        shareValue = None,
        policyInDeceasedName = Some(true),
        isJointlyOwned = None,
        isInTrust = Some(false),
        coveredByExemption = None,
        sevenYearsBefore = None,
        moreThanMaxValue = Some(false)
      )

      val insurancePolicy3 = InsurancePolicy(
        isAnnuitiesBought = Some(true),
        isInsurancePremiumsPayedForSomeoneElse = Some(true),
        value = Some(BigDecimal(66)),
        shareValue = Some(BigDecimal(44)),
        policyInDeceasedName = Some(true),
        isJointlyOwned = Some(false),
        isInTrust = Some(false),
        coveredByExemption = Some(false),
        sevenYearsBefore = Some(true),
        moreThanMaxValue = Some(false)
      )

      val assetsWithInsurancePolicy1 = AllAssets(
        insurancePolicy = Some(insurancePolicy1)
      )

      val assetsWithInsurancePolicy3 = AllAssets(
        insurancePolicy = Some(insurancePolicy3)
      )

      val result = estateController.updateAllAssetsWithInsurancePolicy(assetsWithInsurancePolicy1, insurancePolicy2,identity)

      result should be (assetsWithInsurancePolicy3)

    }
  }
}
