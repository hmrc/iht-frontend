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

package iht.controllers.application.assets

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.IhtMetrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.BasicEstateElement
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset._
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class MoneyOwedControllerImpl @Inject()(val metrics: IhtMetrics,
                                        val ihtConnector: IhtConnector,
                                        val cachingConnector: CachingConnector,
                                        val authConnector: AuthConnector,
                                        val moneyOwedView: money_owed,
                                        implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with MoneyOwedController {
}

trait MoneyOwedController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsMoneyOwed)

  val moneyOwedView: money_owed
  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[BasicEstateElement](moneyOwedForm, moneyOwedView.apply, _.allAssets.flatMap(_.moneyOwed), userNino)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
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
        moneyOwedView.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(assetsRedirectLocation, Some(appConfig.AppSectionMoneyOwedID)),
        userNino
      )
    }
  }
}
