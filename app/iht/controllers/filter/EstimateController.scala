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
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.filter.estimate

import scala.concurrent.Future

class EstimateControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                       val cachingConnector: CachingConnector,
                                       val authConnector: AuthConnector,
                                       val cc: MessagesControllerComponents,
                                       val estimateView: estimate,
                                       implicit val appConfig: AppConfig) extends FrontendController(cc) with EstimateController

trait EstimateController extends FrontendController with I18nSupport {
  implicit val appConfig: AppConfig
  val estimateView: estimate
  def onPageLoad(jointAssets: Boolean, submitRoute: Call): Action[AnyContent] = Action.async {
    implicit request => {
      Future.successful(Ok(estimateView(estimateForm, jointAssets,
        submitRoute)))
    }
  }

  def onPageLoadJointAssets: Action[AnyContent] = onPageLoad(jointAssets = true, iht.controllers.filter.routes.EstimateController.onSubmitJointAssets)

  def onPageLoadWithoutJointAssets: Action[AnyContent] = onPageLoad(jointAssets = false, iht.controllers.filter.routes.EstimateController.onSubmitWithoutJointAssets)

  private def onSubmit(jointAssets: Boolean, submitRoute: Call) = Action.async {
    implicit request => {
      val boundForm = estimateForm.bindFromRequest()

      boundForm.fold(
        formWithErrors => Future.successful(BadRequest(estimateView(formWithErrors, jointAssets,
          submitRoute))), {
          choice => (choice.getOrElse(""), jointAssets) match {
            case (Constants.under325000, false) =>
              Future.successful(Redirect(iht.controllers.filter.routes.AnyAssetsController.onPageLoadWithoutJointAssets))
            case (Constants.under325000, true) =>
              Future.successful(Redirect(iht.controllers.filter.routes.AnyAssetsController.onPageLoadWithJointAssets))
            case (Constants.between325000and1million, false) =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseServiceController.onPageLoadOver))
            case (Constants.between325000and1million, true) =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseServiceController.onPageLoadOverWithJointAssets))
            case (Constants.moreThan1million, false) =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseIHT400Controller.onPageLoadWithoutJointAssets))
            case (Constants.moreThan1million, true) =>
              Future.successful(Redirect(iht.controllers.filter.routes.UseIHT400Controller.onPageLoadWithJointAssets))
            case (_, _) => throw new IllegalArgumentException("Invalid Data Submitted")
          }
        }
      )
    }
  }

  def onSubmitJointAssets: Action[AnyContent] = onSubmit(jointAssets = true, iht.controllers.filter.routes.EstimateController.onSubmitJointAssets)

  def onSubmitWithoutJointAssets: Action[AnyContent] = onSubmit(jointAssets = false, iht.controllers.filter.routes.EstimateController.onSubmitWithoutJointAssets)

}
