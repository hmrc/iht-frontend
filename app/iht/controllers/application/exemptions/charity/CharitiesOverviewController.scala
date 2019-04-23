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

package iht.controllers.application.exemptions.charity

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationController
import iht.utils.CommonHelper
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class CharitiesOverviewControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                                val ihtConnector: IhtConnector,
                                                val authConnector: AuthConnector,
                                                override implicit val formPartialRetriever: FormPartialRetriever,
implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with CharitiesOverviewController

trait CharitiesOverviewController extends ApplicationController {


  def cachingConnector: CachingConnector
  def ihtConnector: IhtConnector

  def onPageLoad() = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withApplicationDetails(userNino) {
        rd => ad => {

          val isAssetLeftToCharity: Option[Boolean] = ad.allExemptions.flatMap(_.charity).flatMap(_.isSelected)

          Future.successful(Ok(iht.views.html.application.exemption.charity.charities_overview(ad.charities,
            rd,
            CommonHelper.getOrException(isAssetLeftToCharity))))
        }
      }
    }
  }
}
