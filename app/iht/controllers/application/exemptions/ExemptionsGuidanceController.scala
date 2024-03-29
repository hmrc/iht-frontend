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

package iht.controllers.application.exemptions

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationController
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.exemption.exemptions_guidance

import scala.concurrent.Future

/**
 * Created by jon on 21/07/15.
 */
class ExemptionsGuidanceControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                                 val ihtConnector: IhtConnector,
                                                 val authConnector: AuthConnector,
                                                 val exemptionsGuidanceView: exemptions_guidance,
                                                 implicit val appConfig: AppConfig,
                                                 val cc: MessagesControllerComponents) extends FrontendController(cc) with ExemptionsGuidanceController

trait ExemptionsGuidanceController extends ApplicationController {


  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector
  val exemptionsGuidanceView: exemptions_guidance

  def onPageLoad(ihtReference: String) = authorisedForIht {
    implicit request => {
      Future.successful(Ok(exemptionsGuidanceView(ihtReference)))
    }
  }

  def onSubmit(ihtReference: String) = authorisedForIht {
    implicit request => { // False positive warning. Workaround: scala/bug#11175 -Ywarn-unused:params false positive
      Future.successful(Redirect(iht.controllers.application.routes.EstateOverviewController
        .onPageLoadWithIhtRef(ihtReference)))
    }
  }
}
