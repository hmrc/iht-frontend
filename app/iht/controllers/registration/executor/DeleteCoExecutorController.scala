/*
 * Copyright 2018 HM Revenue & Customs
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
import iht.constants.IhtProperties
import iht.connector.IhtConnectors
import iht.controllers.registration.RegistrationController
import iht.metrics.Metrics
import play.Logger
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.Future

trait DeleteCoExecutorController extends RegistrationController {
  def areThereOthersApplying: Predicate = (rd, _) => rd.areOthersApplyingForProbate.getOrElse(false)

  def isThereMoreThanOneCoExecutor: Predicate = (rd, _) => rd.coExecutors.nonEmpty

  override def guardConditions: Set[Predicate] = Set(areThereOthersApplying, isThereMoreThanOneCoExecutor)

  def cachingConnector: CachingConnector

  def onPageLoad(id: String) = authorisedForIht {
    implicit user => implicit request =>
      withRegistrationDetailsRedirectOnGuardCondition {
        rd => {
          val index = rd.coExecutors.indexWhere(_.id.contains(id))

          if (index == -1) {
            Logger.warn("Coexecutor confirm deletion of id " + id + " fails. Id not found. Redirecting to Internal Sever Error")
            Future.successful(InternalServerError("Coexecutor confirm deletion of id " + id + " fails. Id not found."))
          } else {
            val coExecutor = rd.coExecutors(index)
            val addr = IhtProperties.ukIsoCountryCode
            Future.successful(Ok(iht.views.html.registration.executor.delete_coexecutor_confirm(coExecutor)))
          }
        }
      }
  }

  def onSubmit(id: String) = authorisedForIht {
    implicit user => implicit request => {
      withRegistrationDetailsRedirectOnGuardCondition {
        rd => {
          val index = rd.coExecutors.indexWhere(_.id.contains(id))

          if (index == -1) {
            Logger.warn("Coexecutor deletion of id " + id + " fails. Id not found. Redirecting to Internal Sever Error")
            Future.successful(InternalServerError("Coexecutor deletion of id " + id + " fails. Id not found."))
          } else {
            val newCoexecutors = rd.coExecutors.patch(index, Nil, 1)
            val newRegistrationDetails = rd copy (coExecutors = newCoexecutors)
            storeRegistrationDetails(newRegistrationDetails,
              routes.ExecutorOverviewController.onPageLoad(),
              "Failed to store registration details during DeleteCoExecutor"
            )
          }
        }
      }
    }
  }
}

object DeleteCoExecutorController extends DeleteCoExecutorController with IhtConnectors {
  def metrics: Metrics = Metrics
}
