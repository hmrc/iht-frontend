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
import iht.forms.registration.DeceasedForms._
import iht.controllers.registration.applicant.{routes => applicantRoutes}
import iht.metrics.Metrics
import iht.models.{DeceasedDetails, RegistrationDetails}
import iht.utils.CommonHelper
import iht.views.html.registration.{deceased => views}
import play.api.data.Form
import play.api.mvc.{AnyContent, Request}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object DeceasedAddressDetailsOutsideUKController extends DeceasedAddressDetailsOutsideUKController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait DeceasedAddressDetailsOutsideUKController extends RegistrationDeceasedControllerWithEditMode {
  def form = deceasedAddressDetailsOutsideUKForm

  override def guardConditions: Set[Predicate] = guardConditionsDeceasedLastContactAddress

  override val storageFailureMessage = "Storage of registration details fails during deceased address details outside of UK"

  lazy val submitRoute = routes.DeceasedAddressDetailsOutsideUKController.onSubmit
  lazy val editSubmitRoute = routes.DeceasedAddressDetailsOutsideUKController.onEditSubmit
  lazy val switchToUkRoute = routes.DeceasedAddressDetailsUKController.onPageLoad
  lazy val switchToUkEditRoute = routes.DeceasedAddressDetailsUKController.onEditPageLoad

  def okForPageLoad(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    Ok(views.deceased_address_details_outside_uk(form, submitRoute, switchToUkRoute)
    (request, request.acceptLanguages.head, applicationMessages))

  def okForEditPageLoad(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    Ok(views.deceased_address_details_outside_uk(form, editSubmitRoute, switchToUkEditRoute, cancelToRegSummary)
    (request, request.acceptLanguages.head, applicationMessages))

  def badRequestForSubmit(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    BadRequest(views.deceased_address_details_outside_uk(form, submitRoute, switchToUkRoute)
    (request, request.acceptLanguages.head, applicationMessages))

  def badRequestForEditSubmit(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    BadRequest(views.deceased_address_details_outside_uk(form, editSubmitRoute, switchToUkEditRoute, cancelToRegSummary)
    (request, request.acceptLanguages.head, applicationMessages))

  override def fillForm(rd: RegistrationDetails) = {
    val dd = CommonHelper.getOrException(rd.deceasedDetails)

    if (CommonHelper.getOrException(dd.isAddressInUK)) {
      deceasedAddressDetailsOutsideUKForm
    } else {
      deceasedAddressDetailsOutsideUKForm.fill(dd)
    }
  }

  def applyChangesToRegistrationDetails(rd: RegistrationDetails, dd: DeceasedDetails, mode: Mode.Value) = {
    val x = rd.deceasedDetails.map( _ copy(isAddressInUK = Some(false), ukAddress = dd.ukAddress ))
    rd copy (deceasedDetails = x)
  }

  def onwardRoute(rd: RegistrationDetails) = applicantRoutes.ApplyingForProbateController.onPageLoad
}
