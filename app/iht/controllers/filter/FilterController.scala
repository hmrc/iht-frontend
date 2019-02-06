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

package iht.controllers.filter

import iht.config.IhtFormPartialRetriever
import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.Constants
import iht.forms.FilterForms._
import iht.utils.MessagesHelper
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

/**
  * Created by adwelly on 21/10/2016.
  * Modelled closely on the PrivateBetaLandingPageController
  */

object FilterController extends FilterController {
  val cachingConnector = CachingConnector
  val ihtConnector = IhtConnector
}

trait FilterController extends FrontendController {

  def cachingConnector: CachingConnector
  def ihtConnector: IhtConnector

  implicit val formPartialRetriever: FormPartialRetriever = IhtFormPartialRetriever

  def onPageLoad = UnauthorisedAction.async {
    implicit request => {
      val refEndsWithCy = request.headers.get(REFERER).exists(_.endsWith(".cy"))

      if (refEndsWithCy) {
        Future.successful(Ok(iht.views.html.filter.filter_view(filterForm)(
          messages = MessagesHelper.messagesForLang(applicationMessages, "cy"),
          request = request, ihtFormPartialRetriever = formPartialRetriever)))
      } else {
        Future.successful(Ok(iht.views.html.filter.filter_view(filterForm)))
      }
    }
  }

  def redirectPageLoad = UnauthorisedAction {
    implicit user =>
      Redirect(iht.controllers.filter.routes.FilterController.onPageLoad())
  }

  def onSubmit = UnauthorisedAction.async {
    implicit request => {
      val boundForm = filterForm.bindFromRequest()

      boundForm.fold(
        formWithErrors => {
          Future.successful(BadRequest(iht.views.html.filter.filter_view(formWithErrors)))
        }, {
          choice => {
            choice.getOrElse("") match {
              case Constants.continueEstateReport =>
                Future.successful(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad()))
              case Constants.alreadyStarted =>
                Future.successful(Redirect(iht.controllers.registration.routes.RegistrationChecklistController.onPageLoad()))
              case Constants.agent =>
                Future.successful(Redirect(iht.controllers.filter.routes.AgentController.onPageLoad()))
              case Constants.register =>
                Future.successful(Redirect(iht.controllers.filter.routes.DomicileController.onPageLoad()))
            }
          }
        }
      )
    }
  }
}
