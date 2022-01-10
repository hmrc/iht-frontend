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

package iht.controllers.application.declaration


import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.Constants
import iht.controllers.application.ApplicationController
import iht.utils.CommonHelper
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import iht.views.html.application.declaration.declaration_received

import scala.concurrent.Future

class DeclarationReceivedControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                                  val ihtConnector: IhtConnector,
                                                  val authConnector: AuthConnector,
                                                  val declarationReceivedView: declaration_received,
                                                  implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with DeclarationReceivedController

trait DeclarationReceivedController extends ApplicationController {
  def cachingConnector: CachingConnector
  val declarationReceivedView: declaration_received
  def onPageLoad: Action[AnyContent] = authorisedForIht {
    implicit request => {
      withRegistrationDetails { rd =>
        val ihtReference = CommonHelper.getOrException(rd.ihtReference)
        cachingConnector.storeSingleValue(Constants.PDFIHTReference, ihtReference).flatMap { _ =>
          Future.successful(Ok(declarationReceivedView(rd)))
        }
      }
    }
  }
}
