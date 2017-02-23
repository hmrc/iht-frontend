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

package iht.views.application.declaration

import iht.utils.DeclarationReason
import play.api.i18n.Messages
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

/**
  * Created by vineettyagi on 1/12/16.
  *
  * This helper has been kept in views package as its only used in declaration view
  */
object DeclarationViewHelper {

  lazy val haveProvidedNonMatchingDetailsMsg = Messages("iht.estateReport.declaration.haveProvidedNonMatchingDetails")
  lazy val completedAllReasonableEnquiriesMsg = Messages("iht.estateReport.declaration.completedAllReasonableEnquiries")
  lazy val deceasedMarriedWhenPartnerDied = Messages("iht.estateReport.declaration.deceasedMarriedWhenPartnerDied")
  lazy val correctAndCompleteMsg  = Messages("iht.estateReport.declaration.correctAndComplete")
  lazy val didNotUseAnyOfThresholdMsg = Messages("iht.estateReport.declaration.didntUseAnyOfThreshold")
  lazy val noInheritanceTaxPayableMsg= Messages("iht.estateReport.noInheritanceTaxPayable")
  lazy val estateValueBeforeExemptionsLessThan1MillionMsg =
                                      Messages("iht.estateReport.declaration.estateValueBeforeExemptionsLessThan1Million")

  def summaryText(declarationType: String, isMultipleExecutor: Boolean) = {
    Messages(
      isMultipleExecutor match {
        case false => "iht.estateReport.declaration.youMayFaceProsecution"
        case true => "iht.estateReport.declaration.coExecutors.mayFaceProsecution"
      }
    )
  }

  def summaryBullet3Text(declarationType: String, isMultipleExecutor: Boolean) =
    (isMultipleExecutor, declarationType) match {
      case (false, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(haveProvidedNonMatchingDetailsMsg)
      case (false, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(haveProvidedNonMatchingDetailsMsg)
      case (true, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(haveProvidedNonMatchingDetailsMsg)
      case (true, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(haveProvidedNonMatchingDetailsMsg)
      case _ => None
    }

 def mainBullet1Text(declarationType: String, isMultipleExecutor: Boolean) =
   (isMultipleExecutor, declarationType) match {
        case (false, DeclarationReason.ValueLessThanNilRateBand) => Some(completedAllReasonableEnquiriesMsg)
        case (false, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(completedAllReasonableEnquiriesMsg)
        case (false, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(deceasedMarriedWhenPartnerDied)
        case (false, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(deceasedMarriedWhenPartnerDied)
        case (true, DeclarationReason.ValueLessThanNilRateBand) => Some(completedAllReasonableEnquiriesMsg)
        case (true, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(completedAllReasonableEnquiriesMsg)
        case (true, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(deceasedMarriedWhenPartnerDied)
        case (true, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(deceasedMarriedWhenPartnerDied)
        case _ => None
  }

  def mainBullet2Text(declarationType: String, isMultipleExecutor: Boolean) =
    (isMultipleExecutor, declarationType) match {
        case (false, DeclarationReason.ValueLessThanNilRateBand) => Some(correctAndCompleteMsg)
        case (false, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(correctAndCompleteMsg)
        case (false, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(didNotUseAnyOfThresholdMsg)
        case (false, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(didNotUseAnyOfThresholdMsg)
        case (true, DeclarationReason.ValueLessThanNilRateBand) => Some(correctAndCompleteMsg)
        case (true, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(correctAndCompleteMsg)
        case (true, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(didNotUseAnyOfThresholdMsg)
        case (true, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(didNotUseAnyOfThresholdMsg)
        case _ => None
      }


  def mainBullet3Text(declarationType: String, isMultipleExecutor: Boolean) =
   (isMultipleExecutor, declarationType) match {
        case (false, DeclarationReason.ValueLessThanNilRateBand) => Some(noInheritanceTaxPayableMsg)
        case (false, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(estateValueBeforeExemptionsLessThan1MillionMsg)
        case (false, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(completedAllReasonableEnquiriesMsg)
        case (false, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(completedAllReasonableEnquiriesMsg)
        case (true, DeclarationReason.ValueLessThanNilRateBand) => Some(noInheritanceTaxPayableMsg)
        case (true, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(estateValueBeforeExemptionsLessThan1MillionMsg)
        case (true, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(completedAllReasonableEnquiriesMsg)
        case (true, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(completedAllReasonableEnquiriesMsg)
        case _ => None
  }

  def mainBullet4Text(declarationType: String, isMultipleExecutor: Boolean) =
    (isMultipleExecutor, declarationType) match {
      case (false, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(noInheritanceTaxPayableMsg)
      case (false, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(correctAndCompleteMsg)
      case (false, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(correctAndCompleteMsg)
      case (true, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(noInheritanceTaxPayableMsg)
      case (true, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(correctAndCompleteMsg)
      case (true, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(correctAndCompleteMsg)
      case _ => None
    }

  def mainBullet5Text(declarationType: String, isMultipleExecutor: Boolean) =
   (isMultipleExecutor, declarationType) match {
      case (false, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(noInheritanceTaxPayableMsg)
      case (false, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) =>
                                      Some(estateValueBeforeExemptionsLessThan1MillionMsg)
      case (true, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(noInheritanceTaxPayableMsg)
      case (true, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) =>
                                      Some(estateValueBeforeExemptionsLessThan1MillionMsg)
      case _ => None
    }

  def mainBullet6Text(declarationType: String, isMultipleExecutor: Boolean) =
    (isMultipleExecutor, declarationType) match {
      case (false, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(noInheritanceTaxPayableMsg)
      case (true, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(noInheritanceTaxPayableMsg)
      case _ => None
   }

}
