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

package iht.controllers

import iht.config.IhtFormPartialRetriever
import iht.connector.CachingConnector
import iht.views.html.{deadlines_application, deadlines_registration}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.controller.UnauthorisedAction.async
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class DeadlinesControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                        val formPartialRetriever: IhtFormPartialRetriever,
                                        val messagesApi: MessagesApi) extends DeadlinesController

trait DeadlinesController extends FrontendController with I18nSupport {
  implicit val formPartialRetriever: FormPartialRetriever
  def cachingConnector: CachingConnector

  def onPageLoadRegistration: Action[AnyContent] = async { implicit request =>
    Future.successful(Ok(deadlines_registration.apply))
  }

  def onPageLoadApplication: Action[AnyContent] = async { implicit request =>
    Future.successful(Ok(deadlines_application.apply))
  }
}
