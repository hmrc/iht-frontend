/*
 * Copyright 2016 HM Revenue & Customs
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
import iht.controllers.registration.applicant.{routes => applicantRoutes}
import iht.forms.registration.DeceasedForms._
import iht.metrics.Metrics
import iht.models.{DeceasedDetails, RegistrationDetails}
import iht.utils.CommonHelper
import iht.views.html.registration.{deceased => views}
import play.api.data.Form
import play.api.mvc.{AnyContent, Request}

object DeceasedAddressDetailsUKController extends DeceasedAddressDetailsUKController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait DeceasedAddressDetailsUKController extends RegistrationDeceasedControllerWithEditMode {
  def form = deceasedAddressDetailsUKForm

  override def guardConditions: Set[Predicate] = guardConditionsDeceasedLastContactAddress

  override val storageFailureMessage = "Storage of registration details fails during deceased address details UK"

  lazy val submitRoute = routes.DeceasedAddressDetailsUKController.onSubmit
  lazy val editSubmitRoute = routes.DeceasedAddressDetailsUKController.onEditSubmit
  lazy val switchToUkRoute = routes.DeceasedAddressDetailsOutsideUKController.onPageLoad
  lazy val switchToUkEditRoute = routes.DeceasedAddressDetailsOutsideUKController.onEditPageLoad

  def okForPageLoad(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    Ok(views.deceased_address_details_uk(form, submitRoute, switchToUkRoute)
    (request, request.acceptLanguages.head))

  def okForEditPageLoad(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    Ok(views.deceased_address_details_uk(form, editSubmitRoute, switchToUkEditRoute, cancelToRegSummary)
    (request, request.acceptLanguages.head))

  def badRequestForSubmit(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    BadRequest(views.deceased_address_details_uk(form, submitRoute, switchToUkRoute)
    (request, request.acceptLanguages.head))

  def badRequestForEditSubmit(form: Form[DeceasedDetails])(implicit request: Request[AnyContent]) =
    BadRequest(views.deceased_address_details_uk(form, editSubmitRoute, switchToUkEditRoute, cancelToRegSummary)
    (request, request.acceptLanguages.head))

  override def fillForm(rd: RegistrationDetails) = {
    val dd = CommonHelper.getOrException(rd.deceasedDetails)

    if (CommonHelper.getOrException(dd.isAddressInUK)) deceasedAddressDetailsUKForm.fill(dd)
    else deceasedAddressDetailsUKForm
  }

  def applyChangesToRegistrationDetails(rd: RegistrationDetails, dd: DeceasedDetails, mode: Mode.Value) = {
    val x = rd.deceasedDetails.map( _ copy(isAddressInUK = Some(true), ukAddress = dd.ukAddress ))
    rd copy (deceasedDetails = x)
  }

  def onwardRoute(rd: RegistrationDetails) = applicantRoutes.ApplyingForProbateController.onPageLoad
}
