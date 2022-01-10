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

package iht.controllers

object ControllerHelper {

  val SourceMultipleExecutor="multipleExecutors"
  val SourceRegSummary="registrationSummary"
  val SourceApplicantDetails="applicantDetails"
  val SourceDeceasedDetails="deceasedDetails"
  val SourceDeceasedDateOfDeath="deceasedDateOfDeath"
  val EditModeForApplicantDetails="change-your-details"
  val EditModeForDDOD="change-date-of-death"
  val EditModeForDeceasedDetails="change-deceased-details"
  val EditMode="edit"
  val StandardMode="standard"

  // Reasons for being below limit
  val ReasonForBeingBelowLimitExceptedEstate = "Excepted Estate"
  val ReasonForBeingBelowLimitTNRB = "Transferred Nil Rate Band"
  val ReasonForBeingBelowLimitSpouseCivilPartnerOrCharityExemption  = "Spouse, Civil Partner or Charity Exemption"

  // Exemptions
  val ExemptionsGuidanceSeen = "ExemptionGuidanceSeen"

  //Constants for Single Values
  val SingleValueProbateRef = "probateReference"

  val TenureFreehold = "Freehold"
  val TenureLeasehold = "Leasehold"

  // The purpose of the key noBackLinkAvailableKey is to control
  //  when the 'Back' link is made available on the multiple executors page.
  // It is necessary because the link should not be available when this page
  // has been reached via the 'Add or delete executors' link on the
  // registration summary page. So, after editing or deleting executors from the
  // displayed table the 'Back' link should still not be visible if the page was
  // accessed via this route.
  val noBackLinkAvailableKey = "NoBackLinkAvailable"

  // Values to differentiate timeout and system error
  val errorRequestTimeOut = "requestTimeOut"
  val errorSystem = "systemError"
  val errorServiceUnavailable = "serviceUnavailable"
  val errorDESServiceUnavailable = "desServiceUnavailable"

  val desErrorCode502 = "des_error_code_502"
  val desErrorCode503 = "des_error_code_503"
  val desErrorCode504 = "des_error_code_504"
  val notFoundExceptionCode = 404
  val internalExceptionCode = 500

  object Mode extends Enumeration {
    val Edit, Standard = Value
  }
}
