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

import iht.controllers.ControllerHelper.Mode
import iht.controllers.IhtConnectors
import iht.controllers.registration.{routes => registrationRoutes}
import iht.forms.registration.DeceasedForms.aboutDeceasedForm
import iht.metrics.Metrics
import iht.models.{DeceasedDateOfDeath, DeceasedDetails, RegistrationDetails}
import iht.views.html.registration.{deceased => views}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.mvc.{AnyContent, Request}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object AboutDeceasedController extends AboutDeceasedController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait AboutDeceasedController extends RegistrationDeceasedControllerWithEditMode {
  def form = aboutDeceasedForm()

  override def guardConditions = guardConditionsAboutDeceased

  def metrics: Metrics

  lazy val submitRoute = routes.AboutDeceasedController.onSubmit
  lazy val editSubmitRoute = routes.AboutDeceasedController.onEditSubmit

  def okForPageLoad(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    Ok(views.about_deceased(form, submitRoute)(request))

  def okForEditPageLoad(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    Ok(views.about_deceased(form, editSubmitRoute, cancelToRegSummary)(request))

  def badRequestForSubmit(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    BadRequest(views.about_deceased(form, submitRoute)(request))

  def badRequestForEditSubmit(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    BadRequest(views.about_deceased(form, editSubmitRoute, cancelToRegSummary)(request))

  def onwardRoute(rd: RegistrationDetails) = routes.DeceasedAddressQuestionController.onPageLoad

  // Not implemented because we are overriding the submit method
  def applyChangesToRegistrationDetails(rd: RegistrationDetails, dd: DeceasedDetails, mode: Mode.Value) = ???

  override def fillForm(rd: RegistrationDetails) = {
    val dateOfDeath = rd.deceasedDateOfDeath.map(_.dateOfDeath).getOrElse(LocalDate.now)
    rd.deceasedDetails.fold(aboutDeceasedForm())(dd => aboutDeceasedForm(dateOfDeath).fill(dd))
  }

  override def submit(mode: Mode.Value = Mode.Standard) = authorisedForIht {
    implicit user => implicit request => {
      withRegistrationDetails { rd =>
        val dateOfDeath = rd.deceasedDateOfDeath.getOrElse(DeceasedDateOfDeath(LocalDate.now())).dateOfDeath
        val boundForm = aboutDeceasedForm(dateOfDeath).bindFromRequest
        boundForm.fold(
          formWithErrors => {
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
            val copyOfRD = rd copy (deceasedDetails = optDDCopy)

            val route = if (mode == Mode.Standard) onwardRoute(copyOfRD) else onwardRouteInEditMode(copyOfRD)

            storeRegistrationDetails(copyOfRD, route, "Storage of registration details fails during about deceased submission")
          }
        )
      }
    }
  }
}
