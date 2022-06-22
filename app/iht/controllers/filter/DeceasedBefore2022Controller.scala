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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.filter.deceased_before_2022
import play.api.i18n.I18nSupport
import iht.forms.FilterForms.deceasedBefore2022Form

import scala.concurrent.Future
class DeceasedBefore2022ControllerImpl @Inject()(
                                                val cc: MessagesControllerComponents,
                                                val deceasedBefore2022View: deceased_before_2022,
                                                implicit val appConfig: AppConfig
                                                ) extends FrontendController(cc) with DeceasedBefore2022Controller {

}

trait DeceasedBefore2022Controller extends FrontendController with I18nSupport {
  implicit val appConfig: AppConfig
  val deceasedBefore2022View: deceased_before_2022

  def onPageLoad: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(deceasedBefore2022View(deceasedBefore2022Form)))
  }

  def onSubmit(): Action[AnyContent] = Action.async {
    implicit request => {

    }
    val boundForm = deceasedBefore2022Form.bindFromRequest
    boundForm.fold(
      formWithErrors => Future.successful(BadRequest(deceasedBefore2022View(formWithErrors))),
      {
        case Some(true) => Future.successful(Redirect(iht.controllers.filter.routes.FilterJointlyOwnedController.onPageLoad))
        case _ =>          Future.successful(Redirect(iht.controllers.filter.routes.UseCheckerController.onPageLoad))
      }
    )
  }
}