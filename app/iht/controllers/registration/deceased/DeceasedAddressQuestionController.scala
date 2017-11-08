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

package iht.controllers.registration.deceased

import iht.controllers.ControllerHelper.Mode
import iht.connector.IhtConnectors
import iht.forms.registration.DeceasedForms._
import iht.metrics.Metrics
import iht.models.{DeceasedDetails, RegistrationDetails}
import iht.utils.DeceasedInfoHelper
import iht.views.html.registration.{deceased => views}
import play.api.Logger
import play.api.data.Form
import play.api.mvc.{AnyContent, Request}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object DeceasedAddressQuestionController extends DeceasedAddressQuestionController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait DeceasedAddressQuestionController extends RegistrationDeceasedController {
  def form = deceasedAddressQuestionForm

  override def guardConditions: Set[Predicate] = guardConditionsDeceasedLastContactAddressQuestion

  override val storageFailureMessage = "Storage of registration details fails during deceased address question"

  def okForPageLoad(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.deceased_address_question(form,
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(name),
      routes.DeceasedAddressQuestionController.onSubmit())
    (request, language, applicationMessages, formPartialRetriever))

  def badRequestForSubmit(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.deceased_address_question(form,
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(name),
      routes.DeceasedAddressQuestionController.onSubmit())
    (request, language, applicationMessages, formPartialRetriever))

  def onwardRoute(rd: RegistrationDetails) = {
    val addressInUk = rd.deceasedDetails.flatMap(_.isAddressInUK)
    addressInUk.fold(cantFindAddressInUK) { addressInUk =>
      if (addressInUk) {
        routes.DeceasedAddressDetailsUKController.onPageLoad()
      } else {
        routes.DeceasedAddressDetailsOutsideUKController.onPageLoad()
      }
    }
  }

  def applyChangesToRegistrationDetails(rd: RegistrationDetails, dd: DeceasedDetails, mode: Mode.Value = Mode.Standard) = {
    val optDDCopy = rd.deceasedDetails.map(_ copy(isAddressInUK = dd.isAddressInUK))
    rd copy (deceasedDetails = optDDCopy)
  }

  private def cantFindAddressInUK = {
    val msg = "Could not retrieve UK or international location from deceased details in deceased address question"
    Logger.warn(msg)
    throw new RuntimeException(msg)
  }
}
