/*
 * Copyright 2018 HM Revenue & Customs
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
import uk.gov.hmrc.play.frontend.controller.{UnauthorisedAction, FrontendController}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

object DomicileController extends DomicileController {

}

trait DomicileController extends FrontendController {

  implicit val formPartialRetriever: FormPartialRetriever = IhtFormPartialRetriever

  def onPageLoad = UnauthorisedAction.async {
    implicit request => {
      Future.successful(Ok(iht.views.html.filter.domicile(domicileForm)))
    }
  }

  def onSubmit = UnauthorisedAction.async {
    implicit request => {

      val boundForm = domicileForm.bindFromRequest

      boundForm.fold(
        formWithErrors => Future.successful(BadRequest(iht.views.html.filter.domicile(formWithErrors))),
        choice => choice.getOrElse("") match {
          case Constants.englandOrWales =>
            Future.successful(Redirect(iht.controllers.filter.routes.FilterJointlyOwnedController.onPageLoad()))
          case Constants.scotland =>
            Future.successful(Redirect(iht.controllers.filter.routes.TransitionController.onPageLoadScotland()))
          case Constants.northernIreland =>
            Future.successful(Redirect(iht.controllers.filter.routes.TransitionController.onPageLoadNorthernIreland()))
          case Constants.otherCountry =>
            Future.successful(Redirect(iht.controllers.filter.routes.TransitionController.onPageLoadOtherCountry()))
        }
      )
    }
  }
}
