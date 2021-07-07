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
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.ControllerHelper.Mode
import iht.forms.registration.ApplicantForms._
import iht.models.{ApplicantDetails, RegistrationDetails}
import iht.utils.DeceasedInfoHelper
import iht.views.html.registration.applicant.applying_for_probate
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class ApplyingForProbateControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                 val cachingConnector: CachingConnector,
                                                 val authConnector: AuthConnector,
                                                 val applyingForProbateView: applying_for_probate,
                                                 implicit val appConfig: AppConfig,
                                                 val cc: MessagesControllerComponents) extends FrontendController(cc) with ApplyingForProbateController

trait ApplyingForProbateController extends RegistrationApplicantControllerWithEditMode {
  def form(implicit messsages: Messages): Form[ApplicantDetails] = applyingForProbateForm
  override def guardConditions: Set[Predicate] = guardConditionsApplicantApplyingForProbateQuestion

  override def getKickoutReason: RegistrationDetails => Option[String] = checkNotApplyingForProbateKickout

  override val storageFailureMessage = "Store registration details fails on applying for probate submission"

  lazy val submitRoute: Call = routes.ApplyingForProbateController.onSubmit()
  lazy val editSubmitRoute: Call = routes.ApplyingForProbateController.onEditSubmit()
  val applyingForProbateView: applying_for_probate
  def okForPageLoad(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]): Result =
    Ok(applyingForProbateView(form, DeceasedInfoHelper.getDeceasedNameOrDefaultString(name), submitRoute))

  def okForEditPageLoad(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]): Result =
    Ok(applyingForProbateView(form, DeceasedInfoHelper.getDeceasedNameOrDefaultString(name), editSubmitRoute, cancelToRegSummary))

  def badRequestForSubmit(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]): Result =
    BadRequest(applyingForProbateView(form, DeceasedInfoHelper.getDeceasedNameOrDefaultString(name), submitRoute))

  def badRequestForEditSubmit(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]): Result =
    BadRequest(applyingForProbateView(form, DeceasedInfoHelper.getDeceasedNameOrDefaultString(name), editSubmitRoute, cancelToRegSummary))

  def applyChangesToRegistrationDetails(rd: RegistrationDetails, ad: ApplicantDetails, mode: Mode.Value): RegistrationDetails = {
    val x = rd.applicantDetails.getOrElse(new ApplicantDetails(role = Some(appConfig.roleLeadExecutor))) copy (
      isApplyingForProbate = ad.isApplyingForProbate)
    rd copy (applicantDetails = Some(x))
  }

  def onwardRoute(rd: RegistrationDetails): Call = routes.ExecutorOfEstateController.onPageLoad()
}
