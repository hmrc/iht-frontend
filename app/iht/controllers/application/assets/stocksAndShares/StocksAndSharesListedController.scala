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

package iht.controllers.application.assets.stocksAndShares

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset.stocksAndShares.stocks_and_shares_listed
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class StocksAndSharesListedControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                    val cachingConnector: CachingConnector,
                                                    val authConnector: AuthConnector,
                                                    val stocksAndSharesListedView: stocks_and_shares_listed,
                                                    implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with StocksAndSharesListedController {

}

trait StocksAndSharesListedController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsStocksAndSharesListed)

  lazy val submitUrl = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.assets.stocksAndShares.routes.StocksAndSharesOverviewController.onPageLoad,
    Some(appConfig.AssetsStocksListedID))

  val stocksAndSharesListedView: stocks_and_shares_listed

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      estateElementOnPageLoad[StockAndShare](stockAndShareListedForm, stocksAndSharesListedView.apply, _.allAssets.flatMap(_.stockAndShare), userNino)
    }
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], StockAndShare) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, stockAndShare) => {
          val existingValueNotListed = appDetails.allAssets.flatMap(_.stockAndShare.flatMap(_.valueNotListed))
          val existingIsNotListed = appDetails.allAssets.flatMap(_.stockAndShare.flatMap(_.isNotListed))


          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, stockAndShare = Some(stockAndShare)))

          (stockAndShare.isListed match {
            case Some(true) => _.copy(stockAndShare = Some(stockAndShare.copy(valueNotListed = existingValueNotListed,
              isNotListed = existingIsNotListed)))
            case Some(false) => _.copy(stockAndShare = Some(stockAndShare.copy(valueListed = None,
              valueNotListed = existingValueNotListed, isNotListed = existingIsNotListed)))
            case None => throw new RuntimeException("Not able to retrieve the value of StockAndShareListed question")
          })
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[StockAndShare](
        stockAndShareListedForm,
        stocksAndSharesListedView.apply,
        updateApplicationDetails,
        submitUrl,
        userNino
      )
    }
  }
}
