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

package iht.controllers.registration

import iht.config.AppConfig
import iht.connector.CachingConnector
import iht.views.html.registration.registration_error_manual_correspondence_indicator
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class ManualCorrespondenceIndicatorController @Inject()(registrationMCIView: registration_error_manual_correspondence_indicator,
                                                        val cc: MessagesControllerComponents,
                                                        val appConfig: AppConfig,
                                                        val authConnector: AuthConnector,
                                                        val cachingConnector: CachingConnector,
                                                       ) extends FrontendController(cc) with  RegistrationController {

  override def guardConditions: Set[Predicate] = Set.empty

  def onPageLoad = authorisedForIht { implicit request =>
    Future.successful(Ok(registrationMCIView()))
  }

}
