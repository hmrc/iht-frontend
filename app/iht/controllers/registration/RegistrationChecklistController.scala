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

package iht.controllers.registration

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class RegistrationChecklistControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                                    val ihtConnector: IhtConnector,
                                                    val formPartialRetriever: FormPartialRetriever,
                                                    val cc: MessagesControllerComponents,
                                                    implicit val appConfig: AppConfig)
  extends FrontendController(cc) with RegistrationChecklistController

trait RegistrationChecklistController extends FrontendController with I18nSupport {
  implicit val appConfig: AppConfig

  def cachingConnector : CachingConnector
  def ihtConnector : IhtConnector
  implicit val formPartialRetriever: FormPartialRetriever

  def onPageLoad: Action[AnyContent] = Action.async {
    implicit request => {
      Future.successful(Ok(iht.views.html.registration.registration_checklist()))
    }
  }
}
