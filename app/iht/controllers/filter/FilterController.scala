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

import iht.config.FrontendAuthConnector
import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.Constants
import iht.controllers.auth.CustomPasscodeAuthentication
import iht.forms.FilterForms._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

/**
  * Created by adwelly on 21/10/2016.
  * Modelled closely on the PrivateBetaLandingPageController
  */

object FilterController extends FilterController {
  val cachingConnector = CachingConnector
  val ihtConnector = IhtConnector
  val authConnector: AuthConnector = FrontendAuthConnector
}

trait FilterController extends FrontendController with CustomPasscodeAuthentication {

  def cachingConnector: CachingConnector
  def ihtConnector: IhtConnector

  def onPageLoad = customAuthenticatedActionAsync {
    implicit request => {
      Future.successful(Ok(iht.views.html.filter.filter_view(filterForm)))
    }
  }

  def onSubmit = customAuthenticatedActionAsync {
    implicit request => {
      val boundForm = filterForm.bindFromRequest()

      boundForm.fold(
        formWithErrors => {
          Future.successful(BadRequest(iht.views.html.filter.filter_view(formWithErrors)))
        }, {
          choice => {
            choice.getOrElse("") match {
              case Constants.continueEstateReport =>
                Future.successful(Redirect(iht.controllers.home.routes.IhtHomeController.onPageLoad()))
              case Constants.alreadyStarted =>
                Future.successful(Redirect(iht.controllers.registration.routes.RegistrationChecklistController.onPageLoad()))
              case Constants.agent =>
                Future.successful(Redirect(iht.controllers.filter.routes.AgentController.onPageLoad()))
              case Constants.register =>
                Future.successful(Redirect(iht.controllers.filter.routes.DomicileController.onPageLoad()))
            }
          }
        }
      )
    }
  }
}
