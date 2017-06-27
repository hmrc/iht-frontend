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

package iht.controllers.application.exemptions

import javax.inject.{Inject, Singleton}

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationController
import play.api.i18n.MessagesApi

import scala.concurrent.Future

@Singleton
class ExemptionsGuidanceController @Inject()(val messagesApi: MessagesApi) extends ApplicationController {
  def onPageLoad(ihtReference: String) = authorisedForIht {
    implicit user => implicit request => {
      Future.successful(Ok(iht.views.html.application.exemption.exemptions_guidance(ihtReference)))
    }
  }

  def onSubmit(ihtReference: String) = authorisedForIht {
    implicit user => implicit request => {
      Future.successful(Redirect(iht.controllers.application.routes.EstateOverviewController
        .onPageLoadWithIhtRef(ihtReference)))
    }
  }
}
