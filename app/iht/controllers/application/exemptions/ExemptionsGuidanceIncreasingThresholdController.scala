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

package iht.controllers.application.exemptions

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationController
import iht.utils.ExemptionsGuidanceHelper
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.exemption.exemptions_guidance_increasing_threshold

import scala.concurrent.Future

class ExemptionsGuidanceIncreasingThresholdControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                                                    val ihtConnector: IhtConnector,
                                                                    val authConnector: AuthConnector,
                                                                    val exemptionsGuidanceIncreasingThresholdView: exemptions_guidance_increasing_threshold,
                                                                    implicit val appConfig: AppConfig,
                                                                    val cc: MessagesControllerComponents)
  extends FrontendController(cc) with ExemptionsGuidanceIncreasingThresholdController

trait ExemptionsGuidanceIncreasingThresholdController extends ApplicationController with ExemptionsGuidanceHelper {
  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  val exemptionsGuidanceIncreasingThresholdView: exemptions_guidance_increasing_threshold
  def onPageLoad(ihtReference: String): Action[AnyContent] = authorisedForIht {
    implicit request => {
      Future.successful(Ok(exemptionsGuidanceIncreasingThresholdView(ihtReference)))
    }
  }

  def onSubmit(ihtReference: String): Action[AnyContent] = authorisedForIht {
    implicit request => {
      finalDestination(ihtReference, cachingConnector)
        .map(finalDestination => Redirect(finalDestination))
    }
  }
}
