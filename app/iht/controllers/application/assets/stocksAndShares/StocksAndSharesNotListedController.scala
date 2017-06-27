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

import iht.constants.IhtProperties._
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset.stocksAndShares.stocks_and_shares_not_listed
import play.api.i18n.MessagesApi

@Singleton
class StocksAndSharesNotListedController @Inject()(val messagesApi: MessagesApi) extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsStocksAndSharesNotListed)
  val submitUrl = CommonHelper.addFragmentIdentifier(iht.controllers.application.assets.stocksAndShares.routes.StocksAndSharesOverviewController.onPageLoad(), Some(AssetsStocksNotListedID))

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnPageLoad[StockAndShare](stockAndShareNotListedForm, stocks_and_shares_not_listed.apply,_.allAssets.flatMap(_.stockAndShare))
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], StockAndShare) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, stockAndShare) => {
          val existingValueListed = appDetails.allAssets.flatMap(_.stockAndShare.flatMap(_.valueListed))
          val existingIsListed = appDetails.allAssets.flatMap(_.stockAndShare.flatMap(_.isListed))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, stockAndShare = Some(stockAndShare)))
          (stockAndShare.isNotListed match {
            case Some(true) => _.copy(stockAndShare = Some(stockAndShare.copy(valueListed = existingValueListed,
                                                          isListed = existingIsListed) ))
            case Some(false) => _.copy(stockAndShare = Some(stockAndShare.copy(valueNotListed = None,
                                                        valueListed = existingValueListed, isListed = existingIsListed) ))
            case None => throw new RuntimeException("Not able to retrieve the value of StockAndShareNotListed question")
          })
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[StockAndShare](
        stockAndShareNotListedForm,
        stocks_and_shares_not_listed.apply,
        updateApplicationDetails,
        submitUrl
      )
    }
  }
}
