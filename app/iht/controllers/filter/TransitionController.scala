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
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.filter.use_paper_form

import scala.concurrent.Future

class TransitionControllerImpl @Inject()(val usePaperFormView: use_paper_form,
                                         val cc: MessagesControllerComponents,
                                         implicit val appConfig: AppConfig) extends FrontendController(cc) with TransitionController

trait TransitionController extends FrontendController with I18nSupport {
  implicit val appConfig: AppConfig

  def onPageLoadScotland: Action[AnyContent] = doPageLoad("iht.countries.scotland")
  def onPageLoadNorthernIreland: Action[AnyContent] = doPageLoad("iht.countries.northernIreland")
  def onPageLoadOtherCountry: Action[AnyContent] = doPageLoad("page.iht.filter.domicile.choice.other")

  val usePaperFormView: use_paper_form

  def doPageLoad(countryMessageKey: String): Action[AnyContent] = Action.async {
    implicit request => {
      Future.successful(Ok(usePaperFormView(countryMessageKey)))
    }
  }
}
