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

package iht.controllers.application.assets

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.IhtConnectors
import iht.controllers.application.ApplicationController
import iht.models.application.assets.AllAssets
import iht.utils.CommonHelper
import iht.utils.ExemptionsGuidanceHelper._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.Future

/**
  *
  * Created by Vineet Tyagi on 07/12/15.
  *
  */

object AssetsOverviewController extends AssetsOverviewController with IhtConnectors

trait AssetsOverviewController extends ApplicationController {

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      withApplicationDetails { rd => ad =>
        lazy val ihtRef = CommonHelper.getOrExceptionNoIHTRef(rd.ihtReference)
        guidanceRedirect(routes.AssetsOverviewController.onPageLoad(), ad, cachingConnector).map {
          case Some(call) => Redirect(call)
          case None => {
            val allAssets = ad.allAssets.fold(new AllAssets())(assets => assets)
            Ok(iht.views.html.application.asset.assets_overview(
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
