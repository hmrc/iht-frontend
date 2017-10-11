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
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.Call
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

object EstimateController extends EstimateController

trait EstimateController extends FrontendController {
  implicit val formPartialRetriever: FormPartialRetriever = IhtFormPartialRetriever

  def onPageLoad(jointAssets: Boolean, submitRoute: Call) = UnauthorisedAction.async {
    implicit request => {
      Future.successful(Ok(iht.views.html.filter.estimate(estimateForm, jointAssets,
        submitRoute)))
    }
  }

  def onPageLoadJointAssets = onPageLoad(jointAssets = true, iht.controllers.filter.routes.EstimateController.onSubmitJointAssets())

  def onPageLoadWithoutJointAssets = onPageLoad(jointAssets = false, iht.controllers.filter.routes.EstimateController.onSubmitWithoutJointAssets())

  private def onSubmit(jointAssets: Boolean, submitRoute: Call) = UnauthorisedAction.async {
    implicit request => {
      val boundForm = estimateForm.bindFromRequest()

      boundForm.fold(
        formWithErrors => Future.successful(BadRequest(iht.views.html.filter.estimate(formWithErrors, jointAssets,
          submitRoute))), {
          choice => (choice.getOrElse(""), jointAssets) match {
            case (Constants.under325000, false) =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseServiceController.onPageLoadUnder()))
            case (Constants.under325000, true) =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseServiceController.onPageLoadUnderWithJointAssets()))
            case (Constants.between325000and1million, false) =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseServiceController.onPageLoadOver()))
            case (Constants.between325000and1million, true) =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseServiceController.onPageLoadOverWithJointAssets()))
            case (Constants.moreThan1million, false) =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseIHT400Controller.onPageLoadWithoutJointAssets()))
            case (Constants.moreThan1million, true) =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseIHT400Controller.onPageLoadWithJointAssets()))
          }
        }
      )
    }
  }

  def onSubmitJointAssets = onSubmit(jointAssets = true, iht.controllers.filter.routes.EstimateController.onSubmitJointAssets())

  def onSubmitWithoutJointAssets = onSubmit(jointAssets = false, iht.controllers.filter.routes.EstimateController.onSubmitWithoutJointAssets())

}
