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

package iht.forms

import iht.config.AppConfig
import iht.forms.mappings.DateMapping
import iht.models.application.tnrb.{TnrbEligibiltyModel, WidowCheck}
import iht.utils._
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}
import iht.utils.IhtFormValidator
import iht.utils.IhtFormValidator._

/**

 *
 * Form for iht.views.application.tnrb_Eligibilty Page
 *
 */
object TnrbForms {
  // Widow check form.
  private def isWidowedRequiredConstraint: Constraint[WidowCheck] = Constraint({
    case WidowCheck(None, _) => Invalid("error.selectAnswer")
    case _ => Valid
  })

  private def widowCheckRequiredDataConstraint: Constraint[WidowCheck] = Constraint({
    case WidowCheck(Some(true), Some(_)) => Valid
    case WidowCheck(Some(false), None) => Valid
    case _ => Invalid("error.date.blank")
  })

  val partnerLivingInUkForm = Form(mapping(
    "isPartnerLivingInUk" -> yesNoQuestion("error.isPartnerLivingInUk.select")
  )(
    (isPartnerLivingInUk) => TnrbEligibiltyModel(isPartnerLivingInUk, None, None, None ,None, None, None, None, None, None, None)
  )
  (
    (tnrbModel: TnrbEligibiltyModel) => Some(tnrbModel.isPartnerLivingInUk)
  )
  )

  val giftMadeBeforeDeathForm = Form(mapping(
    "isGiftMadeBeforeDeath" -> yesNoQuestion("error.isGiftMadeBeforeDeath.select")
  )(
    (isGiftMadeBeforeDeath) => TnrbEligibiltyModel(None, isGiftMadeBeforeDeath, None, None ,None, None, None, None, None, None, None)
  )
  (
    (tnrbModel: TnrbEligibiltyModel) => Some(tnrbModel.isGiftMadeBeforeDeath)
  )
  )

  val estateClaimAnyBusinessForm = Form(mapping(
    "isStateClaimAnyBusiness" -> yesNoQuestion("error.isStateClaimAnyBusiness.select")
  )(
    (isStateClaimAnyBusiness) => TnrbEligibiltyModel(None, None, isStateClaimAnyBusiness, None ,None, None, None, None, None, None, None)
  )
  (
    (tnrbModel: TnrbEligibiltyModel) => Some(tnrbModel.isStateClaimAnyBusiness)
  )
  )

  val partnerGiftWithResToOtherForm = Form(mapping(
    "isPartnerGiftWithResToOther" -> yesNoQuestion("error.isPartnerGiftWithResToOther.select")
  )(
    (isPartnerGiftWithResToOther) => TnrbEligibiltyModel(None, None, None, isPartnerGiftWithResToOther ,None, None, None, None, None, None, None)
  )
  (
    (tnrbModel: TnrbEligibiltyModel) => Some(tnrbModel.isPartnerGiftWithResToOther)
  )
  )

  val benefitFromTrustForm = Form(mapping(
    "isPartnerBenFromTrust" -> yesNoQuestion("error.isPartnerBenFromTrust.select")
  )(
    (isPartnerBenFromTrust) => TnrbEligibiltyModel(None, None, None, None ,isPartnerBenFromTrust, None, None, None, None, None, None)
  )
  (
    (tnrbModel: TnrbEligibiltyModel) => Some(tnrbModel.isPartnerBenFromTrust)
  )
  )

  val estatePassedToDeceasedOrCharityForm = Form(mapping(
    "isEstateBelowIhtThresholdApplied" -> yesNoQuestion("error.isEstateBelowIhtThresholdApplied.select")
  )(
    (isEstateBelowIhtThresholdApplied) => TnrbEligibiltyModel(None, None, None, None ,None, isEstateBelowIhtThresholdApplied, None, None, None, None, None)
  )
  (
    (tnrbModel: TnrbEligibiltyModel) => Some(tnrbModel.isEstateBelowIhtThresholdApplied)
  )
  )

  val jointAssetPassedForm = Form(mapping(
    "isJointAssetPassed" -> yesNoQuestion("error.isJointAssetPassed.select")
  )(
    (isJointAssetPassed) => TnrbEligibiltyModel(None, None, None, None ,None, None, isJointAssetPassed, None, None, None, None)
  )
  (
    (tnrbModel: TnrbEligibiltyModel) => Some(tnrbModel.isJointAssetPassed)
  )
  )

  val deceasedWidowCheckQuestionForm = Form(mapping(
    "widowed" -> yesNoQuestion("error.widowed.select")
  )(
    (widowed) => WidowCheck(widowed, None)
  )
  (
    (widowCheckModel: WidowCheck) => Some(widowCheckModel.widowed)
  )
  )

  val deceasedWidowCheckDateForm = Form(mapping(
    "dateOfPreDeceased" -> DateMapping.dateOfDeath
  )(
    (dateOfPreDeceased) => WidowCheck(None, Some(dateOfPreDeceased))
  )
  (
    (widowCheckModel: WidowCheck) => widowCheckModel.dateOfPreDeceased
  )
  )

  def partnerNameForm(implicit appConfig: AppConfig) = Form(mapping(
    "firstName" -> of(IhtFormValidator.validatePartnerName(
      "lastName"
    )),
    "lastName" -> optional(text)
  )(
    (firstName, lastName) => TnrbEligibiltyModel(None, None, None, None ,None, None, None, firstName, lastName, None, None)
  )
  (
    (tnrbModel: TnrbEligibiltyModel) => Some((tnrbModel.firstName, tnrbModel.lastName))
  )
  )

  val dateOfMarriageForm = Form(mapping(
    "dateOfMarriage" -> DateMapping.dateOfMarriage
  )(
    (dateOfMarriage) => TnrbEligibiltyModel(None, None, None, None ,None, None, None, None, None, Some(dateOfMarriage), None)
  )
  (
    (tnrbModel: TnrbEligibiltyModel) => tnrbModel.dateOfMarriage
  )
  )
}
