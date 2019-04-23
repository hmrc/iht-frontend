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

package iht.controllers.registration.applicant

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.ControllerHelper.Mode
import iht.forms.registration.ApplicantForms._
import iht.models.{ApplicantDetails, RegistrationDetails}
import iht.utils.DeceasedInfoHelper
import iht.views.html.registration.{applicant => views}
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class ExecutorOfEstateControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                               val cachingConnector: CachingConnector,
                                               val authConnector: AuthConnector,
                                               val formPartialRetriever: FormPartialRetriever,
                                               implicit val appConfig: AppConfig,
                                               val cc: MessagesControllerComponents) extends FrontendController(cc) with ExecutorOfEstateController

trait ExecutorOfEstateController extends RegistrationApplicantControllerWithEditMode {
  def form(implicit messsages: Messages) = executorOfEstateForm

  override def guardConditions: Set[Predicate] = guardConditionsApplicantExecutorOfEstateQuestion

  override def getKickoutReason = checkNotAnExecutorKickout

  override val storageFailureMessage = "Store registration details fails on is an executor submission"

  lazy val submitRoute = routes.ExecutorOfEstateController.onSubmit
  lazy val editSubmitRoute = routes.ExecutorOfEstateController.onEditSubmit

  def okForPageLoad(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.executor_of_estate(form, DeceasedInfoHelper.getDeceasedNameOrDefaultString(name), submitRoute))

  def okForEditPageLoad(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.executor_of_estate(form, DeceasedInfoHelper.getDeceasedNameOrDefaultString(name), editSubmitRoute, cancelToRegSummary))

  def badRequestForSubmit(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.executor_of_estate(form, DeceasedInfoHelper.getDeceasedNameOrDefaultString(name), submitRoute))

  def badRequestForEditSubmit(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.executor_of_estate(form, DeceasedInfoHelper.getDeceasedNameOrDefaultString(name), editSubmitRoute, cancelToRegSummary))

  def applyChangesToRegistrationDetails(rd: RegistrationDetails, ad: ApplicantDetails, mode: Mode.Value) = {
    val x = rd.applicantDetails.getOrElse(new ApplicantDetails(role = Some(appConfig.roleLeadExecutor))) copy (
      executorOfEstate = ad.executorOfEstate)
    rd copy (applicantDetails = Some(x))
  }

  def onwardRoute(rd: RegistrationDetails) = routes.ProbateLocationController.onPageLoad
}
