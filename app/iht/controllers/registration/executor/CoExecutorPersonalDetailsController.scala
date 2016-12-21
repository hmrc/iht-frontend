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

package iht.controllers.registration.executor

import iht.connector.CachingConnector
import iht.constants.IhtProperties
import iht.controllers.ControllerHelper.Mode
import iht.controllers.IhtConnectors
import iht.controllers.registration.RegistrationController
import iht.forms.registration.CoExecutorForms._
import iht.metrics.Metrics
import iht.models.{CoExecutor, RegistrationDetails}
import iht.views.html.registration.{executor => views}
import play.api.data.Form
import play.api.mvc.Call
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object CoExecutorPersonalDetailsController extends CoExecutorPersonalDetailsController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait CoExecutorPersonalDetailsController extends RegistrationController {
  def cachingConnector: CachingConnector

  override def guardConditions = guardConditionsCoExecutorPersonalDetails

  def onPageLoad(id: Option[String]) = pageLoad(id, routes.CoExecutorPersonalDetailsController.onSubmit(id))
  def onEditPageLoad(id: String) = pageLoad(Some(id), routes.CoExecutorPersonalDetailsController.onEditSubmit(id),
    Mode.Edit, cancelToRegSummary)

  def pageLoad(id: Option[String], actionCall: Call, mode: Mode.Value = Mode.Standard,
                  cancelCall: Option[Call] = None) = authorisedForIht {
    implicit user => implicit request =>
      withRegistrationDetailsRedirectOnGuardCondition { (rd: RegistrationDetails) =>

        val form: Form[CoExecutor] = id match {
          case None =>
            if (rd.coExecutors.length >= IhtProperties.maxCoExecutors) {
              throw new Exception("Attempting to add too many co-executors")
            }
            else {
              coExecutorPersonalDetailsForm
            }
          case Some(identifier) =>
            val coExecutor = rd.coExecutors.find(_.id == id)
            coExecutor match {
              case None => throw new Exception(s"Could not find co-executor with id: $identifier")
              case Some(coExec) => coExecutorPersonalDetailsForm.fill(coExec)
            }
        }

        Future.successful(Ok(views.coexecutor_personal_details(form, mode, actionCall, cancelCall)))
      }
  }

  private def submitNewCoExecutor(rd: RegistrationDetails, coExecutor: CoExecutor, mode: Mode.Value)(implicit hc: HeaderCarrier) = {
    if (rd.coExecutors.length >= IhtProperties.maxCoExecutors) {
      throw new Exception("Attempting to add too many co-executors")
    }
    else {
      val newId = iht.models.nextId(rd.coExecutors)
      val coExec = coExecutor.copy(id = Some(newId))
      val route = getRoute(coExecutor.isAddressInUk.getOrElse(true), newId, mode)
      val newRd = rd.copy(coExecutors = rd.coExecutors :+ coExec)
      storeRegistrationDetails(newRd,
        route,
        "Storage of registration details fails during coexecutor personal details for new address")
    }
  }

  private def submitEditOfExistingCoExecutor(rd: RegistrationDetails, id: String, coExecutor: CoExecutor, mode: Mode.Value)(implicit hc: HeaderCarrier) = {
    val index = rd.coExecutors.indexWhere(c => c.id.contains(id))
    if (index == -1) {
      throw new Exception(s"Could not find co-executor with id: $id")
    } else {
      val updatedCoExec = rd.coExecutors(index).updatePersonalDetails(coExecutor)
      val route = getRoute(coExecutor.isAddressInUk.getOrElse(true), id, mode)
      val newRd = rd.copy(coExecutors = rd.coExecutors.updated(index, updatedCoExec))
      storeRegistrationDetails(newRd, route, "Storage of registration details fails during coexecutor personal details for existing address")
    }
  }

  def onSubmit(id: Option[String]) = submit(id, routes.CoExecutorPersonalDetailsController.onSubmit(id))
  def onEditSubmit(id: String) = submit(Some(id), routes.CoExecutorPersonalDetailsController.onEditSubmit(id),
    Mode.Edit, cancelToRegSummary)

  def submit(id: Option[String], onFailureActionCall: Call, mode: Mode.Value = Mode.Standard,
             cancelCall: Option[Call] = None) = authorisedForIht {
    implicit user => implicit request =>
      withRegistrationDetailsRedirectOnGuardCondition { (rd: RegistrationDetails) =>

        val formType =
          if (mode == Mode.Standard) coExecutorPersonalDetailsForm
          else coExecutorPersonalDetailsEditForm

        val boundForm = formType.bindFromRequest()

        boundForm.fold(formWithErrors => {
          Future.successful(BadRequest(views.coexecutor_personal_details(formWithErrors, mode,
            onFailureActionCall, cancelCall)))
        },
          coExecutor =>
            id match {
              case None => submitNewCoExecutor(rd, coExecutor, mode)
              case Some(identifier: String) => submitEditOfExistingCoExecutor(rd, identifier, coExecutor, mode)
            }
        )
      }
  }

  def getRoute(isInUk: Boolean, id: String, mode: Mode.Value) = mode match {
    case Mode.Edit => regSummaryRoute
    case Mode.Standard if isInUk => routes.OtherPersonsAddressController.onPageLoadUK(id)
    case _ => routes.OtherPersonsAddressController.onPageLoadAbroad(id)
  }
}
