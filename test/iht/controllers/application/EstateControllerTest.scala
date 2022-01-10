/*
 * Copyright 2022 HM Revenue & Customs
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

import iht.config.AppConfig
import iht.models.application.assets._
import iht.testhelpers.CommonBuilder
import iht.utils.{ApplicationStatus, KickOutReason}
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class EstateControllerTest extends ApplicationControllerTest {

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC123"))

  protected abstract class TestController extends FrontendController(mockControllerComponents) with EstateController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def estateController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "EstateController" must {
    "update application details with correct kickout reason and status for TrustsMoreThanOne" in {
      CommonBuilder.buildApplicationDetailsForKickout(KickOutReason.TrustsMoreThanOne) foreach { ad =>
        val result = estateController.appKickoutUpdateKickout(registrationDetails = registrationDetails, applicationDetails=ad)
        result.status mustBe ApplicationStatus.KickOut
        result.kickoutReason mustBe Some(KickOutReason.TrustsMoreThanOne)
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

      result must be (assetsWithInsurancePolicy3)

    }
  }
}
