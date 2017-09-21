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

  def haveProvidedNonMatchingDetailsMsg(messages: Messages, deceasedName:String) = messages(
    "iht.estateReport.declaration.haveProvidedNonMatchingDetails", deceasedName)
  def completedAllReasonableEnquiriesMsg(messages: Messages, deceasedName:String) = messages(
    "iht.estateReport.declaration.completedAllReasonableEnquiries", deceasedName)
  def deceasedMarriedWhenPartnerDied(messages: Messages, deceasedName: String) = messages(
    "iht.estateReport.declaration.deceasedMarriedWhenPartnerDied", deceasedName)
  def correctAndCompleteMsg(messages: Messages) = messages("iht.estateReport.declaration.correctAndComplete")
  def didNotUseAnyOfThresholdMsg(messages: Messages) = messages("iht.estateReport.declaration.didntUseAnyOfThreshold")
  def noInheritanceTaxPayableMsg(messages: Messages)= messages("iht.estateReport.noInheritanceTaxPayable")
  def estateValueBeforeExemptionsLessThan1MillionMsg(messages: Messages) =
    messages("iht.estateReport.declaration.estateValueBeforeExemptionsLessThan1Million")

  def summaryText(declarationType: String, isMultipleExecutor: Boolean)(implicit messages: Messages) = {
    messages(
      isMultipleExecutor match {
        case false => "iht.estateReport.declaration.youMayFaceProsecution"
        case true => "iht.estateReport.declaration.coExecutors.mayFaceProsecution"
      }
    )
  }

  def summaryBullet3Text(declarationType: String, isMultipleExecutor: Boolean, deceasedName:String)(implicit messages: Messages) =
    (isMultipleExecutor, declarationType) match {
      case (false, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(haveProvidedNonMatchingDetailsMsg(messages, deceasedName))
      case (false, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(haveProvidedNonMatchingDetailsMsg(messages, deceasedName))
      case (true, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(haveProvidedNonMatchingDetailsMsg(messages, deceasedName))
      case (true, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(haveProvidedNonMatchingDetailsMsg(messages, deceasedName))
      case _ => None
    }

 def mainBullet1Text(declarationType: String, isMultipleExecutor: Boolean, deceasedName: String) (implicit messages: Messages) =
   (isMultipleExecutor, declarationType) match {
        case (false, DeclarationReason.ValueLessThanNilRateBand) => Some(completedAllReasonableEnquiriesMsg(messages, deceasedName))
        case (false, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(completedAllReasonableEnquiriesMsg(messages, deceasedName))
        case (false, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(deceasedMarriedWhenPartnerDied(messages, deceasedName))
        case (false, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(deceasedMarriedWhenPartnerDied(messages, deceasedName))
        case (true, DeclarationReason.ValueLessThanNilRateBand) => Some(completedAllReasonableEnquiriesMsg(messages, deceasedName))
        case (true, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(completedAllReasonableEnquiriesMsg(messages, deceasedName))
        case (true, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(deceasedMarriedWhenPartnerDied(messages, deceasedName))
        case (true, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(deceasedMarriedWhenPartnerDied(messages, deceasedName))
        case _ => None
  }

  def mainBullet2Text(declarationType: String, isMultipleExecutor: Boolean) (implicit messages: Messages)=
    (isMultipleExecutor, declarationType) match {
        case (false, DeclarationReason.ValueLessThanNilRateBand) => Some(correctAndCompleteMsg(messages))
        case (false, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(correctAndCompleteMsg(messages))
        case (false, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(didNotUseAnyOfThresholdMsg(messages))
        case (false, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(didNotUseAnyOfThresholdMsg(messages))
        case (true, DeclarationReason.ValueLessThanNilRateBand) => Some(correctAndCompleteMsg(messages))
        case (true, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(correctAndCompleteMsg(messages))
        case (true, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(didNotUseAnyOfThresholdMsg(messages))
        case (true, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(didNotUseAnyOfThresholdMsg(messages))
        case _ => None
      }


  def mainBullet3Text(declarationType: String, isMultipleExecutor: Boolean, deceasedName: String) (implicit messages: Messages)=
   (isMultipleExecutor, declarationType) match {
        case (false, DeclarationReason.ValueLessThanNilRateBand) => Some(noInheritanceTaxPayableMsg(messages))
        case (false, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(estateValueBeforeExemptionsLessThan1MillionMsg(messages))
        case (false, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(completedAllReasonableEnquiriesMsg(messages, deceasedName))
        case (false, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(completedAllReasonableEnquiriesMsg(messages, deceasedName))
        case (true, DeclarationReason.ValueLessThanNilRateBand) => Some(noInheritanceTaxPayableMsg(messages))
        case (true, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(estateValueBeforeExemptionsLessThan1MillionMsg(messages))
        case (true, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(completedAllReasonableEnquiriesMsg(messages, deceasedName))
        case (true, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(completedAllReasonableEnquiriesMsg(messages, deceasedName))
        case _ => None
  }

  def mainBullet4Text(declarationType: String, isMultipleExecutor: Boolean)(implicit messages: Messages) =
    (isMultipleExecutor, declarationType) match {
      case (false, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(noInheritanceTaxPayableMsg(messages))
      case (false, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(correctAndCompleteMsg(messages))
      case (false, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(correctAndCompleteMsg(messages))
      case (true, DeclarationReason.ValueLessThanNilRateBandAfterExemption) => Some(noInheritanceTaxPayableMsg(messages))
      case (true, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(correctAndCompleteMsg(messages))
      case (true, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(correctAndCompleteMsg(messages))
      case _ => None
    }

  def mainBullet5Text(declarationType: String, isMultipleExecutor: Boolean)(implicit messages: Messages) =
   (isMultipleExecutor, declarationType) match {
      case (false, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(noInheritanceTaxPayableMsg(messages))
      case (false, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) =>
                                      Some(estateValueBeforeExemptionsLessThan1MillionMsg(messages))
      case (true, DeclarationReason.ValueLessThanTransferredNilRateBand) => Some(noInheritanceTaxPayableMsg(messages))
      case (true, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) =>
                                      Some(estateValueBeforeExemptionsLessThan1MillionMsg(messages))
      case _ => None
    }

  def mainBullet6Text(declarationType: String, isMultipleExecutor: Boolean)(implicit messages: Messages) =
    (isMultipleExecutor, declarationType) match {
      case (false, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(noInheritanceTaxPayableMsg(messages))
      case (true, DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption) => Some(noInheritanceTaxPayableMsg(messages))
      case _ => None
   }

}
