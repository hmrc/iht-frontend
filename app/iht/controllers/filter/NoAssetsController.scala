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

import iht.views.html.filter.no_assets
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.Future

class NoAssetsControllerImpl @Inject()(val cc: MessagesControllerComponents,
                                       val noAssetsView: no_assets
                                      ) extends FrontendController(cc) with NoAssetsController

trait NoAssetsController extends FrontendController with I18nSupport {

  val noAssetsView: no_assets

  def onPageLoad(jointAssets: Boolean): Action[AnyContent] = Action.async {
    implicit request => {
      Future.successful(Ok(noAssetsView(jointAssets)))
    }
  }

  def onPageLoadWithJointAssets: Action[AnyContent] = onPageLoad(true)

  def onPageLoadWithoutJointAssets: Action[AnyContent] = onPageLoad(false)

}
