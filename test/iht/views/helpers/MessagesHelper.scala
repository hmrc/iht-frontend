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

package iht.views.helpers

trait MessagesHelper {

  val pageIhtIVFailureTechnicalIssueHeading = "Sorry, there is a problem with the service"
  val errorApplicationSystemErrorp1 = "Try again later to sign in to the service at https://www.tax.service.gov.uk/inheritance-tax/estate-report (save this link)."
  val ihtApplicationTimeoutp1 = "We saved your progress on the estate report."
  val pageIhtIVFailureYouCanAlso = "You can also:"
  val pageIhtIVFailureReportWithPaperForm = "report the estate value using the IHT205 paper form instead"
  val pageIhtIVFailureaskForHelp = "ask for help by email using 'Get help with this page' below"

  val errorRegistrationSystemErrorp1 = "Try again later to sign in to the service at https://www.tax.service.gov.uk/inheritance-tax/registration/registration-checklist (save this link)."

  val errorEstateOverviewJsonErrorp1 = "You cannot complete your estate report online. This is because of an error in our system."
  val errorEstateOverviewJsonErrorp2 = "Use the IHT205 paper form to report the estate value."

  val errorRegistrationServiceUnavailablep1 = "Wait a few seconds and try again."
  val errorEstateReportServiceUnavailablep3 = "If you see this message several times you can sign out now and try again later to submit your report at https://www.tax.service.gov.uk/inheritance-tax/estate-report/declaration (save this link)."


  val errorRegistrationServiceUnavailablep2 = "If you see this message several times, you will need to sign out and register again later at https://www.tax.service.gov.uk/inheritance-tax/registration/registration-checklist (save this link)."
  val errorRegistrationServiceUnavailablep3 = "You can choose to report the estate value using the IHT205 paper form instead."
  val ihtIVTryAgain = "Try again"
  val ihtIVTryAgainLink = "/inheritance-tax/registration/check-your-answers"

  val pageIHTSignOut = "Sign out"
  val pageIHTSignOutLink = "/inheritance-tax/feedback-survey"


  //IV Wayfinder
  val WayfinderVerificationTitle = "We have confirmed who you are"
  val WayfinderVerificationP1 = "Check that you have the details you need to register the estate (opens in a new window). Only start once you have everything you need because there is no way to save an incomplete registration."
  val WayfinderVerificationP1Url = "/inheritance-tax/registration/registration-checklist"
  val WayfinderVerificationP1Link = "have the details you need to register the estate (opens in a new window)."
  val WayfinderVerificationP2 = "If you stay on one page for more than 30 minutes you will be signed out for your security."

  val WayfinderLoginTitle = "We have signed you in"
  val WayfinderLoginP1 = "Check that you have the details you need to register the estate (opens in a new window). Only start once you have everything you need because there is no way to save an incomplete registration."
  val WayfinderLoginP1Url = "/inheritance-tax/registration/registration-checklist"
  val WayfinderLoginP1Link = "have the details you need to register the estate (opens in a new window)."
  val WayfinderLoginP2 = "If you stay on one page for more than 30 minutes you will be signed out for your security."

  val WayfinderStartUrl = "/inheritance-tax/registration/date-of-death"
  val WayfinderStartLink = "Start registration"

  val WayfinderLeaveUrl = "/inheritance-tax/what-do-you-want-to-do"
  val WayfinderLeaveLink = "Leave this page to get all the details you need"
}
