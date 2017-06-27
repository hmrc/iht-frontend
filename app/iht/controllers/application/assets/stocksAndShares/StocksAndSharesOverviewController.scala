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

package iht.controllers.application.assets.stocksAndShares

import javax.inject.{Inject, Singleton}
import iht.controllers.application.EstateController
import iht.models.application.ApplicationDetails
import iht.models.application.assets.StockAndShare
import iht.utils.CommonHelper
import play.api.i18n.MessagesApi

@Singleton
class StocksAndSharesOverviewController @Inject()(val messagesApi: MessagesApi) extends EstateController {

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      withRegistrationDetails { registrationDetails =>

        for {

          applicationDetails: Option[ApplicationDetails] <- ihtConnector.getApplication(
            CommonHelper.getNino(user),
            CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
            registrationDetails.acknowledgmentReference
          )
          stocksAndShares: Option[StockAndShare] = applicationDetails.flatMap(_.allAssets.flatMap(_.stockAndShare))
        } yield {
          Ok(iht.views.html.application.asset.stocksAndShares.stocks_and_shares_overview(stocksAndShares, registrationDetails))
        }
      }
    }
  }
}
