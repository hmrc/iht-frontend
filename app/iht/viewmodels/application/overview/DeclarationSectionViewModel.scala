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

package iht.viewmodels.application.overview

import iht.constants.IhtProperties
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.CommonHelper._
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import play.api.i18n.Messages.Implicits._


/**
  * Created by vineet on 17/10/16.
  */

sealed abstract class DeclarationSectionStatus

case object InComplete extends DeclarationSectionStatus

case object NotDeclarable extends DeclarationSectionStatus

case object Declarable extends DeclarationSectionStatus

case class DeclarationSectionViewModel(ihtReference: String,
                                       declarationSectionStatus: DeclarationSectionStatus)

object DeclarationSectionViewModel {

  def apply(regDetails: RegistrationDetails,
            appDetails: ApplicationDetails): DeclarationSectionViewModel =
    DeclarationSectionViewModel(
      ihtReference = CommonHelper.getOrException(regDetails.ihtReference),
      declarationSectionStatus = isReadyToDeclare(regDetails, appDetails))


  private def isReadyToDeclare(regDetails: RegistrationDetails,
                               appDetails: ApplicationDetails): DeclarationSectionStatus = {

    areAllSectionsCompleted(regDetails, appDetails) match {
      case false => InComplete
      case _ => getDeclarationStatus(regDetails, appDetails)
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
                                      appDetails: ApplicationDetails) = {
    appDetails.areAllAssetsGiftsAndDebtsCompleted && appDetails.allExemptions.fold(true) {
      _ => isExemptionsCompleted(regDetails, appDetails)
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
  private def getDeclarationStatus(regDetails: RegistrationDetails,
                                   appDetails: ApplicationDetails): DeclarationSectionStatus = {

    val netEstateValue = appDetails.netValueAfterExemptionAndDebtsForPositiveExemption
    val tnrb = appDetails.increaseIhtThreshold
    val appDetailsUpdatedWithKickOut = ApplicationKickOutHelper.updateKickout(
                                                checks = ApplicationKickOutHelper.checksBackend,
                                                registrationDetails = regDetails,
                                                applicationDetails = appDetails)
    val kickOutReason = appDetailsUpdatedWithKickOut.kickoutReason
    val maritalStatus = getOrException(getOrException(regDetails.deceasedDetails).maritalStatus)

    if (netEstateValue <= IhtProperties.exemptionsThresholdValue.toInt
      && tnrb.isEmpty && kickOutReason.isEmpty) {
      Declarable
    } else if (netEstateValue <= IhtProperties.transferredNilRateBand.toInt &&
      tnrb.isDefined && !maritalStatus.equals(IhtProperties.statusSingle) && kickOutReason.isEmpty) {
      Declarable
    } else {
      NotDeclarable
    }
  }
}
