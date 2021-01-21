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

package iht.controllers.application.assets.trusts

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.CommonHelper
import iht.views.html.application.asset.trusts.trusts_owned_question
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class TrustsOwnedQuestionControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                  val cachingConnector: CachingConnector,
                                                  val authConnector: AuthConnector,
                                                  val formPartialRetriever: FormPartialRetriever,
                                                  implicit val appConfig: AppConfig,
                                                  val cc: MessagesControllerComponents) extends FrontendController(cc) with TrustsOwnedQuestionController {

}

trait TrustsOwnedQuestionController extends EstateController {


  lazy val submitUrl = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.assets.trusts.routes.TrustsOverviewController.onPageLoad(), Some(appConfig.AssetsTrustsBenefitedID))

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[HeldInTrust](trustsOwnedQuestionForm, trusts_owned_question.apply, _.allAssets.flatMap(_.heldInTrust), userNino)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], HeldInTrust) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, heldInTrust) => {

          val existingValue = appDetails.allAssets.flatMap(_.heldInTrust.flatMap(_.value))
          val existingMoreThanOne = appDetails.allAssets.flatMap(_.heldInTrust.flatMap(_.isMoreThanOne))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, heldInTrust = Some(heldInTrust)))
          (heldInTrust.isOwned match {
            case Some(true) => _.copy(heldInTrust = Some(heldInTrust.copy(value = existingValue,
              isMoreThanOne = existingMoreThanOne)))
            case Some(false) => _.copy(heldInTrust = Some(heldInTrust))
            case None => throw new RuntimeException
          }
          )))
          (updatedAD, None)
        }

      estateElementOnSubmitConditionalRedirect[HeldInTrust](
        trustsOwnedQuestionForm,
        trusts_owned_question.apply,
        updateApplicationDetails,
        (ad, _) => ad.allAssets.flatMap(allAssets => allAssets.heldInTrust).flatMap(_.isOwned) match {
          case Some(true) => submitUrl
          case Some(false) => CommonHelper.addFragmentIdentifier(assetsRedirectLocation, Some(appConfig.AppSectionHeldInTrustID))
          case _ => throw new RuntimeException("Held in trust value does not exist")
        },
        userNino
      )
    }
  }
}
