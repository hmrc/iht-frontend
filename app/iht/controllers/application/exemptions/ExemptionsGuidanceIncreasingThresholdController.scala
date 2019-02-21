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

package iht.controllers.application.exemptions

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationController
import iht.utils.ExemptionsGuidanceHelper
import javax.inject.Inject
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

/**
 * Created by jon on 21/07/15.
 */
class ExemptionsGuidanceIncreasingThresholdControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                                                    val ihtConnector: IhtConnector,
                                                                    val authConnector: AuthConnector,
                                                                    override implicit val formPartialRetriever: FormPartialRetriever) extends ExemptionsGuidanceIncreasingThresholdController

trait ExemptionsGuidanceIncreasingThresholdController extends ApplicationController {


  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad(ihtReference: String) = authorisedForIht {
    implicit request => {
      Future.successful(Ok(iht.views.html.application.exemption.exemptions_guidance_increasing_threshold(ihtReference)))
    }
  }

  def onSubmit(ihtReference: String) = authorisedForIht {
    implicit request => {
      ExemptionsGuidanceHelper.finalDestination(ihtReference, cachingConnector)
        .map( finalDestination => Redirect(finalDestination))
    }
  }
}
