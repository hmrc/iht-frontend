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

import iht.connector.CitizenDetailsConnector
import iht.controllers.ControllerHelper.Mode
import iht.connector.IhtConnectors
import iht.controllers.registration.{routes => registrationRoutes}
import iht.forms.registration.ApplicantForms._
import iht.metrics.Metrics
import iht.models.{ApplicantDetails, CidPerson, RegistrationDetails}
import iht.utils.CommonHelper
import iht.utils.CommonHelper._
import iht.views.html.registration.{applicant => views}
import play.api.data.Form
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.domain.Nino
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future


object ApplicantTellUsAboutYourselfController extends ApplicantTellUsAboutYourselfController with IhtConnectors {
  def metrics: Metrics = Metrics

  override lazy val citizenDetailsConnector = CitizenDetailsConnector
}

trait ApplicantTellUsAboutYourselfController extends RegistrationApplicantControllerWithEditMode {
  def form = applicantTellUsAboutYourselfForm

  override def guardConditions: Set[Predicate] = guardConditionsApplicantContactDetails

  def metrics: Metrics

  def citizenDetailsConnector: CitizenDetailsConnector

  lazy val submitRoute = routes.ApplicantTellUsAboutYourselfController.onSubmit
  lazy val editSubmitRoute = routes.ApplicantTellUsAboutYourselfController.onEditSubmit

  def okForPageLoad(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.applicant_tell_us_about_yourself(form, Mode.Standard, submitRoute)
    (request, request.acceptLanguages.head, applicationMessages))

  override def pageLoad(mode: Mode.Value) = authorisedForIht {
    implicit user => implicit request =>
      withRegistrationDetailsRedirectOnGuardCondition { rd =>
        val f = fillForm(rd)
        val okResult: Result = if (mode == Mode.Standard) {
          okForPageLoad(f)
        } else {
          okForEditPageLoad(f)
        }
        val result = okResult.withSession(CommonHelper.ensureSessionHasNino(request.session, user))
        Future.successful(result)
      }
  }

  def okForEditPageLoad(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.applicant_tell_us_about_yourself(form, Mode.Edit, editSubmitRoute, cancelToRegSummary)
    (request, request.acceptLanguages.head, applicationMessages))

  def badRequestForSubmit(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.applicant_tell_us_about_yourself(form, Mode.Standard, submitRoute)
    (request, request.acceptLanguages.head, applicationMessages))

  def badRequestForEditSubmit(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.applicant_tell_us_about_yourself(form, Mode.Edit, editSubmitRoute, cancelToRegSummary)
  (request, request.acceptLanguages.head, applicationMessages))

  // Implementation not required as we are overriding the submit method
  def applyChangesToRegistrationDetails(rd: RegistrationDetails, ad: ApplicantDetails, mode: Mode.Value = Mode.Standard) = ???

  def onwardRoute(rd: RegistrationDetails) = routes.ProbateLocationController.onPageLoad

  override def submit(mode: Mode.Value) = authorisedForIht {
    implicit user => implicit request => {
      withRegistrationDetails { rd =>
        val formType = if (mode == Mode.Standard) {
            applicantTellUsAboutYourselfForm
          } else {
            applicantTellUsAboutYourselfEditForm
          }

        val boundForm = formType.bindFromRequest
        boundForm.fold(
          formWithErrors => {
            if (mode == Mode.Standard) {
              Future.successful(badRequestForSubmit(formWithErrors))
            } else {
              Future.successful(badRequestForEditSubmit(formWithErrors))
            }
          },
          ad => {
            val nino = Nino(getNino(user))

            val applicantDetailsFuture = citizenDetailsConnector.getCitizenDetails(nino).map {
              person: CidPerson => {
                if (mode == Mode.Standard) {
                  rd.applicantDetails.getOrElse(new ApplicantDetails) copy(firstName = person.firstName,
                    lastName = person.lastName, dateOfBirth = person.dateOfBirthLocalDate,
                    nino = Some(nino.nino)) copy(phoneNo = ad.phoneNo, doesLiveInUK = ad.doesLiveInUK)
                } else {
                  rd.applicantDetails.getOrElse(new ApplicantDetails) copy (phoneNo = ad.phoneNo)
                }
              }
            }

            applicantDetailsFuture.flatMap {
              case result => {
                val copyOfRD = rd copy (applicantDetails = Some(result))
                val route = ad.doesLiveInUK match {
                  case Some(true) if mode == Mode.Standard => routes.ApplicantAddressController.onPageLoadUk
                  case Some(false) if mode == Mode.Standard => routes.ApplicantAddressController.onPageLoadAbroad
                  case _ => registrationRoutes.RegistrationSummaryController.onPageLoad
                }
                storeRegistrationDetails(copyOfRD,
                  route,
                  "Fails to store registration details during application tell us about yourself submission")
              }
            }
          }
        )
      }
    }
  }
}
