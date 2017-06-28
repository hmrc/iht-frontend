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

package iht.controllers.application.assets.trusts

import javax.inject.{Inject, Singleton}

import iht.constants.IhtProperties
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.CommonHelper
import iht.views.html.application.asset.trusts.trusts_owned_question
import play.api.i18n.MessagesApi


@Singleton
class TrustsOwnedQuestionController @Inject()(
                                               val messagesApi: MessagesApi,
                                               val ihtProperties: IhtProperties,
                                               val applicationForms: ApplicationForms) extends EstateController {
  val submitUrl = CommonHelper
    .addFragmentIdentifier(iht.controllers.application.assets.trusts.routes.TrustsOverviewController.onPageLoad(), Some(ihtProperties.AssetsTrustsBenefitedID))

  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[HeldInTrust](applicationForms.trustsOwnedQuestionForm, trusts_owned_question.apply, _.allAssets.flatMap(_.heldInTrust))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], HeldInTrust) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, heldInTrust) => {

          val existingValue = appDetails.allAssets.flatMap(_.heldInTrust.flatMap(_.value))
          val existingMoreThanOne = appDetails.allAssets.flatMap(_.heldInTrust.flatMap(_.isMoreThanOne))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
            (new AllAssets(action = None, heldInTrust = Some(heldInTrust)))
           (heldInTrust.isOwned match {
            case Some(true) => _.copy(heldInTrust = Some(heldInTrust.copy(value = existingValue,
                                                          isMoreThanOne = existingMoreThanOne) ))
            case Some(false) => _.copy(heldInTrust = Some(heldInTrust))
            case None => throw new RuntimeException     }
           )))
          (updatedAD, None)
        }

      estateElementOnSubmitConditionalRedirect[HeldInTrust](
        applicationForms.trustsOwnedQuestionForm,
        trusts_owned_question.apply,
        updateApplicationDetails,
        (ad, _) =>  ad.allAssets.flatMap(allAssets=>allAssets.heldInTrust).flatMap(_.isOwned) match {
          case Some(true) => submitUrl
          case Some(false) => CommonHelper.addFragmentIdentifier(assetsRedirectLocation, Some(ihtProperties.AppSectionHeldInTrustID))
          case _ => throw new RuntimeException("Held in trust value does not exist")
        }
      )
    }
  }
}
