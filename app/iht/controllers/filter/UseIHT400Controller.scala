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
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.filter.use_iht400

import scala.concurrent.Future

class UseIHT400ControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                        val ihtConnector: IhtConnector,
                                        val useIht400View: use_iht400,
                                        implicit val appConfig: AppConfig,
                                        val cc: MessagesControllerComponents) extends FrontendController(cc) with UseIHT400Controller

trait UseIHT400Controller extends FrontendController with I18nSupport {
  def cachingConnector: CachingConnector
  def ihtConnector: IhtConnector

  implicit val appConfig: AppConfig

  val useIht400View: use_iht400

  def onPageLoad(jointAssets: Boolean): Action[AnyContent] = Action.async {
    implicit request => {
      Future.successful(Ok(useIht400View(jointAssets)))
    }
  }

  def onPageLoadWithJointAssets: Action[AnyContent] = onPageLoad(true)

  def onPageLoadWithoutJointAssets: Action[AnyContent] = onPageLoad(false)

}
