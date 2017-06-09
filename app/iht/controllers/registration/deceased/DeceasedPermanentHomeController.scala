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

import iht.constants.FieldMappings._
import iht.controllers.ControllerHelper.Mode
import iht.connector.IhtConnectors
import iht.forms.registration.DeceasedForms._
import iht.metrics.Metrics
import iht.models.{DeceasedDetails, RegistrationDetails}
import iht.utils.RegistrationKickOutHelper
import iht.views.html.registration.{deceased => views}
import play.api.data.Form
import play.api.mvc._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object DeceasedPermanentHomeController extends DeceasedPermanentHomeController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait DeceasedPermanentHomeController extends RegistrationDeceasedControllerWithEditMode {
  def form = deceasedPermanentHomeForm

  override def guardConditions: Set[Predicate] = guardConditionsDeceasedPermanentHome

  override def getKickoutReason = RegistrationKickOutHelper.kickoutReasonDeceasedDetails

  override val storageFailureMessage = "Storage of registration details fails during deceased permanent home submission"

  lazy val submitRoute = routes.DeceasedPermanentHomeController.onSubmit
  lazy val editSubmitRoute = routes.DeceasedPermanentHomeController.onEditSubmit

  def okForPageLoad(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.deceased_permanent_home(form, submitRoute)
    (request, request.acceptLanguages.head, applicationMessages))

  def okForEditPageLoad(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    Ok(views.deceased_permanent_home(form, editSubmitRoute, cancelToRegSummary)
    (request, request.acceptLanguages.head, applicationMessages))

  def badRequestForSubmit(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    BadRequest(views.deceased_permanent_home(form, submitRoute)
    (request, request.acceptLanguages.head, applicationMessages))

  def badRequestForEditSubmit(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    BadRequest(views.deceased_permanent_home(form, editSubmitRoute, cancelToRegSummary)
    (request, request.acceptLanguages.head, applicationMessages))

  def onwardRoute(rd: RegistrationDetails) = routes.AboutDeceasedController.onPageLoad

  def applyChangesToRegistrationDetails(rd: RegistrationDetails, dd: DeceasedDetails, mode: Mode.Value) = {
    val x = rd.deceasedDetails.getOrElse(new DeceasedDetails) copy (domicile = dd.domicile)
    rd copy (deceasedDetails = Some(x))
  }
}
