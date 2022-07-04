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
import iht.constants.Constants
import iht.forms.FilterForms._
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.filter.filter_jointly_owned

import scala.concurrent.Future

class FilterJointlyOwnedControllerImpl @Inject()(val filterJointlyOwnedView: filter_jointly_owned,
                                                 val cc: MessagesControllerComponents,
                                                 implicit val appConfig: AppConfig) extends FrontendController(cc) with FilterJointlyOwnedController

trait FilterJointlyOwnedController extends FrontendController with I18nSupport {
  implicit val appConfig: AppConfig
  val filterJointlyOwnedView: filter_jointly_owned

  def onPageLoad: Action[AnyContent] = Action.async {
    implicit request => {
      Future.successful(Ok(filterJointlyOwnedView(filterJointlyOwnedForm)))
    }
  }

  def onSubmit: Action[AnyContent] = Action.async {
    implicit request => {
      val boundForm = filterJointlyOwnedForm.bindFromRequest

      boundForm.fold(
        formWithErrors => Future.successful(BadRequest(filterJointlyOwnedView(formWithErrors))),
        choice => choice.getOrElse("") match {
          case Constants.filterJointlyOwnedNo => Future.successful(Redirect(iht.controllers.filter.routes.EstimateController.onPageLoadWithoutJointAssets))
          case Constants.filterJointlyOwnedYes => Future.successful(Redirect(iht.controllers.filter.routes.EstimateController.onPageLoadJointAssets))
        }
      )
    }
  }

}
