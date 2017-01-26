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

package iht.controllers.registration.executor

import iht.connector.CachingConnector
import iht.controllers.IhtConnectors
import iht.controllers.registration.{RegistrationController, routes => registrationRoutes}
import iht.forms.registration.CoExecutorForms._
import iht.metrics.Metrics
import iht.models.RegistrationDetails
import iht.utils.CommonHelper._
import play.api.mvc.Call

import scala.concurrent.Future

object OthersApplyingForProbateController extends OthersApplyingForProbateController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait OthersApplyingForProbateController extends RegistrationController {
  def cachingConnector: CachingConnector

  override def guardConditions: Set[Predicate] = Set(isThereAnApplicantAddress)

  def metrics: Metrics

  private def submitRoute(arrivedFromOverview: Boolean) =
    if (arrivedFromOverview) {
      routes.OthersApplyingForProbateController.onSubmitFromOverview
    } else {
      routes.OthersApplyingForProbateController.onSubmit
    }

  def onPageLoad() = pageLoad(false)

  def onPageLoadFromOverview() = pageLoad(true)

  def onEditPageLoad() = pageLoad(arrivedFromOverview = false, cancelToRegSummary)

  private def pageLoad(arrivedFromOverview: Boolean, cancelCall: Option[Call] = None) = authorisedForIht {
    implicit user => implicit request =>
      withRegistrationDetailsRedirectOnGuardCondition { rd =>
        Future.successful(Ok(iht.views.html.registration.executor.others_applying_for_probate(
          othersApplyingForProbateForm.fill(rd.areOthersApplyingForProbate),
          submitRoute(arrivedFromOverview), cancelCall)))
      }
  }

  def onSubmit() = submit(false)

  def onSubmitFromOverview() = submit(true)

  def onEditSubmit() = submit(arrivedFromOverview = false, cancelToRegSummary)

  def submit(arrivedFromOverview: Boolean, cancelCall: Option[Call] = None) = authorisedForIht {
    implicit user => implicit request => {
      withRegistrationDetails { (rd: RegistrationDetails) =>
        val boundForm = othersApplyingForProbateForm.bindFromRequest
        boundForm.fold(
          formWithErrors => {
            Future.successful(BadRequest(iht.views.html.registration.executor.others_applying_for_probate(formWithErrors,
              submitRoute(arrivedFromOverview), cancelCall)))
          },

          areOthersApplying => {
            val rdUpdated = getOrException(areOthersApplying) match {
              case true => rd copy(areOthersApplyingForProbate = Some(true), coExecutors = rd.coExecutors)
              case false => rd copy(areOthersApplyingForProbate = Some(false), coExecutors = Seq())
            }
            cachingConnector.storeRegistrationDetails(rdUpdated).flatMap(storeResult => {
              val route = getOrException(rdUpdated.areOthersApplyingForProbate) match {
                case true if arrivedFromOverview && rd.coExecutors.isEmpty => Redirect(routes.CoExecutorPersonalDetailsController.onPageLoad(None))
                case true if arrivedFromOverview => Redirect(routes.ExecutorOverviewController.onPageLoad())
                case true if !arrivedFromOverview => Redirect(routes.CoExecutorPersonalDetailsController.onPageLoad(None))
                case false => Redirect(registrationRoutes.RegistrationSummaryController.onPageLoad())
              }
              storeResult match {
                case Some(_) => Future.successful(route)
                case None => Future.successful(InternalServerError)
              }
            })
        })
      }
    }
  }
}
