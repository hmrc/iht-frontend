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

package iht.controllers.application.declaration

import iht.controllers.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.utils.{CommonHelper, LogHelper}

import scala.concurrent.Future

object CheckedEverythingQuestionController extends CheckedEverythingQuestionController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait CheckedEverythingQuestionController extends EstateController {

  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      withRegistrationDetails { rd =>
        Future.successful(Ok(iht.views.html.application.declaration.checked_everything_question(checkedEverythingQuestionForm, rd)))
      }
  }


  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val boundForm = ApplicationForms.checkedEverythingQuestionForm.bindFromRequest
      withRegistrationDetails { rd =>
        boundForm.fold(
          formWithErrors => {
            LogHelper.logFormError(formWithErrors)
            Future.successful(BadRequest(iht.views.html.application.declaration.checked_everything_question(formWithErrors, rd)))
          }, optionBoolean => {
            val redirectLocation = CommonHelper.getOrException(optionBoolean) match {
              case true =>
                Redirect(iht.controllers.application.declaration.routes.DeclarationController.onPageLoad())
              case _ =>
                Redirect(iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(
                  CommonHelper.getOrExceptionNoIHTRef(rd.ihtReference)))
            }
            Future.successful(redirectLocation)
          }
        )
      }
    }
  }
}
