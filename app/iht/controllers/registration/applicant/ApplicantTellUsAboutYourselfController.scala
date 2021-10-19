/*
 * Copyright 2021 HM Revenue & Customs
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

import iht.config.AppConfig
import iht.connector.{CachingConnector, CitizenDetailsConnector}
import iht.controllers.ControllerHelper.Mode
import iht.controllers.registration.{routes => registrationRoutes}
import iht.forms.registration.ApplicantForms._
import iht.metrics.IhtMetrics
import iht.models.{ApplicantDetails, CidPerson, RegistrationDetails}
import iht.utils.{SessionHelper, StringHelper}
import iht.views.html.registration.applicant.applicant_tell_us_about_yourself
import iht.views.html.registration.registration_error_citizenDetails
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.UpstreamErrorResponse.WithStatusCode
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class ApplicantTellUsAboutYourselfControllerImpl @Inject()(val citizenDetailsConnector: CitizenDetailsConnector,
                                                           val metrics: IhtMetrics,
                                                           val cachingConnector: CachingConnector,
                                                           val authConnector: AuthConnector,
                                                           val registrationErrorCitizenDetailsView: registration_error_citizenDetails,
                                                           val applicantTellUsAboutYourselfView: applicant_tell_us_about_yourself,
                                                           implicit val appConfig: AppConfig,
                                                           val cc: MessagesControllerComponents)
  extends FrontendController(cc) with ApplicantTellUsAboutYourselfController

trait ApplicantTellUsAboutYourselfController extends RegistrationApplicantControllerWithEditMode with StringHelper {
  def form(implicit messsages: Messages) = applicantTellUsAboutYourselfForm

  override def guardConditions: Set[Predicate] = guardConditionsApplicantContactDetails

  def metrics: IhtMetrics

  def citizenDetailsConnector: CitizenDetailsConnector

  lazy val submitRoute = routes.ApplicantTellUsAboutYourselfController.onSubmit
  lazy val editSubmitRoute = routes.ApplicantTellUsAboutYourselfController.onEditSubmit
  val registrationErrorCitizenDetailsView: registration_error_citizenDetails
  val applicantTellUsAboutYourselfView: applicant_tell_us_about_yourself
  def okForPageLoad(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(applicantTellUsAboutYourselfView(form, Mode.Standard, submitRoute))

  override def pageLoad(mode: Mode.Value) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      withRegistrationDetailsRedirectOnGuardCondition { rd =>
        val f = fillForm(rd)
        val okResult: Result = if (mode == Mode.Standard) {
          okForPageLoad(f)
        } else {
          okForEditPageLoad(f)
        }
        val result = okResult.withSession(SessionHelper.ensureSessionHasNino(request.session, userNino))
        Future.successful(result)
      }
  }

  def okForEditPageLoad(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(applicantTellUsAboutYourselfView(form, Mode.Edit, editSubmitRoute, cancelToRegSummary))

  def badRequestForSubmit(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(applicantTellUsAboutYourselfView(form, Mode.Standard, submitRoute))

  def badRequestForEditSubmit(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(applicantTellUsAboutYourselfView(form, Mode.Edit, editSubmitRoute, cancelToRegSummary))

  // Implementation not required as we are overriding the submit method
  def applyChangesToRegistrationDetails(rd: RegistrationDetails, ad: ApplicantDetails, mode: Mode.Value = Mode.Standard) = ???

  def onwardRoute(rd: RegistrationDetails) = routes.ProbateLocationController.onPageLoad

  def routeForSubmit(ad: ApplicantDetails, mode: Mode.Value): Call = ad.doesLiveInUK match {
    case Some(true) if mode == Mode.Standard => routes.ApplicantAddressController.onPageLoadUk()
    case Some(false) if mode == Mode.Standard => routes.ApplicantAddressController.onPageLoadAbroad()
    case _ => registrationRoutes.RegistrationSummaryController.onPageLoad()
  }

  override def submit(mode: Mode.Value) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
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
            val nino = Nino(getNino(userNino))
            val citizenDetailsPersonFuture: Future[CidPerson] = citizenDetailsConnector.getCitizenDetails(nino)
            val applicantDetailsFuture = citizenDetailsPersonFuture.map {
              person: CidPerson => {
                if (mode == Mode.Standard) {
                  rd.applicantDetails.getOrElse(new ApplicantDetails(role = Some(appConfig.roleLeadExecutor))) copy(firstName = person.firstName,
                    lastName = person.lastName, dateOfBirth = person.dateOfBirthLocalDate,
                    nino = Some(ninoFormat(nino.nino))) copy(phoneNo = ad.phoneNo, doesLiveInUK = ad.doesLiveInUK)
                } else {
                  rd.applicantDetails.getOrElse(new ApplicantDetails(role = Some(appConfig.roleLeadExecutor))) copy (phoneNo = ad.phoneNo)
                }
              }
            }
            applicantDetailsFuture.flatMap {
              result =>
                val copyOfRD = rd copy (applicantDetails = Some(result))
                val route = routeForSubmit(ad, mode)
                storeRegistrationDetails(copyOfRD,
                  route,
                  "Fails to store registration details during application tell us about yourself submission")
            } recover citizenDetailsFailure()
          }
        )
      }
    }
  }

  def citizenDetailsFailure()(implicit request: Request[_]): PartialFunction[Throwable, Result] = {
    case WithStatusCode(NOT_FOUND) => {
      logger.warn("[ApplicantTellUsAboutYourselfController][citizenDetailsFailure]: Citizen Details returned a 404, showing citizenDetailsNotFound guidance")
      BadRequest(registrationErrorCitizenDetailsView(
        "page.iht.registration.applicantDetails.citizenDetailsNotFound.title",
        "page.iht.registration.applicantDetails.citizenDetailsNotFound.guidance"))
    }
    case e@WithStatusCode(LOCKED) => {
      logger.warn("[ApplicantTellUsAboutYourselfController][citizenDetailsFailure]: Citizen Details returned an MCI error")
      throw e
    }
  }

}
