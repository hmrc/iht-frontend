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

package iht.controllers.application.assets.trusts

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset.trusts.trusts_value
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class TrustsValueControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                          val cachingConnector: CachingConnector,
                                          val authConnector: AuthConnector,
                                          val trustsValueView: trusts_value,
                                          implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with TrustsValueController {

}

trait TrustsValueController extends EstateController {


  lazy val submitUrl = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.assets.trusts.routes.TrustsOverviewController.onPageLoad, Some(appConfig.AssetsTrustsValueID))
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsTrustsValue)
  val trustsValueView: trusts_value

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[HeldInTrust](trustsValueForm, trustsValueView.apply, _.allAssets.flatMap(_.heldInTrust), userNino)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], HeldInTrust) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, heldInTrust) => {

          val existingIsMoreThanOne = appDetails.allAssets.flatMap(_.heldInTrust.flatMap(_.isMoreThanOne))
          val existingIsOwned = appDetails.allAssets.flatMap(_.heldInTrust.flatMap(_.isOwned))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, heldInTrust = Some(heldInTrust)))
          (_.copy(heldInTrust = Some(heldInTrust.copy(isMoreThanOne = existingIsMoreThanOne, isOwned = existingIsOwned))))
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[HeldInTrust](
        trustsValueForm,
        trustsValueView.apply,
        updateApplicationDetails,
        submitUrl,
        userNino
      )
    }
  }
}
