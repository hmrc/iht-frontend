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

package iht.controllers.application.assets

import javax.inject.{Inject, Singleton}

import iht.constants.IhtProperties
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.BasicEstateElement
import iht.utils.{ApplicationKickOutHelper, CommonHelper, ApplicationStatus => AppStatus}
import iht.views.html.application.asset._
import play.api.i18n.MessagesApi

@Singleton
class NominatedController @Inject()(
                                     val messagesApi: MessagesApi,
                                     val ihtProperties: IhtProperties,
                                     val applicationForms: ApplicationForms) extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsNominatedAssets)
  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[BasicEstateElement](applicationForms.nominatedForm, nominated.apply, _.allAssets.flatMap(_.nominated))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], BasicEstateElement) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, nominated) => {
          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
            (new AllAssets(action = None, nominated = Some(nominated)))

          (nominated.isOwned match {
            case Some(true) => _.copy(nominated = Some(nominated))
            case Some(false) => _.copy(nominated = Some(nominated.copy(value = None)))
            case None => throw new RuntimeException("Not able to retrieve the value of NominatedAsset question")
          })
          ))
          (updatedAD, None)
        }
      estateElementOnSubmit[BasicEstateElement](applicationForms.nominatedForm,
        nominated.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(assetsRedirectLocation, Some(ihtProperties.AppSectionNominatedID))
      )
    }
  }
}
