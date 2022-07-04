/*
 * Copyright 2022 HM Revenue & Customs
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
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.registration.RegistrationController
import iht.views.html.registration.executor.delete_coexecutor_confirm
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class DeleteCoExecutorControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                               val cachingConnector: CachingConnector,
                                               val authConnector: AuthConnector,
                                               val deleteCoexecutorConfirmView: delete_coexecutor_confirm,
                                               implicit val appConfig: AppConfig,
                                               val cc: MessagesControllerComponents) extends FrontendController(cc) with DeleteCoExecutorController

trait DeleteCoExecutorController extends RegistrationController {
  def areThereOthersApplying: Predicate = (rd, _) => rd.areOthersApplyingForProbate.getOrElse(false)

  def isThereMoreThanOneCoExecutor: Predicate = (rd, _) => rd.coExecutors.nonEmpty

  override def guardConditions: Set[Predicate] = Set(areThereOthersApplying, isThereMoreThanOneCoExecutor)

  def cachingConnector: CachingConnector
  val deleteCoexecutorConfirmView: delete_coexecutor_confirm
  def onPageLoad(id: String) = authorisedForIht {
    implicit request =>
      withRegistrationDetailsRedirectOnGuardCondition {
        rd => {
          val index = rd.coExecutors.indexWhere(_.id.contains(id))

          if (index == -1) {
            logger.warn("Coexecutor confirm deletion of id " + id + " fails. Id not found. Redirecting to Internal Sever Error")
            Future.successful(InternalServerError("Coexecutor confirm deletion of id " + id + " fails. Id not found."))
          } else {
            val coExecutor = rd.coExecutors(index)
            Future.successful(Ok(deleteCoexecutorConfirmView(coExecutor)))
          }
        }
      }
  }

  def onSubmit(id: String) = authorisedForIht {
    implicit request => {
      withRegistrationDetailsRedirectOnGuardCondition {
        rd => {
          val index = rd.coExecutors.indexWhere(_.id.contains(id))

          if (index == -1) {
            logger.warn("Coexecutor deletion of id " + id + " fails. Id not found. Redirecting to Internal Sever Error")
            Future.successful(InternalServerError("Coexecutor deletion of id " + id + " fails. Id not found."))
          } else {
            val newCoexecutors = rd.coExecutors.patch(index, Nil, 1)
            val newRegistrationDetails = rd copy (coExecutors = newCoexecutors)
            storeRegistrationDetails(newRegistrationDetails,
              routes.ExecutorOverviewController.onPageLoad,
              "Failed to store registration details during DeleteCoExecutor"
            )
          }
        }
      }
    }
  }
}