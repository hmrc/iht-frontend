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

package iht.controllers.application.assets.pensions

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.IhtMetrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.CommonHelper
import iht.views.html.application.asset.pensions.pensions_owned_question
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController


class PensionsOwnedQuestionControllerImpl @Inject()(val metrics: IhtMetrics,
                                                    val ihtConnector: IhtConnector,
                                                    val cachingConnector: CachingConnector,
                                                    val authConnector: AuthConnector,
                                                    val pensionsOwnedQuestionView: pensions_owned_question,
                                                    implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with PensionsOwnedQuestionController

trait PensionsOwnedQuestionController extends EstateController {

  val pensionsOwnedQuestionView: pensions_owned_question
  lazy val submitUrl = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.assets.pensions.routes.PensionsOverviewController.onPageLoad, Some(appConfig.AssetsPensionsOwnedID))

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[PrivatePension](pensionsOwnedQuestionForm, pensionsOwnedQuestionView.apply, _.allAssets.flatMap(_.privatePension), userNino)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], PrivatePension) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, privatePension) => {

          val existingValue = appDetails.allAssets.flatMap(_.privatePension.flatMap(_.value))
          val existingChanged = appDetails.allAssets.flatMap(_.privatePension.flatMap(_.isChanged))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, privatePension = Some(privatePension)))
          (privatePension.isOwned match {
            case Some(true) => _.copy(privatePension = Some(privatePension.copy(value = existingValue, isChanged = existingChanged)))
            case Some(false) => _.copy(privatePension = Some(privatePension))
            case None => throw new RuntimeException
          }
          )))
          (updatedAD, None)
        }

      estateElementOnSubmitConditionalRedirect[PrivatePension](
        pensionsOwnedQuestionForm,
        pensionsOwnedQuestionView.apply,
        updateApplicationDetails,
        (ad, _) => ad.allAssets.flatMap(allAssets => allAssets.privatePension).flatMap(_.isOwned) match {
          case Some(true) => submitUrl
          case Some(false) => CommonHelper.addFragmentIdentifier(assetsRedirectLocation, Some(appConfig.AppSectionPrivatePensionID))
          case _ => throw new RuntimeException("Pensions value does not exist")
        },
        userNino
      )
    }
  }
}
