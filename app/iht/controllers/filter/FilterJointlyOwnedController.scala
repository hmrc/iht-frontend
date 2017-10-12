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

package iht.controllers.filter

import iht.config.IhtFormPartialRetriever
import iht.constants.Constants
import iht.forms.FilterForms._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

object FilterJointlyOwnedController extends FilterJointlyOwnedController

trait FilterJointlyOwnedController extends FrontendController {

  implicit val formPartialRetriever: FormPartialRetriever = IhtFormPartialRetriever

  def onPageLoad: Action[AnyContent] = UnauthorisedAction.async {
    implicit request => {
      Future.successful(Ok(iht.views.html.filter.filter_jointly_owned(filterJointlyOwnedForm)))
    }
  }

  def onSubmit: Action[AnyContent] = UnauthorisedAction.async {
    implicit request => {
      val boundForm = filterJointlyOwnedForm.bindFromRequest

      boundForm.fold(
        formWithErrors => Future.successful(BadRequest(iht.views.html.filter.filter_jointly_owned(formWithErrors))),
        choice => choice.getOrElse("") match {
          case Constants.filterJointlyOwnedNo => Future.successful(Redirect(iht.controllers.filter.routes.EstimateController.onPageLoadWithoutJointAssets()))
          case Constants.filterJointlyOwnedYes => Future.successful(Redirect(iht.controllers.filter.routes.EstimateController.onPageLoadJointAssets()))
        }
      )
    }
  }

}