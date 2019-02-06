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

import iht.config.{AppConfig, FrontendAuthConnector}
import iht.connector.{CachingConnector, IhtConnector}
import iht.connector.IhtConnectors
import iht.controllers.application.ApplicationController
import iht.utils.CommonHelper
import javax.inject.Inject
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.PlayAuthConnector

import scala.concurrent.Future
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}

/**
  * Created by vineet on 11/08/16.
  */

class CharitiesOverviewControllerImpl @Inject()() extends CharitiesOverviewController with IhtConnectors

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
