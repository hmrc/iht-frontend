/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.{FrontendController, UnauthorisedAction}
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class AgentControllerImpl @Inject()(val messagesApi: MessagesApi,
                                    val formPartialRetriever: FormPartialRetriever) extends AgentController

trait AgentController extends FrontendController with I18nSupport {
  implicit val formPartialRetriever: FormPartialRetriever

  def onPageLoad: Action[AnyContent] = UnauthorisedAction.async {
    implicit request => {
      Future.successful(Ok(iht.views.html.filter.agent_view()))
    }
  }
}
