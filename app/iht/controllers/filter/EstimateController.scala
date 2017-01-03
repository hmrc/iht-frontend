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

import iht.constants.Constants
import iht.controllers.auth.CustomPasscodeAuthentication
import iht.forms.FilterForms._
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object EstimateController extends EstimateController

trait EstimateController extends FrontendController with CustomPasscodeAuthentication {
  def onPageLoad = customAuthenticatedActionAsync {
    implicit request => {
      Future.successful(Ok(iht.views.html.filter.estimate(estimateForm)))
    }
  }

  def onSubmit = customAuthenticatedActionAsync {
    implicit request => {
      val boundForm = estimateForm.bindFromRequest()

      boundForm.fold(
        formWithErrors => Future.successful(BadRequest(iht.views.html.filter.estimate(formWithErrors))), {
          choice => choice.getOrElse("") match {
            case Constants.under325000 =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseServiceController.onPageLoadUnder()))
            case Constants.between325000and1million =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseServiceController.onPageLoadOver()))
            case Constants.moreThan1million =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseIHT400Controller.onPageLoad()))
          }
        }
      )
    }
  }
}
