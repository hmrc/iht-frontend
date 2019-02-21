/*
 * Copyright 2019 HM Revenue & Customs
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

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.ControllerHelper.Mode
import iht.controllers.registration.applicant.{routes => applicantRoutes}
import iht.forms.registration.DeceasedForms._
import iht.models.{DeceasedDetails, RegistrationDetails}
import iht.utils.{CommonHelper, DeceasedInfoHelper}
import iht.views.html.registration.{deceased => views}
import javax.inject.Inject
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.partials.FormPartialRetriever

class DeceasedAddressDetailsUKControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                       val cachingConnector: CachingConnector,
                                                       val authConnector: AuthConnector,
                                                       val formPartialRetriever: FormPartialRetriever) extends DeceasedAddressDetailsUKController {

}

trait DeceasedAddressDetailsUKController extends RegistrationDeceasedControllerWithEditMode {
  def form = deceasedAddressDetailsUKForm

  override def guardConditions: Set[Predicate] = guardConditionsDeceasedLastContactAddress

  override val storageFailureMessage = "Storage of registration details fails during deceased address details UK"

  lazy val submitRoute = routes.DeceasedAddressDetailsUKController.onSubmit
  lazy val editSubmitRoute = routes.DeceasedAddressDetailsUKController.onEditSubmit
  lazy val switchToUkRoute = routes.DeceasedAddressDetailsOutsideUKController.onPageLoad
  lazy val switchToUkEditRoute = routes.DeceasedAddressDetailsOutsideUKController.onEditPageLoad

  def okForPageLoad(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.deceased_address_details_uk(form,
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(name),
      submitRoute,
      switchToUkRoute)
    (request, language, applicationMessages, formPartialRetriever))

  def okForEditPageLoad(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.deceased_address_details_uk(form,
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(name),
      editSubmitRoute,
      switchToUkEditRoute,
      cancelToRegSummary)
    (request, language, applicationMessages, formPartialRetriever))

  def badRequestForSubmit(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.deceased_address_details_uk(form,
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(name),
      submitRoute,
      switchToUkRoute)
    (request, language, applicationMessages, formPartialRetriever))

  def badRequestForEditSubmit(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.deceased_address_details_uk(form,
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(name),
      editSubmitRoute,
      switchToUkEditRoute,
      cancelToRegSummary)
    (request, language, applicationMessages, formPartialRetriever))

  override def fillForm(rd: RegistrationDetails) = {
    val dd = CommonHelper.getOrException(rd.deceasedDetails)

    if (CommonHelper.getOrException(dd.isAddressInUK)) {
      deceasedAddressDetailsUKForm.fill(dd)
    } else {
      deceasedAddressDetailsUKForm
    }
  }

  def applyChangesToRegistrationDetails(rd: RegistrationDetails, dd: DeceasedDetails, mode: Mode.Value) = {
    val x = rd.deceasedDetails.map( _ copy(isAddressInUK = Some(true), ukAddress = dd.ukAddress ))
    rd copy (deceasedDetails = x)
  }

  def onwardRoute(rd: RegistrationDetails) = applicantRoutes.ApplyingForProbateController.onPageLoad
}
