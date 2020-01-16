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

package iht.viewmodels.application.overview

import iht.config.AppConfig
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.CommonHelper._
import iht.utils.{AppKickoutFixture, CommonHelper, RegistrationDetailsHelperFixture}


sealed abstract class DeclarationSectionStatus

case object InComplete extends DeclarationSectionStatus
case object NotDeclarable extends DeclarationSectionStatus
case object Declarable extends DeclarationSectionStatus
case class DeclarationSectionViewModel(ihtReference: String,
                                       declarationSectionStatus: DeclarationSectionStatus)

object DeclarationSectionViewModel {

  def apply(regDetails: RegistrationDetails, appDetails: ApplicationDetails)(implicit appConfig: AppConfig): DeclarationSectionViewModel =
    DeclarationSectionViewModel(
      ihtReference = CommonHelper.getOrException(regDetails.ihtReference),
      declarationSectionStatus = isReadyToDeclare(regDetails, appDetails))


  private def isReadyToDeclare(regDetails: RegistrationDetails,
                               appDetails: ApplicationDetails)(implicit appConfig: AppConfig): DeclarationSectionStatus = {

    if (areAllSectionsCompleted(regDetails, appDetails)) {
      getDeclarationStatus(regDetails, appDetails)
    } else {
      InComplete
    }
  }

  /**
    * Returns true if assets, gifts, debts, exemptions and tnrb sections are completed.
    * Note - exemptions and tnrb are treated as completed in case where these have not started at all.
    *
    * @param regDetails
    * @param appDetails
    * @return
    */
  private def areAllSectionsCompleted(regDetails: RegistrationDetails,
                                      appDetails: ApplicationDetails)(implicit appConfig: AppConfig) = {
    appDetails.areAllAssetsGiftsAndDebtsCompleted && appDetails.allExemptions.fold(true) {
      _ => RegistrationDetailsHelperFixture().isExemptionsCompleted(regDetails, appDetails)
    } && isTrnrbFlowCompleted(appDetails)
  }

  private def isTrnrbFlowCompleted(appDetails: ApplicationDetails) =
    appDetails.widowCheck.fold(true) (_.widowed.fold(false)(x => !x) || appDetails.isSuccessfulTnrbCase)

  /**
    * Returns the DeclarationStatus  - Declarable/NotDeclarable
    *
    * @param regDetails
    * @param appDetails
    * @return
    */
  private def getDeclarationStatus(regDetails: RegistrationDetails, appDetails: ApplicationDetails)
                                  (implicit appConfig: AppConfig): DeclarationSectionStatus = {

    val netEstateValue = appDetails.netValueAfterExemptionAndDebtsForPositiveExemption
    val tnrb = appDetails.increaseIhtThreshold
    val appDetailsUpdatedWithKickOut = AppKickoutFixture().appKickoutUpdateKickout(
                                                checks = AppKickoutFixture().checksBackend,
                                                registrationDetails = regDetails,
                                                applicationDetails = appDetails)
    val kickOutReason = appDetailsUpdatedWithKickOut.kickoutReason
    val maritalStatus = getOrException(getOrException(regDetails.deceasedDetails).maritalStatus)

    if (netEstateValue <= appConfig.exemptionsThresholdValue.toInt
      && tnrb.isEmpty && kickOutReason.isEmpty) {
      Declarable
    } else if (netEstateValue <= appConfig.transferredNilRateBand.toInt &&
      tnrb.isDefined && !maritalStatus.equals(appConfig.statusSingle) && kickOutReason.isEmpty) {
      Declarable
    } else {
      NotDeclarable
    }
  }
}
