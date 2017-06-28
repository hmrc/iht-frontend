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

package iht.controllers.registration.applicant

import javax.inject.{Inject, Singleton}

import iht.controllers.ControllerHelper.Mode
import iht.forms.registration.ApplicantForms
import iht.models.{ApplicantDetails, RegistrationDetails}
import iht.utils.{CommonHelper, RegistrationKickOutHelper}
import iht.views.html.registration.{applicant => views}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, Request}

@Singleton
class ProbateLocationController @Inject()(
                                           val messagesApi: MessagesApi,
                                           val applicantForms: ApplicantForms
                                         ) extends RegistrationApplicantControllerWithEditMode {
  def form = applicantForms.probateLocationForm

  override def guardConditions = guardConditionsApplicantProbateLocation

  override def getKickoutReason = RegistrationKickOutHelper.kickoutReasonApplicantDetails

  override val storageFailureMessage = "Store registration details fails on probate location submission"

  lazy val submitRoute = routes.ProbateLocationController.onSubmit
  lazy val editSubmitRoute = routes.ProbateLocationController.onEditSubmit

  def okForPageLoad(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.probate_location(form, submitRoute)
    (request, request.acceptLanguages.head, request2Messages))

  def okForEditPageLoad(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.probate_location(form, editSubmitRoute, cancelToRegSummary)
    (request, request.acceptLanguages.head, request2Messages))

  def badRequestForSubmit(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.probate_location(form, submitRoute)
    (request, request.acceptLanguages.head, request2Messages))

  def badRequestForEditSubmit(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.probate_location(form, editSubmitRoute, cancelToRegSummary)
    (request, request.acceptLanguages.head, request2Messages))

  def applyChangesToRegistrationDetails(rd: RegistrationDetails, ad: ApplicantDetails, mode: Mode.Value) = {
    val x = CommonHelper.getOrException(rd.applicantDetails) copy (country = ad.country)
    rd copy (applicantDetails = Some(x))
  }

  def onwardRoute(rd: RegistrationDetails) = routes.ApplicantTellUsAboutYourselfController.onPageLoad
}
