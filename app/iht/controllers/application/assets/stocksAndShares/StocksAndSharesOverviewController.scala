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

package iht.controllers.application.assets.stocksAndShares

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationController
import iht.models.application.ApplicationDetails
import iht.models.application.assets.StockAndShare
import iht.utils.{CommonHelper, StringHelper}
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.asset.stocksAndShares.stocks_and_shares_overview

class StocksAndSharesOverviewControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                      val cachingConnector: CachingConnector,
                                                      val authConnector: AuthConnector,
                                                      val stocksAndSharesOverviewView: stocks_and_shares_overview,
                                                      implicit val appConfig: AppConfig,
                                                      val cc: MessagesControllerComponents) extends FrontendController(cc) with StocksAndSharesOverviewController

trait StocksAndSharesOverviewController extends ApplicationController with StringHelper {


  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector
  val stocksAndSharesOverviewView: stocks_and_shares_overview

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withRegistrationDetails { registrationDetails =>
        for {
          applicationDetails: Option[ApplicationDetails] <- ihtConnector.getApplication(
            getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
            registrationDetails.acknowledgmentReference
          )
          stocksAndShares: Option[StockAndShare] = applicationDetails.flatMap(_.allAssets.flatMap(_.stockAndShare))
        } yield {
          Ok(stocksAndSharesOverviewView(stocksAndShares, registrationDetails))
        }
      }
    }
  }
}
