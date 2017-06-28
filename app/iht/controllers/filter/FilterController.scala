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

import javax.inject.{Inject, Singleton}

import iht.constants.Constants
import iht.controllers.auth.CustomPasscodeAuthentication
import iht.forms.FilterForms
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

/**
  * Created by adwelly on 21/10/2016.
  * Modelled closely on the PrivateBetaLandingPageController
  */

@Singleton
class FilterController @Inject() (val filterForms: FilterForms)extends FrontendController with CustomPasscodeAuthentication {

  def onPageLoad = customAuthenticatedActionAsync {
    implicit request => {
      Future.successful(Ok(iht.views.html.filter.filter_view(filterForms.filterForm)))
    }
  }

  def onSubmit = customAuthenticatedActionAsync {
    implicit request => {
      val boundForm = filterForms.filterForm.bindFromRequest()

      boundForm.fold(
        formWithErrors => {
          Future.successful(BadRequest(iht.views.html.filter.filter_view(formWithErrors)))
        }, {
          choice => {
            choice.getOrElse("") match {
              case constants.continueEstateReport =>
                Future.successful(Redirect(iht.controllers.home.routes.IhtHomeController.onPageLoad()))
              case constants.alreadyStarted =>
                Future.successful(Redirect(iht.controllers.registration.routes.RegistrationChecklistController.onPageLoad()))
              case constants.agent =>
                Future.successful(Redirect(iht.controllers.filter.routes.AgentController.onPageLoad()))
              case constants.register =>
                Future.successful(Redirect(iht.controllers.filter.routes.DomicileController.onPageLoad()))
            }
          }
        }
      )
    }
  }
}
