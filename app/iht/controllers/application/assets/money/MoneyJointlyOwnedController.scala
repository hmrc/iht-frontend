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

package iht.controllers.application.assets.money

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.IhtMetrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset.money.money_jointly_owned
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever


class MoneyJointlyOwnedControllerImpl @Inject()(val metrics: IhtMetrics,
                                                val ihtConnector: IhtConnector,
                                                val cachingConnector: CachingConnector,
                                                val authConnector: AuthConnector,
                                                val formPartialRetriever: FormPartialRetriever,
                                                implicit val appConfig: AppConfig,
                                                val cc: MessagesControllerComponents) extends FrontendController(cc) with MoneyJointlyOwnedController {

}

trait MoneyJointlyOwnedController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsMoneyJointlyOwned)


  lazy val submitUrl = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.assets.money.routes.MoneyOverviewController.onPageLoad(), Some(appConfig.AssetsMoneySharedID))

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[ShareableBasicEstateElement](moneyJointlyOwnedForm, money_jointly_owned.apply, _.allAssets.flatMap(_.money), userNino)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], ShareableBasicEstateElement) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, money) => {
          val existingValue = appDetails.allAssets.flatMap(_.money.flatMap(_.value))
          val existingIsOwned = appDetails.allAssets.flatMap(_.money.flatMap(_.isOwned))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, money = Some(money)))
          (
            money.isOwnedShare match {
              case Some(true) => _.copy(money = Some(money.copy(value = existingValue, isOwned = existingIsOwned)))
              case Some(false) => _.copy(money = Some(money.copy(shareValue = None, value = existingValue,
                isOwned = existingIsOwned)))
              case None => throw new RuntimeException
            }

          )))
          (updatedAD, None)
        }

      estateElementOnSubmit[ShareableBasicEstateElement](
        moneyJointlyOwnedForm,
        money_jointly_owned.apply,
        updateApplicationDetails,
        submitUrl,
        userNino
      )
    }
  }
}
