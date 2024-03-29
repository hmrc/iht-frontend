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

package iht.controllers.filter

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.Constants
import iht.forms.FilterForms._
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.filter.filter_view

import scala.concurrent.Future

class FilterControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                     val cachingConnector: CachingConnector,
                                     val authConnector: AuthConnector,
                                     val cc: MessagesControllerComponents,
                                     val filterViewView: filter_view,
                                     implicit val appConfig: AppConfig) extends FrontendController(cc) with FilterController

trait FilterController extends FrontendController with I18nSupport {
  implicit val appConfig: AppConfig
  def cachingConnector: CachingConnector
  def ihtConnector: IhtConnector

  val filterViewView: filter_view

  def onPageLoad: Action[AnyContent] = Action.async {
    implicit request => {
      val refEndsWithCy = request.headers.get(REFERER).exists(_.endsWith(".cy"))

      if (refEndsWithCy) {
        Future.successful(Ok(filterViewView(filterForm)(
          messages = messagesApi.preferred(Seq(Lang("cy"))),
          request = request
        )))
      } else {
        Future.successful(Ok(filterViewView(filterForm)))
      }
    }
  }

  def redirectPageLoad: Action[AnyContent] = Action {
    Redirect(iht.controllers.filter.routes.FilterController.onPageLoad)
  }

  def onSubmit: Action[AnyContent] = Action.async {
    implicit request => {
      val boundForm = filterForm.bindFromRequest()

      boundForm.fold(
        formWithErrors => {
          Future.successful(BadRequest(filterViewView(formWithErrors)))
        }, {
          choice => {
            choice.getOrElse("") match {
              case Constants.continueEstateReport =>
                Future.successful(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad))
              case Constants.alreadyStarted =>
                Future.successful(Redirect(iht.controllers.registration.routes.RegistrationChecklistController.onPageLoad))
              case Constants.agent =>
                Future.successful(Redirect(iht.controllers.filter.routes.AgentController.onPageLoad))
              case Constants.register =>
                Future.successful(Redirect(iht.controllers.filter.routes.DomicileController.onPageLoad))
            }
          }
        }
      )
    }
  }
}
