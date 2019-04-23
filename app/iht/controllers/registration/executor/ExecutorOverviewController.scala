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

package iht.controllers.registration.executor

import iht.config.AppConfig
import iht.connector.CachingConnector
import iht.controllers.registration.{RegistrationController, routes => registrationRoutes}
import iht.forms.registration.CoExecutorForms
import iht.metrics.IhtMetrics
import iht.models.RegistrationDetails
import javax.inject.Inject
import play.api.data.Form
import play.api.mvc.{AnyContent, Call, MessagesControllerComponents, Request}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class ExecutorOverviewControllerImpl @Inject()(val metrics: IhtMetrics,
                                               val cachingConnector: CachingConnector,
                                               val authConnector: AuthConnector,
                                               val formPartialRetriever: FormPartialRetriever,
                                               implicit val appConfig: AppConfig,
                                               val cc: MessagesControllerComponents) extends FrontendController(cc) with ExecutorOverviewController


trait ExecutorOverviewController extends RegistrationController with CoExecutorForms {

  def cachingConnector: CachingConnector
  override def guardConditions: Set[Predicate] = Set((rd, _) => rd.areOthersApplyingForProbate.getOrElse(false))
  def metrics: IhtMetrics

  def submitRoute = routes.ExecutorOverviewController.onSubmit()
  def editSubmitRoute = routes.ExecutorOverviewController.onEditSubmit()

  private def badRequest(rd: RegistrationDetails, submitRoute: Call, showCancelRoute: Boolean,
                         formWithErrors: Form[Option[Boolean]], request: Request[AnyContent]) =
    {
      implicit val req = request
      Future.successful(
        BadRequest(iht.views.html.registration.executor.executor_overview(
          formWithErrors,
          rd.areOthersApplyingForProbate.get,
          rd.coExecutors,
          submitRoute,
          if (showCancelRoute) cancelToRegSummary else None)))
    }

  private def goodRequest(rd: RegistrationDetails, submitRoute: Call, showCancelRoute: Boolean, request: Request[AnyContent]) =
    {
      implicit val req = request
      Future.successful(
        Ok(iht.views.html.registration.executor.executor_overview(executorOverviewForm,
          rd.areOthersApplyingForProbate.getOrElse(false),
          rd.coExecutors,
          submitRoute,
          if (showCancelRoute) cancelToRegSummary else None)))
    }

  def onPageLoad = pageLoad(showCancelRoute = false, submitRoute)

  def onEditPageLoad = pageLoad(showCancelRoute = true, editSubmitRoute)

  private def pageLoad(showCancelRoute: Boolean, route: Call) = authorisedForIht {
    implicit request =>
      withRegistrationDetailsRedirectOnGuardCondition { goodRequest(_, route, showCancelRoute, request) }
  }

  def onSubmit = submit(showCancelRoute = false, submitRoute)

  def onEditSubmit = submit(showCancelRoute = true, editSubmitRoute)

  private def submit(showCancelRoute: Boolean, route: Call) = authorisedForIht {
    implicit request =>
      withRegistrationDetailsRedirectOnGuardCondition { rd =>
        val boundForm = executorOverviewForm.bindFromRequest
        boundForm.fold(formWithErrors => badRequest(rd, route, showCancelRoute, formWithErrors, request), {addMore =>
          (addMore, rd.areOthersApplyingForProbate, rd.coExecutors.isEmpty) match {
            case (Some(true), Some(true), _) =>
              Future.successful(Redirect(routes.CoExecutorPersonalDetailsController.onPageLoad(None)))
            case (Some(false), Some(true), true) =>
              badRequest(rd, route, showCancelRoute,
                boundForm.withError("addMoreCoExecutors",
                  "error.applicant.insufficientCoExecutors"), request)
            case _ => Future.successful(Redirect(registrationRoutes.RegistrationSummaryController.onPageLoad()))
          }
        })
      }
  }
}