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

package iht.utils

import iht.constants.IhtProperties
import iht.models.application.ApplicationDetails

/**
  * Created by david-beer on 15/11/16.
  */
object DeclarationHelper {

  def getDeclarationType(appDetails: ApplicationDetails): String  = {

    val totalAssetsValue = appDetails.totalAssetsValue
    val totalGiftsValue = CommonHelper.getOrZero(appDetails.totalPastYearsGiftsOption)
    val totalExemptionsValue = appDetails.totalExemptionsValue
    val thresholdValue = IhtProperties.exemptionsThresholdValue
    val tnrbThresholdValue = IhtProperties.transferredNilRateBand

    if(totalAssetsValue + totalGiftsValue <= thresholdValue
      && totalExemptionsValue == BigDecimal(0) && appDetails.increaseIhtThreshold.isEmpty) {

      // Declaration type 1
      DeclarationReason.ValueLessThanNilRateBand
    } else if (appDetails.netValueAfterExemptionAndDebtsForPositiveExemption <= thresholdValue &&
      totalExemptionsValue > 0 && appDetails.increaseIhtThreshold.isEmpty ) {

      // Declaration type 2
      DeclarationReason.ValueLessThanNilRateBandAfterExemption
    } else if(totalAssetsValue + totalGiftsValue <= tnrbThresholdValue &&
      totalExemptionsValue == BigDecimal(0) &&
      appDetails.increaseIhtThreshold.fold(false)(_.areAllQuestionsAnswered)) {

      // Declaration type 3
      DeclarationReason.ValueLessThanTransferredNilRateBand
    } else {
      // Declaration type 4
      DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption
    }
  }

}
