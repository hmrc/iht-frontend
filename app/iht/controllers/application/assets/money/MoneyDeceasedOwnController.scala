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

package iht.controllers.application.assets.money

import iht.controllers.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.utils.ApplicationKickOutHelper
import iht.views.html.application.asset.money.money_deceased_own

object MoneyDeceasedOwnController extends MoneyDeceasedOwnController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait MoneyDeceasedOwnController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsMoneyDeceasedOwned)
  val submitUrl = iht.controllers.application.assets.money.routes.MoneyOverviewController.onPageLoad()

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnPageLoad[ShareableBasicEstateElement](moneyFormOwn, money_deceased_own.apply,_.allAssets.flatMap(_.money))
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], ShareableBasicEstateElement) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, money) => {
          val existingShareValue = appDetails.allAssets.flatMap(_.money.flatMap(_.shareValue))
          val existingIsOwnedShare = appDetails.allAssets.flatMap(_.money.flatMap(_.isOwnedShare))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, money = Some(money)))
            (money.isOwned match {
              case Some(true) => _.copy(money = Some(money.copy(shareValue = existingShareValue,
                                                    isOwnedShare = existingIsOwnedShare) ))
              case Some(false) => _.copy(money = Some(money.copy(value = None, shareValue = existingShareValue,
                                                    isOwnedShare = existingIsOwnedShare)))
              case None => throw new RuntimeException("Not able to retrieve the value of money owed question")
            }
          )))
          (updatedAD, None)
        }

      estateElementOnSubmit[ShareableBasicEstateElement](
        moneyFormOwn,
        money_deceased_own.apply,
        updateApplicationDetails,
        submitUrl
      )
    }
  }
}
