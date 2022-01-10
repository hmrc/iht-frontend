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

package iht.controllers.registration.deceased

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.ControllerHelper.Mode
import iht.controllers.registration.{RegistrationController, routes => registrationRoutes}
import iht.forms.registration.DeceasedForms
import iht.forms.registration.DeceasedForms.aboutDeceasedForm
import iht.models.{DeceasedDetails, RegistrationDetails}
import iht.utils.{DeceasedInfoHelper, SessionHelper, StringHelper}
import iht.views.html.registration.deceased.about_deceased
import javax.inject.Inject
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.mvc.{MessagesControllerComponents, _}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class AboutDeceasedControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                            val cachingConnector: CachingConnector,
                                            val authConnector: AuthConnector,
                                            val aboutDeceasedView: about_deceased,
                                            implicit val appConfig: AppConfig,
                                            val cc: MessagesControllerComponents) extends FrontendController(cc) with AboutDeceasedController {

  override def deceasedForms = DeceasedForms
}

trait AboutDeceasedController extends RegistrationController with StringHelper {
  lazy val submitRoute: Call = routes.AboutDeceasedController.onSubmit()
  lazy val editSubmitRoute: Call = routes.AboutDeceasedController.onEditSubmit()

  def cachingConnector: CachingConnector

  def deceasedForms: DeceasedForms

  override def guardConditions = guardConditionsAboutDeceased

  def onPageLoad = pageLoad(routes.AboutDeceasedController.onSubmit())
  def pageLoad(actionCall: Call, mode: Mode.Value = Mode.Standard,
               cancelCall: Option[Call] = None) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      withRegistrationDetailsRedirectOnGuardCondition { rd =>
        val assertedNino = getNino(userNino)

        val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd)
        val dateOfDeath = rd.deceasedDateOfDeath.map(_.dateOfDeath).getOrElse(LocalDate.now)
        val deceasedDetailsForm = rd.deceasedDetails.fold(
          aboutDeceasedForm(loginNino = assertedNino)
        )(dd =>
          aboutDeceasedForm(dateOfDeath, loginNino = assertedNino).fill(dd)
        )
        val okResult: Result = if (mode == Mode.Standard) {
          okForPageLoad(deceasedDetailsForm, Some(deceasedName))
        } else {
          okForEditPageLoad(deceasedDetailsForm, Some(deceasedName))
        }
        val result = okResult.withSession(SessionHelper.ensureSessionHasNino(request.session, userNino))
        Future.successful(result)
      }
  }
  val aboutDeceasedView: about_deceased
  def okForPageLoad(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(aboutDeceasedView(form, submitRoute))

  def okForEditPageLoad(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(aboutDeceasedView(form, editSubmitRoute, cancelToRegSummary))

  def onEditPageLoad = pageLoad(routes.AboutDeceasedController.onEditSubmit(), Mode.Edit, cancelToRegSummary)

  def onSubmit: Action[AnyContent] = submit(routes.AboutDeceasedController.onSubmit())

  def onEditSubmit: Action[AnyContent] = {
    submit(routes.AboutDeceasedController.onEditSubmit(),
      Mode.Edit, cancelToRegSummary)
  }

  def submit(onFailureActionCall: Call, mode: Mode.Value = Mode.Standard,
             cancelCall: Option[Call] = None) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      withRegistrationDetailsRedirectOnGuardCondition { rd: RegistrationDetails =>
        val assertedNino = getNino(userNino)
        val formType = deceasedForms.aboutDeceasedForm(oRegDetails = Some(rd), loginNino = assertedNino)

        val boundForm = formType.bindFromRequest()

        boundForm.fold(formWithErrors => {
          if (mode == Mode.Standard) {
            Future.successful(badRequestForSubmit(formWithErrors))
          } else {
            Future.successful(badRequestForEditSubmit(formWithErrors))
          }
        },
          dd => {
            val optDDCopy = rd.deceasedDetails.map(_ copy(
              firstName = dd.firstName,
              lastName = dd.lastName,
              dateOfBirth = dd.dateOfBirth,
              nino = dd.ninoFormatted,
              maritalStatus = dd.maritalStatus))

            val copyOfRD: RegistrationDetails = rd copy (deceasedDetails = optDDCopy)
            val route: Call = if (mode == Mode.Standard) onwardRoute(copyOfRD) else onwardRouteInEditMode(copyOfRD)

            storeRegistrationDetails(copyOfRD, route, "Storage of registration details fails during about deceased submission")
          })
      }
  }

  def onwardRoute(rd: RegistrationDetails) = routes.DeceasedAddressQuestionController.onPageLoad()

  def onwardRouteInEditMode(rd: RegistrationDetails): Call = registrationRoutes.RegistrationSummaryController.onPageLoad()

  def badRequestForSubmit(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    BadRequest(aboutDeceasedView(form, submitRoute))

  def badRequestForEditSubmit(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    BadRequest(aboutDeceasedView(form, editSubmitRoute, cancelToRegSummary))
}
