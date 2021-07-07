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

package iht.controllers.application.assets.pensions

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.IhtMetrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset.pensions.pensions_value
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController


class PensionsValueControllerImpl @Inject()(val metrics: IhtMetrics,
                                            val ihtConnector: IhtConnector,
                                            val cachingConnector: CachingConnector,
                                            val authConnector: AuthConnector,
                                            val pensionsValueView: pensions_value,
                                            implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with PensionsValueController

trait PensionsValueController extends EstateController {

  val pensionsValueView: pensions_value

  lazy val submitUrl = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.assets.pensions.routes.PensionsOverviewController.onPageLoad(), Some(appConfig.AssetsPensionsValueID))
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsPensionsValue)

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[PrivatePension](pensionsValueForm, pensionsValueView.apply, _.allAssets.flatMap(_.privatePension), userNino)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], PrivatePension) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, privatePension) => {

          val existingIsChanged = appDetails.allAssets.flatMap(_.privatePension.flatMap(_.isChanged))
          val existingIsOwned = appDetails.allAssets.flatMap(_.privatePension.flatMap(_.isOwned))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, privatePension = Some(privatePension)))
          (_.copy(privatePension = Some(privatePension.copy(isChanged = existingIsChanged, isOwned = existingIsOwned))))
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[PrivatePension](
        pensionsValueForm,
        pensionsValueView.apply,
        updateApplicationDetails,
        submitUrl,
        userNino
      )
    }
  }
}
