/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.BasicEstateElement
import iht.utils.ApplicationKickOutHelper
import iht.views.html.application.asset._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.constants.Constants._
import iht.constants.IhtProperties._
import iht.utils.CommonHelper

object MoneyOwedController extends MoneyOwedController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait MoneyOwedController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsMoneyOwed)

  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[BasicEstateElement](moneyOwedForm, money_owed.apply, _.allAssets.flatMap(_.moneyOwed))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], BasicEstateElement) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, moneyOwed) => {
          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
            (new AllAssets(action = None, moneyOwed = Some(moneyOwed)))

          (moneyOwed.isOwned match {
            case Some(true) => _.copy(moneyOwed = Some(moneyOwed))
            case Some(false) => _.copy(moneyOwed = Some(moneyOwed.copy(value = None)))
            case None => throw new RuntimeException("Not able to retrieve the value of MoneyOwed question")
          })
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[BasicEstateElement](moneyOwedForm,
        money_owed.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(assetsRedirectLocation, Some(AppSectionMoneyOwedID))
      )
    }
  }
}
