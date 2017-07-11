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

package iht.controllers.registration.deceased

import iht.connector.{CachingConnector, IhtConnectors}
import iht.controllers.ControllerHelper.Mode
import iht.controllers.registration.{RegistrationController, routes => registrationRoutes}
import iht.forms.registration.DeceasedForms
import iht.forms.registration.DeceasedForms.aboutDeceasedForm
import iht.metrics.Metrics
import iht.models.{DeceasedDetails, RegistrationDetails}
import iht.utils.{DeceasedInfoHelper, SessionHelper}
import iht.views.html.registration.{deceased => views}
import org.joda.time.LocalDate
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc._

import scala.concurrent.Future

object AboutDeceasedController extends AboutDeceasedController with IhtConnectors {
  def metrics: Metrics = Metrics
  override def deceasedForms = DeceasedForms
}

trait AboutDeceasedController extends RegistrationController {
  def cachingConnector: CachingConnector
  def deceasedForms: DeceasedForms

  override def guardConditions = guardConditionsAboutDeceased

  def onPageLoad = pageLoad(routes.AboutDeceasedController.onSubmit())

  def onEditPageLoad = pageLoad(routes.AboutDeceasedController.onEditSubmit(), Mode.Edit, cancelToRegSummary)

  def pageLoad(actionCall: Call, mode: Mode.Value = Mode.Standard,
               cancelCall: Option[Call] = None) = authorisedForIht {
    implicit user =>
      implicit request =>
        withRegistrationDetailsRedirectOnGuardCondition { rd =>
          val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd)
          val dateOfDeath = rd.deceasedDateOfDeath.map(_.dateOfDeath).getOrElse(LocalDate.now)
          val f = rd.deceasedDetails.fold(aboutDeceasedForm())(dd => aboutDeceasedForm(dateOfDeath).fill(dd))
          val okResult: Result = if (mode == Mode.Standard) {
            okForPageLoad(f, Some(deceasedName))
          } else {
            okForEditPageLoad(f, Some(deceasedName))
          }
          val result = okResult.withSession(SessionHelper.ensureSessionHasNino(request.session, user))
          Future.successful(result)
        }
  }

  lazy val submitRoute: Call = routes.AboutDeceasedController.onSubmit()
  lazy val editSubmitRoute: Call = routes.AboutDeceasedController.onEditSubmit()
  def onwardRoute(rd: RegistrationDetails) = routes.DeceasedAddressQuestionController.onPageLoad()
  def onwardRouteInEditMode(rd: RegistrationDetails): Call = registrationRoutes.RegistrationSummaryController.onPageLoad()

  def okForPageLoad(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.about_deceased(form, submitRoute)(request, applicationMessages))

  def okForEditPageLoad(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.about_deceased(form, editSubmitRoute, cancelToRegSummary)(request, applicationMessages))

  def onSubmit: Action[AnyContent] = submit(routes.AboutDeceasedController.onSubmit())
  def onEditSubmit: Action[AnyContent] = {
    submit(routes.AboutDeceasedController.onEditSubmit(),
      Mode.Edit, cancelToRegSummary)
  }

  def submit(onFailureActionCall: Call, mode: Mode.Value = Mode.Standard,
             cancelCall: Option[Call] = None) = authorisedForIht {
    implicit user =>
      implicit request =>
        withRegistrationDetailsRedirectOnGuardCondition { (rd: RegistrationDetails) =>

          val formType = deceasedForms.aboutDeceasedForm()

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
                nino = dd.nino,
                maritalStatus = dd.maritalStatus))

              val copyOfRD: RegistrationDetails = rd copy (deceasedDetails = optDDCopy)
              val route: Call = if (mode == Mode.Standard) onwardRoute(copyOfRD) else onwardRouteInEditMode(copyOfRD)

              storeRegistrationDetails(copyOfRD, route, "Storage of registration details fails during about deceased submission")
            })
        }
  }

  def badRequestForSubmit(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    BadRequest(views.about_deceased(form, submitRoute)(request, applicationMessages))

  def badRequestForEditSubmit(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    BadRequest(views.about_deceased(form, editSubmitRoute, cancelToRegSummary)(request, applicationMessages))
}
