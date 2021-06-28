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

package iht.controllers.application.assets

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationController
import iht.metrics.IhtMetrics
import iht.models.application.assets.AllAssets
import iht.utils.{CommonHelper, ExemptionsGuidanceHelper}
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.asset.assets_overview

class AssetsOverviewControllerImpl @Inject()(val metrics: IhtMetrics,
                                             val ihtConnector: IhtConnector,
                                             val cachingConnector: CachingConnector,
                                             val authConnector: AuthConnector,
                                             val assetsOverviewView: assets_overview,
                                             implicit val appConfig: AppConfig,
                                             val cc: MessagesControllerComponents) extends FrontendController(cc) with AssetsOverviewController

trait AssetsOverviewController extends ApplicationController with ExemptionsGuidanceHelper {

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  val assetsOverviewView: assets_overview
  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withApplicationDetails(userNino) { rd =>
        ad =>
          lazy val ihtRef = CommonHelper.getOrExceptionNoIHTRef(rd.ihtReference)
          guidanceRedirect(routes.AssetsOverviewController.onPageLoad(), ad, cachingConnector).map {
            case Some(call) => Redirect(call)
            case None => {
              val allAssets = ad.allAssets.fold(new AllAssets())(assets => assets)
              Ok(assetsOverviewView(
                ad,
                allAssets,
                ihtRef,
                CommonHelper.getOrException(rd.deceasedDetails).name))
            }
          }
      }
    }
  }
}
