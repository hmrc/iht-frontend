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

package iht.controllers.registration.executor

import iht.config.AppConfig
import iht.connector.CachingConnector
import iht.controllers.registration.{RegistrationController, routes => registrationRoutes}
import iht.forms.registration.CoExecutorForms
import iht.metrics.IhtMetrics
import iht.models.RegistrationDetails
import iht.utils.AddressHelper
import iht.utils.CommonHelper._
import iht.views.html.registration.executor.others_applying_for_probate
import javax.inject.Inject
import play.api.mvc.{Call, MessagesControllerComponents, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class OthersApplyingForProbateControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                                       val ihtMetrics: IhtMetrics,
                                                       val authConnector: AuthConnector,
                                                       val othersApplyingForProbateView: others_applying_for_probate,
                                                       implicit val appConfig: AppConfig,
                                                       val cc: MessagesControllerComponents) extends FrontendController(cc) with OthersApplyingForProbateController

trait OthersApplyingForProbateController extends RegistrationController with CoExecutorForms {
  def cachingConnector: CachingConnector
  override def guardConditions: Set[Predicate] = Set(AddressHelper.isThereAnApplicantAddress)

  private def submitRoute(arrivedFromOverview: Boolean) =
    if (arrivedFromOverview) {
      routes.OthersApplyingForProbateController.onSubmitFromOverview()
    } else {
      routes.OthersApplyingForProbateController.onSubmit()
    }

  def onPageLoad() = pageLoad(arrivedFromOverview = false)

  def onPageLoadFromOverview() = pageLoad(arrivedFromOverview = true)

  def onEditPageLoad() = pageLoad(arrivedFromOverview = false, cancelToRegSummary)
  val othersApplyingForProbateView: others_applying_for_probate
  private def pageLoad(arrivedFromOverview: Boolean, cancelCall: Option[Call] = None) = authorisedForIht {
      implicit request =>
        withRegistrationDetailsRedirectOnGuardCondition { rd =>
          Future.successful(Ok(othersApplyingForProbateView(
            othersApplyingForProbateForm.fill(rd.areOthersApplyingForProbate),
            submitRoute(arrivedFromOverview), cancelCall)))
        }
  }

  def onSubmit() = submit(arrivedFromOverview = false)

  def onSubmitFromOverview() = submit(arrivedFromOverview = true)

  def onEditSubmit() = submit(arrivedFromOverview = false, cancelToRegSummary)

  private def submitOnwardResult(othersApplying: Boolean, arrivedFromOverview: Boolean, rd: RegistrationDetails): Result = {
    othersApplying match {
      case true if arrivedFromOverview && rd.coExecutors.isEmpty => Redirect(routes.CoExecutorPersonalDetailsController.onPageLoad(None))
      case true if arrivedFromOverview => Redirect(routes.ExecutorOverviewController.onPageLoad())
      case true if !arrivedFromOverview => Redirect(routes.CoExecutorPersonalDetailsController.onPageLoad(None))
      case false => Redirect(registrationRoutes.RegistrationSummaryController.onPageLoad())
    }
  }

  def submit(arrivedFromOverview: Boolean, cancelCall: Option[Call] = None) = authorisedForIht {
      implicit request => {
        withRegistrationDetails { rd: RegistrationDetails =>
          val boundForm = othersApplyingForProbateForm.bindFromRequest
          boundForm.fold(
            formWithErrors => {
              Future.successful(BadRequest(othersApplyingForProbateView(formWithErrors,
                submitRoute(arrivedFromOverview), cancelCall)))
            },

            areOthersApplying => {
              val rdUpdated = if (getOrException(areOthersApplying)) {
                rd copy(areOthersApplyingForProbate = Some(true), coExecutors = rd.coExecutors)
              } else {
                rd copy(areOthersApplyingForProbate = Some(false), coExecutors = Seq())
              }
              cachingConnector.storeRegistrationDetails(rdUpdated).flatMap({
                case Some(_) => Future.successful(submitOnwardResult(
                  othersApplying = getOrException(rdUpdated.areOthersApplyingForProbate),
                  arrivedFromOverview = arrivedFromOverview,
                  rd = rd))
                case None => Future.successful(InternalServerError)
              }
              )
            })
        }
      }
  }
}
