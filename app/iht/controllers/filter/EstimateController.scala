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

import javax.inject.Singleton

import iht.constants.Constants
import iht.controllers.auth.CustomPasscodeAuthentication
import iht.forms.FilterForms._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

@Singleton
class EstimateController extends FrontendController with CustomPasscodeAuthentication {
  def onPageLoad = customAuthenticatedActionAsync {
    implicit request => {
      Future.successful(Ok(iht.views.html.filter.estimate(applicationForms.estimateForm)))
    }
  }

  def onSubmit = customAuthenticatedActionAsync {
    implicit request => {
      val boundForm = estimateForm.bindFromRequest()

      boundForm.fold(
        formWithErrors => Future.successful(BadRequest(iht.views.html.filter.estimate(formWithErrors))), {
          choice => choice.getOrElse("") match {
            case constants.under325000 =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseServiceController.onPageLoadUnder()))
            case constants.between325000and1million =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseServiceController.onPageLoadOver()))
            case constants.moreThan1million =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseIHT400Controller.onPageLoad()))
          }
        }
      )
    }
  }
}
