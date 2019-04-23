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
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.registration.RegistrationController
import iht.forms.registration.CoExecutorForms
import iht.models.UkAddress
import iht.utils.CommonHelper._
import iht.views.html.registration.{executor => views}
import javax.inject.Inject
import play.api.i18n.Lang
import play.api.mvc.{Call, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future


class OtherPersonsAddressControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                  val cachingConnector: CachingConnector,
                                                  val authConnector: AuthConnector,
                                                  val formPartialRetriever: FormPartialRetriever,
                                                  implicit val appConfig: AppConfig,
                                                  val cc: MessagesControllerComponents) extends FrontendController(cc) with OtherPersonsAddressController {

}

trait OtherPersonsAddressController extends RegistrationController with CoExecutorForms {
  def cachingConnector: CachingConnector

  override def guardConditions: Set[Predicate] = guardConditionsCoExecutorAddress

  def loadRouteUk(id: String) = routes.OtherPersonsAddressController.onPageLoadUK(id)

  def loadRouteAbroad(id: String) = routes.OtherPersonsAddressController.onPageLoadAbroad(id)

  def editLoadRouteUk(id: String) = routes.OtherPersonsAddressController.onEditPageLoadUK(id)

  def editLoadRouteAbroad(id: String) = routes.OtherPersonsAddressController.onEditPageLoadAbroad(id)

  def submitRouteUk(id: String) = routes.OtherPersonsAddressController.onSubmitUK(id)

  def submitRouteAbroad(id: String) = routes.OtherPersonsAddressController.onSubmitAbroad(id)

  def editSubmitRouteUk(id: String) = routes.OtherPersonsAddressController.onEditSubmitUK(id)

  def editSubmitRouteAbroad(id: String) = routes.OtherPersonsAddressController.onEditSubmitAbroad(id)

  def nextPageRoute = routes.ExecutorOverviewController.onPageLoad

  def isInternationalAddress: UkAddress => Boolean = addr => addr.postCode.trim.isEmpty &&
    addr.countryCode != appConfig.ukIsoCountryCode

  def onPageLoadUK(id: String) = onPageLoad(id, false, submitRouteUk(id), loadRouteAbroad(id))

  def onPageLoadAbroad(id: String) = onPageLoad(id, true, submitRouteAbroad(id), loadRouteUk(id))

  def onEditPageLoadUK(id: String) = onPageLoad(id, isInternational = false, editSubmitRouteUk(id),
    editLoadRouteAbroad(id), cancelToRegSummary)

  def onEditPageLoadAbroad(id: String) = onPageLoad(id, isInternational = true, editSubmitRouteAbroad(id),
    editLoadRouteUk(id), cancelToRegSummary)

  def onPageLoad(id: String, isInternational: Boolean, actionCall: Call, changeNationalityCall: Call,
                 cancelCall: Option[Call] = None) = authorisedForIht {
    implicit request => {
      implicit val lang: Lang = messagesApi.preferred(request).lang

      withRegistrationDetailsRedirectOnGuardCondition { rd =>
        val formType = if (isInternational) coExecutorAddressAbroadForm else coExecutorAddressUkForm
        findExecutor(id, rd.coExecutors) match {
          case Some(coExecutor) => {
            val form = if (coExecutor.isAddressInUk.fold(false)(_ == isInternational)) {
              formType
            } else {
              coExecutor.ukAddress.fold(formType)(address => formType.fill(address))
            }
            Future.successful(
              Ok(views.others_applying_for_probate_address(form, id, coExecutor.name, isInternational,
                actionCall, changeNationalityCall, cancelCall)))
          }
          case None => throw new Exception("Unknown id")
        }
      }
    }
  }

  def onSubmitUK(id: String) = submitAddress(id, isInternational = false, nextPageRoute, submitRouteUk(id), loadRouteAbroad(id))

  def onSubmitAbroad(id: String) = submitAddress(id, isInternational = true, nextPageRoute, submitRouteAbroad(id), loadRouteUk(id))

  def onEditSubmitUK(id: String) = submitAddress(id, isInternational = false, regSummaryRoute, editSubmitRouteUk(id),
    editLoadRouteAbroad(id), cancelToRegSummary)

  def onEditSubmitAbroad(id: String) = submitAddress(id, isInternational = true, regSummaryRoute, editSubmitRouteAbroad(id),
    editLoadRouteUk(id), cancelToRegSummary)

  def submitAddress(coExecutorId: String, isInternational: Boolean, actionCall: Call, onFailureActionCall: Call,
                    changeNationalityCall: Call,
                    cancelCall: Option[Call] = None) = {
    authorisedForIht {
      implicit request => {
        implicit val lang: Lang = messagesApi.preferred(request).lang

        withRegistrationDetails {
          rd => {
            val formType = if (isInternational) coExecutorAddressAbroadForm else coExecutorAddressUkForm
            val coExecutor = findExecutor(coExecutorId, rd.coExecutors).get
            val boundForm = formType.bindFromRequest()
            boundForm.fold(
              formWithErrors => {
                Future.successful(
                  BadRequest(views.others_applying_for_probate_address(formWithErrors, coExecutorId, coExecutor.name,
                    isInternational, onFailureActionCall, changeNationalityCall, cancelCall)))
              },
              (address: UkAddress) => {
                val coExecutorWithAddress = coExecutor copy(ukAddress = Some(address), isAddressInUk = Some(!isInternational))
                val newExecutors = rd.coExecutors.map { coExecutor =>

                  if (coExecutor.id.contains(coExecutorId)) coExecutorWithAddress else coExecutor
                }
                storeRegistrationDetails(rd copy (coExecutors = newExecutors), actionCall,
                  "Storage of registration details fails during other persons address submission")
              }
            )
          }
        }
      }
    }
  }
}
