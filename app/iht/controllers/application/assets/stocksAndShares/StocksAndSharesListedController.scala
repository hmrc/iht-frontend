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

import iht.controllers.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.ApplicationKickOutHelper
import iht.views.html.application.asset.stocksAndShares.stocks_and_shares_listed
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object StocksAndSharesListedController extends StocksAndSharesListedController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait StocksAndSharesListedController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsStocksAndSharesListed)
  val submitUrl = iht.controllers.application.assets.stocksAndShares.routes.StocksAndSharesOverviewController.onPageLoad()

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnPageLoad[StockAndShare](stockAndShareListedForm, stocks_and_shares_listed.apply,_.allAssets.flatMap(_.stockAndShare))
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], StockAndShare) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, stockAndShare) => {
          val existingValueNotListed = appDetails.allAssets.flatMap(_.stockAndShare.flatMap(_.valueNotListed))
          val existingIsNotListed = appDetails.allAssets.flatMap(_.stockAndShare.flatMap(_.isNotListed))


          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, stockAndShare = Some(stockAndShare)))

          (stockAndShare.isListed match {
            case Some(true) => _.copy(stockAndShare = Some(stockAndShare.copy(valueNotListed = existingValueNotListed,
                                                          isNotListed = existingIsNotListed) ))
            case Some(false) => _.copy(stockAndShare = Some(stockAndShare.copy(valueListed = None,
                                        valueNotListed = existingValueNotListed, isNotListed = existingIsNotListed) ))
            case None => throw new RuntimeException("Not able to retrieve the value of StockAndShareListed question")
          })
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[StockAndShare](
        stockAndShareListedForm,
        stocks_and_shares_listed.apply,
        updateApplicationDetails,
        submitUrl
      )
    }
  }
}
