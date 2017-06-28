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
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.BasicEstateElement
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset._
import play.api.i18n.MessagesApi


@Singleton
class OtherController @Inject()(val messagesApi: MessagesApi, val ihtProperties: IhtProperties) extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsOther)

  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[BasicEstateElement](otherForm, other.apply, _.allAssets.flatMap(_.other))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], BasicEstateElement) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, other) => {
          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, other = Some(other)))

          (other.isOwned match {
            case Some(true) => _.copy(other = Some(other))
            case Some(false) => _.copy(other = Some(other.copy(value= None)))
            case None => throw new RuntimeException("Not able to retrieve the value of OtherAsset question")
          })
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[BasicEstateElement](otherForm,
        other.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(assetsRedirectLocation, Some(ihtProperties.AppSectionOtherID))
      )
    }
  }
}
