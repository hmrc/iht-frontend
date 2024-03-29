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
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.filter.domicile

import scala.concurrent.Future

class DomicileControllerImpl @Inject()(val domicileView: domicile,
                                       implicit val appConfig: AppConfig,
                                       val cc: MessagesControllerComponents) extends FrontendController(cc) with DomicileController

trait DomicileController extends FrontendController with I18nSupport {
  implicit val appConfig: AppConfig
  val domicileView: domicile
  def onPageLoad = Action.async {
    implicit request => {
      Future.successful(Ok(domicileView(domicileForm)))
    }
  }

  def onSubmit = Action.async {
    implicit request => {

      val boundForm = domicileForm.bindFromRequest

      boundForm.fold(
        formWithErrors => Future.successful(BadRequest(domicileView(formWithErrors))),
        choice => choice.getOrElse("") match {
          case Constants.englandOrWales =>
            Future.successful(Redirect(iht.controllers.filter.routes.DeceasedBefore2022Controller.onPageLoad))
          case Constants.scotland =>
            Future.successful(Redirect(iht.controllers.filter.routes.TransitionController.onPageLoadScotland))
          case Constants.northernIreland =>
            Future.successful(Redirect(iht.controllers.filter.routes.TransitionController.onPageLoadNorthernIreland))
          case Constants.otherCountry =>
            Future.successful(Redirect(iht.controllers.filter.routes.TransitionController.onPageLoadOtherCountry))
        }
      )
    }
  }
}
