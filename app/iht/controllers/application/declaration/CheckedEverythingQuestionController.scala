/*
 * Copyright 2021 HM Revenue & Customs
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

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms
import iht.forms.ApplicationForms._
import iht.utils.{CommonHelper, LogHelper}
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class CheckedEverythingQuestionControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                        val cachingConnector: CachingConnector,
                                                        val authConnector: AuthConnector,
                                                        val formPartialRetriever: FormPartialRetriever,
                                                        implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with CheckedEverythingQuestionController {

}

trait CheckedEverythingQuestionController extends EstateController {

  def onPageLoad: Action[AnyContent] = authorisedForIht {
    implicit request =>
      withRegistrationDetails { rd =>
        Future.successful(Ok(iht.views.html.application.declaration.checked_everything_question(checkedEverythingQuestionForm, rd)))
      }
  }

  def onSubmit: Action[AnyContent] = authorisedForIht {
    implicit request => {
      val boundForm = ApplicationForms.checkedEverythingQuestionForm.bindFromRequest
      withRegistrationDetails { rd =>
        boundForm.fold(
          formWithErrors => {
            LogHelper.logFormError(formWithErrors)
            Future.successful(BadRequest(iht.views.html.application.declaration.checked_everything_question(formWithErrors, rd)))
          }, optionBoolean => {
            val redirectLocation = if (CommonHelper.getOrException(optionBoolean)) {
              Redirect(iht.controllers.application.declaration.routes.DeclarationController.onPageLoad())
            } else {
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
