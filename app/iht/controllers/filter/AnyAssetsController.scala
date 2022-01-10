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

import javax.inject.Inject
import iht.views.html.filter.any_assets
import iht.forms.FilterForms._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class AnyAssetsControllerImpl @Inject()(val cc: MessagesControllerComponents,
                                        val anyAssetsView: any_assets,
                                        implicit val appConfig: AppConfig) extends FrontendController(cc) with AnyAssetsController

trait AnyAssetsController extends FrontendController with I18nSupport {
  implicit val appConfig: AppConfig
  val anyAssetsView: any_assets

  def onPageLoad(jointAssets: Boolean): Action[AnyContent] = Action.async { implicit request => {
      Future.successful(Ok(anyAssetsView(anyAssetsForm, jointAssets)))
    }
  }

  def onPageLoadWithJointAssets: Action[AnyContent] = onPageLoad(jointAssets = true)
  def onPageLoadWithoutJointAssets: Action[AnyContent] = onPageLoad(jointAssets = false)

  def onSubmit(jointAssets: Boolean): Action[AnyContent] = Action.async { implicit request => {
      val boundForm = anyAssetsForm.bindFromRequest()

      boundForm.fold(
        formWithErrors => Future.successful(BadRequest(anyAssetsView(formWithErrors, jointAssets))),
        choice => (choice.getOrElse(""), jointAssets) match {
          case (Constants.anyAssetsYes, false) => Future.successful(Redirect(iht.controllers.filter.routes.UseServiceController.onPageLoadUnder()))
          case (Constants.anyAssetsYes, true) => Future.successful(Redirect(iht.controllers.filter.routes.UseServiceController.onPageLoadUnderWithJointAssets()))
          case (Constants.anyAssetsNo, true) => Future.successful(Redirect(iht.controllers.filter.routes.NoAssetsController.onPageLoadWithJointAssets()))
          case (Constants.anyAssetsNo, false) => Future.successful(Redirect(iht.controllers.filter.routes.NoAssetsController.onPageLoadWithoutJointAssets()))
          case (_, _) => throw new IllegalArgumentException("Invalid Data Submitted")
        }
      )
    }
  }

  def onSubmitWithJointAssets: Action[AnyContent] = onSubmit(jointAssets = true)
  def onSubmitWithoutJointAssets: Action[AnyContent] = onSubmit(jointAssets = false)

}
