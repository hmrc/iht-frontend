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

package iht.utils

import iht.config.AppConfig
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails

trait EstateNotDeclarableHelper extends RegistrationDetailsHelper {
  implicit val appConfig: AppConfig

  def isEstateOverGrossEstateLimit(appDetails: ApplicationDetails): Boolean = appDetails.totalValue > appConfig.grossEstateLimit.toInt

  def isEstateValueMoreThanTaxThresholdBeforeExemptionsStarted(appDetails: ApplicationDetails): Boolean = {
    appDetails.totalValue > appConfig.exemptionsThresholdValue.toInt && appDetails.allExemptions.isEmpty &&
      appDetails.widowCheck.isEmpty && appDetails.increaseIhtThreshold.isEmpty
  }

  def isEstateValueMoreThanTaxThresholdBeforeTnrbStarted(appDetails: ApplicationDetails,
                                                         regDetails: RegistrationDetails): Boolean = {
    val netEstateValue = appDetails.netValueAfterExemptionAndDebtsForPositiveExemption

    netEstateValue > appConfig.exemptionsThresholdValue.toInt && netEstateValue <= appConfig.transferredNilRateBand.toInt &&
      appDetails.allExemptions.isDefined && appDetails.widowCheck.isEmpty &&
      !getMaritalStatus(regDetails).equals(appConfig.statusSingle)
  }

  def isEstateValueMoreThanTaxThresholdBeforeTnrbFinished(appDetails: ApplicationDetails,
                                                          regDetails: RegistrationDetails): Boolean = {
    val netEstateValue = appDetails.netValueAfterExemptionAndDebtsForPositiveExemption

    netEstateValue > appConfig.exemptionsThresholdValue.toInt && netEstateValue <= appConfig.transferredNilRateBand.toInt &&
      appDetails.increaseIhtThreshold.isDefined && !getMaritalStatus(regDetails).equals(appConfig.statusSingle)
  }

}
