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

import iht.controllers.ControllerHelper.Mode
import iht.connector.IhtConnectors
import iht.forms.registration.ApplicantForms._
import iht.metrics.Metrics
import iht.models.{ApplicantDetails, RegistrationDetails}
import iht.utils.RegistrationKickOutHelper
import iht.views.html.registration.{applicant => views}
import play.api.data.Form
import play.api.mvc.{AnyContent, Request}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object ApplyingForProbateController extends ApplyingForProbateController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait ApplyingForProbateController extends RegistrationApplicantControllerWithEditMode {
  def form = applyingForProbateForm

  override def guardConditions: Set[Predicate] = guardConditionsApplicantApplyingForProbateQuestion

  override def getKickoutReason = RegistrationKickOutHelper.checkNotApplyingForProbateKickout

  override val storageFailureMessage = "Store registration details fails on applying for probate submission"

  lazy val submitRoute = routes.ApplyingForProbateController.onSubmit
  lazy val editSubmitRoute = routes.ApplyingForProbateController.onEditSubmit

  def okForPageLoad(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.applying_for_probate(form, submitRoute)
    (request, request.acceptLanguages.head, applicationMessages))

  def okForEditPageLoad(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.applying_for_probate(form, editSubmitRoute, cancelToRegSummary)
    (request, request.acceptLanguages.head, applicationMessages))

  def badRequestForSubmit(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.applying_for_probate(form, submitRoute)
    (request, request.acceptLanguages.head, applicationMessages))

  def badRequestForEditSubmit(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.applying_for_probate(form, editSubmitRoute, cancelToRegSummary)
    (request, request.acceptLanguages.head, applicationMessages))

  def applyChangesToRegistrationDetails(rd: RegistrationDetails, ad: ApplicantDetails, mode: Mode.Value) = {
    val x = rd.applicantDetails.getOrElse(new ApplicantDetails) copy (
      isApplyingForProbate = ad.isApplyingForProbate)
    rd copy (applicantDetails = Some(x))
  }

  def onwardRoute(rd: RegistrationDetails) = routes.ProbateLocationController.onPageLoad
}
