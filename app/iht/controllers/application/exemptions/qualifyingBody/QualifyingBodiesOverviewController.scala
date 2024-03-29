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

package iht.controllers.application.exemptions.qualifyingBody

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationController
import iht.utils.CommonHelper
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.exemption.qualifyingBody.qualifying_bodies_overview
import scala.concurrent.Future

class QualifyingBodiesOverviewControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                                       val ihtConnector: IhtConnector,
                                                       val authConnector: AuthConnector,
                                                       val qualifyingBodiesOverviewView: qualifying_bodies_overview,
                                                       implicit val appConfig: AppConfig,
                                                       val cc: MessagesControllerComponents)
  extends FrontendController(cc) with QualifyingBodiesOverviewController

trait QualifyingBodiesOverviewController extends ApplicationController {

  val qualifyingBodiesOverviewView: qualifying_bodies_overview
  def onPageLoad() = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withApplicationDetails(userNino) {
        rd => ad => {

          val isAssetLeftToQualifyingBody = ad.allExemptions.flatMap(_.qualifyingBody).flatMap(_.isSelected)

          Future.successful(Ok(qualifyingBodiesOverviewView(ad.qualifyingBodies,
            rd,
            CommonHelper.getOrException(isAssetLeftToQualifyingBody, "Illegal page navigation"))))
        }
      }
    }
  }
}
