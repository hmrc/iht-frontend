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

package iht.controllers.registration.deceased

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.ControllerHelper.Mode
import iht.controllers.registration.applicant.{routes => applicantRoutes}
import iht.forms.registration.DeceasedForms._
import iht.models.{DeceasedDetails, RegistrationDetails}
import iht.utils.{CommonHelper, DeceasedInfoHelper}
import iht.views.html.registration.deceased.deceased_address_details_uk
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Call, MessagesControllerComponents, Request}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class DeceasedAddressDetailsUKControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                       val cachingConnector: CachingConnector,
                                                       val authConnector: AuthConnector,
                                                       val deceasedAddressDetailsUkView: deceased_address_details_uk,
                                                       implicit val appConfig: AppConfig,
                                                       val cc: MessagesControllerComponents) extends FrontendController(cc) with DeceasedAddressDetailsUKController {

}

trait DeceasedAddressDetailsUKController extends RegistrationDeceasedControllerWithEditMode {
  def form(implicit messsages: Messages) = deceasedAddressDetailsUKForm

  override def guardConditions: Set[Predicate] = guardConditionsDeceasedLastContactAddress

  override val storageFailureMessage = "Storage of registration details fails during deceased address details UK"

  lazy val submitRoute: Call = routes.DeceasedAddressDetailsUKController.onSubmit
  lazy val editSubmitRoute: Call = routes.DeceasedAddressDetailsUKController.onEditSubmit
  lazy val switchToUkRoute: Call = routes.DeceasedAddressDetailsOutsideUKController.onPageLoad
  lazy val switchToUkEditRoute: Call = routes.DeceasedAddressDetailsOutsideUKController.onEditPageLoad
  val deceasedAddressDetailsUkView: deceased_address_details_uk
  def okForPageLoad(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(deceasedAddressDetailsUkView(form,
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(name),
      submitRoute,
      switchToUkRoute))

  def okForEditPageLoad(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(deceasedAddressDetailsUkView(form,
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(name),
      editSubmitRoute,
      switchToUkEditRoute,
      cancelToRegSummary))

  def badRequestForSubmit(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(deceasedAddressDetailsUkView(form,
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(name),
      submitRoute,
      switchToUkRoute))

  def badRequestForEditSubmit(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(deceasedAddressDetailsUkView(form,
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(name),
      editSubmitRoute,
      switchToUkEditRoute,
      cancelToRegSummary))

  override def fillForm(rd: RegistrationDetails)(implicit request: Request[_]) = {
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
