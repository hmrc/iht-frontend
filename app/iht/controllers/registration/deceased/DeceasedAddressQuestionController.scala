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

package iht.controllers.registration.deceased

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.ControllerHelper.Mode
import iht.forms.registration.DeceasedForms._
import iht.models.{DeceasedDetails, RegistrationDetails}
import iht.utils.DeceasedInfoHelper
import iht.views.html.registration.{deceased => views}
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class DeceasedAddressQuestionControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                      val cachingConnector: CachingConnector,
                                                      val authConnector: AuthConnector,
                                                      val formPartialRetriever: FormPartialRetriever,
                                                      implicit val appConfig: AppConfig,
                                                      val cc: MessagesControllerComponents) extends FrontendController(cc) with DeceasedAddressQuestionController {

}

trait DeceasedAddressQuestionController extends RegistrationDeceasedController {
  def form(implicit messsages: Messages) = deceasedAddressQuestionForm

  override def guardConditions: Set[Predicate] = guardConditionsDeceasedLastContactAddressQuestion

  override val storageFailureMessage = "Storage of registration details fails during deceased address question"

  def okForPageLoad(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.deceased_address_question(form,
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(name),
      routes.DeceasedAddressQuestionController.onSubmit())
    )

  def badRequestForSubmit(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.deceased_address_question(form,
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(name),
      routes.DeceasedAddressQuestionController.onSubmit())
    )

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
    logger.warn(msg)
    throw new RuntimeException(msg)
  }
}
